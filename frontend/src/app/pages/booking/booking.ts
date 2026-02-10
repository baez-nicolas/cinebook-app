import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import Swal from 'sweetalert2';
import { Seat } from '../../models/seat.model';
import { Showtime } from '../../models/showtime.model';
import { ApiService } from '../../services/api.service';

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
  userName: string = 'user1';
  loading = true;
  error: string | null = null;
  processing = false;
  rows: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'];
  seatsPerRow: number = 12;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
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
      error: (err) => {
        this.error = 'Error al cargar la función';
        this.loading = false;
        console.error(err);
      },
    });
  }

  loadSeats(showtimeId: number): void {
    this.apiService.getSeatsByShowtime(showtimeId).subscribe({
      next: (seats) => {
        this.seats = seats;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los asientos';
        this.loading = false;
        console.error(err);
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
    if (this.processing || !this.userName.trim() || this.selectedSeats.length === 0) {
      return;
    }

    const bookingData = {
      userName: this.userName,
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
      title: '💳 Confirmar Pago',
      html: `
        <div style="text-align: left; padding: 20px;">
          <h3 style="color: #FFD700; margin-bottom: 20px; text-align: center;">📋 Resumen de tu Reserva</h3>

          <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 10px; margin-bottom: 15px;">
            <p style="margin: 10px 0;"><strong>🎬 Película:</strong> ${bookingData.movieTitle}</p>
            <p style="margin: 10px 0;"><strong>🏢 Cine:</strong> ${bookingData.cinemaName}</p>
            <p style="margin: 10px 0;"><strong>📅 Fecha y Hora:</strong> ${this.formatDateForDialog(bookingData.showDateTime)}</p>
            <p style="margin: 10px 0;"><strong>🎞️ Tipo:</strong> ${this.getTypeLabel(bookingData.showtimeType)}</p>
            <p style="margin: 10px 0;"><strong>💺 Asientos:</strong> ${bookingData.seatNumbers.join(', ')}</p>
            <p style="margin: 10px 0;"><strong>👤 Usuario:</strong> ${bookingData.userName}</p>
          </div>

          <hr style="margin: 20px 0; border: 1px solid #FFD700;">

          <p style="font-size: 1.8rem; color: #FFD700; text-align: center; font-weight: bold; margin: 20px 0;">
            💰 Total a Pagar: $${bookingData.totalPrice.toLocaleString('es-AR')}
          </p>

          <p style="text-align: center; color: #ccc; font-size: 0.9rem; margin-top: 15px;">
            ⚠️ Esta es una simulación de pago. No se realizará ningún cargo real.
          </p>
        </div>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: '✅ Confirmar Pago',
      cancelButtonText: '❌ Cancelar',
      confirmButtonColor: '#8B0000',
      cancelButtonColor: '#666',
      background: '#1a1a1a',
      color: '#fff',
      width: '600px',
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
          confirmButtonColor: '#8B0000',
          background: '#1a1a1a',
          color: '#fff',
        });
      }
    });
  }

  processPayment(bookingData: any): void {
    this.processing = true;

    console.log('🎬 DATOS DE RESERVA:', bookingData);
    console.log('👤 Usuario:', bookingData.userName);

    Swal.fire({
      title: 'Procesando pago...',
      html: '<div style="text-align: center;"><div class="custom-spinner"></div><p style="margin-top: 20px; color: #FFD700;">Por favor, espera un momento</p></div>',
      allowOutsideClick: false,
      showConfirmButton: false,
      background: '#1a1a1a',
      color: '#fff',
      didOpen: () => {
        Swal.showLoading();
      },
    });

    setTimeout(() => {
      const request = {
        showtimeId: bookingData.showtimeId,
        seatIds: bookingData.seatIds,
      };

      console.log('📤 ENVIANDO AL BACKEND:', request);

      this.apiService.createBooking(bookingData.userName, request).subscribe({
        next: (response) => {
          console.log('✅ RESPUESTA DEL BACKEND:', response);
          this.processing = false;
          this.showSuccessMessage(response);
        },
        error: (err) => {
          console.error('❌ ERROR COMPLETO:', err);
          console.error('❌ ERROR MESSAGE:', err.message);
          console.error('❌ ERROR ERROR:', err.error);
          this.processing = false;
          Swal.fire({
            title: 'Error en el Pago',
            text:
              'Hubo un problema al procesar tu reserva: ' +
              (err.error?.message || err.message || 'Error desconocido'),
            icon: 'error',
            confirmButtonText: 'Entendido',
            confirmButtonColor: '#8B0000',
            background: '#1a1a1a',
            color: '#fff',
          });
        },
      });
    }, 2000);
  }

  showSuccessMessage(booking: any): void {
    Swal.fire({
      title: '🎉 ¡Pago Exitoso!',
      html: `
        <div style="text-align: center; padding: 20px;">
          <h2 style="color: #FFD700; margin-bottom: 25px;">¡Tu reserva fue confirmada!</h2>

          <div style="background: linear-gradient(135deg, rgba(255,215,0,0.2) 0%, rgba(139,0,0,0.2) 100%); padding: 25px; border-radius: 15px; margin: 25px 0; border: 2px solid #FFD700;">
            <p style="font-size: 1.1rem; margin-bottom: 10px; color: #fff;">Código de Confirmación:</p>
            <p style="font-size: 2.5rem; color: #FFD700; font-weight: bold; letter-spacing: 2px; margin: 10px 0;">
              ${booking.confirmationCode}
            </p>
          </div>

          <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 10px; text-align: left; margin: 20px 0;">
            <p style="margin: 12px 0;"><strong>🎬</strong> ${booking.movieTitle}</p>
            <p style="margin: 12px 0;"><strong>🏢</strong> ${booking.cinemaName}</p>
            <p style="margin: 12px 0;"><strong>📅</strong> ${this.formatDateForDialog(booking.showDateTime)}</p>
            <p style="margin: 12px 0;"><strong>💺</strong> ${booking.seatNumbers.join(', ')}</p>
            <p style="margin: 12px 0;"><strong>👤</strong> ${booking.userName}</p>
            <p style="margin: 12px 0; font-size: 1.3rem; color: #FFD700;"><strong>💰</strong> $${booking.totalPrice.toLocaleString('es-AR')}</p>
          </div>

          <p style="margin-top: 25px; color: #ccc; font-size: 1rem;">
            📱 Guarda este código para retirar tus entradas en el cine
          </p>
        </div>
      `,
      icon: 'success',
      confirmButtonText: '🏠 Volver al Inicio',
      confirmButtonColor: '#8B0000',
      background: '#1a1a1a',
      color: '#fff',
      width: '650px',
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
