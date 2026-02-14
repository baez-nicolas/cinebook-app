import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import Swal from 'sweetalert2';
import { Seat } from '../../models/seat.model';
import { Showtime } from '../../models/showtime.model';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking.html',
  styleUrl: './booking.css',
})
export class BookingComponent implements OnInit {
  showtime: Showtime | null = null;
  showtimeId: number = 0;
  seats: Seat[] = [];
  selectedSeats: Seat[] = [];
  loading = true;
  error: string | null = null;
  processing = false;
  rows: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'];
  seatsPerRow: number = 12;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('showtimeId');
    if (id) {
      this.showtimeId = +id;
      this.loadShowtime(this.showtimeId);
    }
  }

  loadShowtime(id: number): void {
    this.apiService.getShowtimeById(id).subscribe({
      next: (showtime) => {
        this.showtime = showtime;
        this.loadSeats(id);
      },
      error: () => {
        this.error = 'Error al cargar la función';
        this.loading = false;
      },
    });
  }

  loadSeats(showtimeId: number): void {
    this.apiService.getSeatsByShowtime(showtimeId).subscribe({
      next: (seats) => {
        this.seats = seats;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar los asientos';
        this.loading = false;
      },
    });
  }

  toggleSeat(seat: Seat): void {
    if (seat.status !== 'AVAILABLE') return;

    const index = this.selectedSeats.findIndex((s) => s.id === seat.id);

    if (index > -1) {
      this.selectedSeats.splice(index, 1);
    } else {
      this.selectedSeats.push(seat);
    }
  }

  isSeatSelected(seat: Seat): boolean {
    return this.selectedSeats.some((s) => s.id === seat.id);
  }

  getSeatClass(seat: Seat): string {
    if (this.isSeatSelected(seat)) return 'selected';
    return seat.status.toLowerCase();
  }

  getTotal(): number {
    if (!this.showtime) return 0;
    return this.selectedSeats.length * this.showtime.price;
  }

  get totalPrice(): number {
    return this.getTotal();
  }

  confirmBooking(): void {
    if (this.processing || this.selectedSeats.length === 0) {
      return;
    }

    const userEmail = this.authService.getUserEmail();
    if (!userEmail) {
      Swal.fire({
        title: 'No autenticado',
        text: 'Debes iniciar sesión para realizar una reserva',
        icon: 'warning',
        confirmButtonText: 'Ir a Login',
        confirmButtonColor: '#22c55e',
        background: '#0a0a0a',
        color: '#fff',
        scrollbarPadding: false,
        heightAuto: false,
      }).then(() => {
        this.router.navigate(['/login']);
      });
      return;
    }

    const bookingData = {
      userEmail: userEmail,
      showtimeId: this.showtimeId,
      seatIds: this.selectedSeats.map((seat) => seat.id),
      seatNumbers: this.selectedSeats.map((seat) => seat.seatNumber),
      totalPrice: this.totalPrice,
      movieTitle: this.showtime?.movieTitle || '',
      moviePosterUrl: this.showtime?.moviePosterUrl || '',
      cinemaName: this.showtime?.cinemaName || '',
      showDateTime: this.showtime?.showDateTime || '',
      showtimeType: this.showtime?.type || '',
    };

    this.showPaymentDialog(bookingData);
  }

  showPaymentDialog(bookingData: any): void {
    Swal.fire({
      title: 'Confirmar Pago',
      html: `
        <div style="text-align: center; padding: 10px;">
          <h3 style="color: #4ade80; margin-bottom: 15px; font-size: clamp(1rem, 4vw, 1.2rem);">Resumen de tu Reserva</h3>

          <div style="background: rgba(255,255,255,0.05); padding: 15px; border-radius: 12px; margin-bottom: 15px; border: 1px solid rgba(255,255,255,0.1); text-align: left;">
            <p style="margin: 8px 0; color: #e5e5e5; font-size: clamp(0.8rem, 3vw, 0.95rem); word-break: break-word;"><strong style="color: #fff;">Película:</strong> ${bookingData.movieTitle}</p>
            <p style="margin: 8px 0; color: #e5e5e5; font-size: clamp(0.8rem, 3vw, 0.95rem);"><strong style="color: #fff;">Cine:</strong> ${bookingData.cinemaName}</p>
            <p style="margin: 8px 0; color: #e5e5e5; font-size: clamp(0.8rem, 3vw, 0.95rem); word-break: break-word;"><strong style="color: #fff;">Fecha:</strong> ${this.formatDateForDialog(bookingData.showDateTime)}</p>
            <p style="margin: 8px 0; color: #e5e5e5; font-size: clamp(0.8rem, 3vw, 0.95rem);"><strong style="color: #fff;">Tipo:</strong> ${this.getTypeLabel(bookingData.showtimeType)}</p>
            <p style="margin: 8px 0; color: #e5e5e5; font-size: clamp(0.8rem, 3vw, 0.95rem);"><strong style="color: #fff;">Asientos:</strong> ${bookingData.seatNumbers.join(', ')}</p>
          </div>

          <div style="background: rgba(34, 197, 94, 0.1); padding: 15px; border-radius: 12px; border: 1px solid #22c55e; margin: 15px 0;">
            <p style="font-size: clamp(1.2rem, 5vw, 1.6rem); color: #4ade80; font-weight: bold; margin: 0;">
              $${bookingData.totalPrice.toLocaleString('es-AR')}
            </p>
            <p style="color: #a3a3a3; font-size: 0.75rem; margin-top: 5px;">Total a pagar</p>
          </div>

          <p style="color: #a3a3a3; font-size: clamp(0.7rem, 2.5vw, 0.8rem); margin-top: 10px;">
            Simulación de pago - Sin cargo real
          </p>
        </div>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Confirmar Pago',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#22c55e',
      cancelButtonColor: '#404040',
      background: '#0a0a0a',
      color: '#fff',
      width: 'auto',
      scrollbarPadding: false,
      heightAuto: false,
      customClass: {
        popup: 'custom-swal-popup',
        title: 'custom-swal-title',
        confirmButton: 'custom-swal-confirm',
        cancelButton: 'custom-swal-cancel',
      },
    }).then((result) => {
      if (result.isConfirmed) {
        this.processPayment(bookingData);
      } else {
        Swal.fire({
          title: 'Reserva Cancelada',
          text: 'Tu reserva no fue procesada. Los asientos no fueron reservados.',
          icon: 'info',
          confirmButtonText: 'Entendido',
          confirmButtonColor: '#22c55e',
          background: '#0a0a0a',
          color: '#fff',
          scrollbarPadding: false,
          heightAuto: false,
        });
      }
    });
  }

  processPayment(bookingData: any): void {
    this.processing = true;

    Swal.fire({
      title: 'Procesando pago...',
      html: '<div style="text-align: center;"><p style="margin-top: 20px; color: #a3a3a3;">Por favor, espera un momento</p></div>',
      allowOutsideClick: false,
      showConfirmButton: false,
      background: '#0a0a0a',
      color: '#fff',
      scrollbarPadding: false,
      heightAuto: false,
      didOpen: () => {
        Swal.showLoading();
      },
    });

    setTimeout(() => {
      const request = {
        showtimeId: bookingData.showtimeId,
        seatIds: bookingData.seatIds,
      };

      this.apiService.createBooking(request).subscribe({
        next: (response) => {
          this.processing = false;
          this.showSuccessMessage(response);
        },
        error: (err) => {
          this.processing = false;
          Swal.fire({
            title: 'Error en el Pago',
            text:
              'Hubo un problema al procesar tu reserva: ' +
              (err.error?.message || err.message || 'Error desconocido'),
            icon: 'error',
            confirmButtonText: 'Entendido',
            confirmButtonColor: '#22c55e',
            background: '#0a0a0a',
            color: '#fff',
            scrollbarPadding: false,
            heightAuto: false,
          });
        },
      });
    }, 2000);
  }

  showSuccessMessage(booking: any): void {
    Swal.fire({
      title: '¡Pago Exitoso!',
      html: `
        <div style="text-align: center; padding: 10px;">
          <p style="color: #4ade80; margin-bottom: 15px; font-size: clamp(0.95rem, 4vw, 1.1rem);">¡Tu reserva fue confirmada!</p>

          <div style="background: rgba(34, 197, 94, 0.1); padding: 15px; border-radius: 12px; margin: 15px 0; border: 2px solid #22c55e;">
            <p style="font-size: clamp(0.8rem, 3vw, 0.9rem); margin-bottom: 5px; color: #a3a3a3;">Código de Confirmación</p>
            <p style="font-size: clamp(1.5rem, 6vw, 2.2rem); color: #4ade80; font-weight: bold; letter-spacing: 2px; margin: 5px 0; word-break: break-all;">
              ${booking.confirmationCode}
            </p>
          </div>

          <div style="background: rgba(255,255,255,0.05); padding: 12px 15px; border-radius: 12px; text-align: left; margin: 15px 0; border: 1px solid rgba(255,255,255,0.1);">
            <p style="margin: 8px 0; color: #e5e5e5; font-size: clamp(0.8rem, 3vw, 0.9rem); word-break: break-word;"><strong style="color: #fff;">Película:</strong> ${booking.movieTitle}</p>
            <p style="margin: 8px 0; color: #e5e5e5; font-size: clamp(0.8rem, 3vw, 0.9rem);"><strong style="color: #fff;">Cine:</strong> ${booking.cinemaName}</p>
            <p style="margin: 8px 0; color: #e5e5e5; font-size: clamp(0.8rem, 3vw, 0.9rem);"><strong style="color: #fff;">Asientos:</strong> ${booking.seatNumbers.join(', ')}</p>
            <p style="margin: 8px 0; color: #4ade80; font-size: clamp(0.9rem, 3.5vw, 1.1rem);"><strong>Total:</strong> $${booking.totalPrice.toLocaleString('es-AR')}</p>
          </div>

          <p style="color: #a3a3a3; font-size: clamp(0.75rem, 2.5vw, 0.85rem); margin-top: 10px;">
            Guarda este código para retirar tus entradas
          </p>
        </div>
      `,
      icon: 'success',
      confirmButtonText: 'Volver al Inicio',
      confirmButtonColor: '#22c55e',
      background: '#0a0a0a',
      color: '#fff',
      width: 'auto',
      scrollbarPadding: false,
      heightAuto: false,
      customClass: {
        popup: 'custom-swal-popup',
        confirmButton: 'custom-swal-confirm',
      },
    }).then(() => {
      this.router.navigate(['/movies']);
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const options: Intl.DateTimeFormatOptions = {
      weekday: 'short',
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    };
    return date.toLocaleDateString('es-AR', options);
  }

  formatDateForDialog(dateString: string): string {
    const date = new Date(dateString);
    const options: Intl.DateTimeFormatOptions = {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    };
    return date.toLocaleDateString('es-AR', options);
  }

  getTypeLabel(type: string): string {
    const labels: { [key: string]: string } = {
      SPANISH_2D: '2D Español',
      SUBTITLED_2D: '2D Subtitulada',
      SPANISH_3D: '3D Español',
    };
    return labels[type] || type;
  }

  goBack(): void {
    window.history.back();
  }

  getSeatsByRow(row: string): Seat[] {
    return this.seats
      .filter((seat) => seat.seatNumber.startsWith(row))
      .sort((a, b) => this.getColumnNumber(a) - this.getColumnNumber(b));
  }

  getColumnNumber(seat: Seat): number {
    const match = seat.seatNumber.match(/\d+$/);
    return match ? parseInt(match[0], 10) : 0;
  }
}
