import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

interface BookingData {
  bookingId: number;
  confirmationCode: string;
  userName: string;
  movieTitle: string;
  moviePosterUrl: string;
  cinemaName: string;
  cinemaAddress: string;
  showDateTime: string;
  showtimeType: string;
  seatNumbers: string[];
  totalPrice: number;
  paymentStatus: string;
  bookingDateTime: string;
}

@Component({
  selector: 'app-booking-confirmation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './booking-confirmation.html',
  styleUrl: './booking-confirmation.css',
})
export class BookingConfirmationComponent implements OnInit {
  booking: BookingData | null = null;
  processing = false;

  constructor(private router: Router) {
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras?.state) {
      this.booking = navigation.extras.state['booking'];
    }
  }

  ngOnInit(): void {
    if (!this.booking) {
      this.router.navigate(['/movies']);
      return;
    }

    this.showPaymentDialog();
  }

  showPaymentDialog(): void {
    if (!this.booking) return;

    Swal.fire({
      title: '💳 Confirmar Pago',
      html: `
        <div style="text-align: left; padding: 20px;">
          <h3 style="color: #FFD700; margin-bottom: 20px; text-align: center;">📋 Resumen de tu Reserva</h3>

          <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 10px; margin-bottom: 15px;">
            <p style="margin: 10px 0;"><strong>🎬 Película:</strong> ${this.booking.movieTitle}</p>
            <p style="margin: 10px 0;"><strong>🏢 Cine:</strong> ${this.booking.cinemaName}</p>
            <p style="margin: 10px 0;"><strong>📍 Dirección:</strong> ${this.booking.cinemaAddress}</p>
            <p style="margin: 10px 0;"><strong>📅 Fecha y Hora:</strong> ${this.formatDate(this.booking.showDateTime)}</p>
            <p style="margin: 10px 0;"><strong>🎞️ Tipo:</strong> ${this.getTypeLabel(this.booking.showtimeType)}</p>
            <p style="margin: 10px 0;"><strong>💺 Asientos:</strong> ${this.booking.seatNumbers.join(', ')}</p>
            <p style="margin: 10px 0;"><strong>👤 Usuario:</strong> ${this.booking.userName}</p>
          </div>

          <hr style="margin: 20px 0; border: 1px solid #FFD700;">

          <p style="font-size: 1.8rem; color: #FFD700; text-align: center; font-weight: bold; margin: 20px 0;">
            💰 Total a Pagar: $${this.booking.totalPrice.toLocaleString('es-AR')}
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
        this.processPayment();
      } else {
        this.router.navigate(['/movies']);
      }
    });
  }

  processPayment(): void {
    this.processing = true;

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
      this.processing = false;
      this.showSuccessMessage();
    }, 2500);
  }

  showSuccessMessage(): void {
    if (!this.booking) return;

    Swal.fire({
      title: '🎉 ¡Pago Exitoso!',
      html: `
        <div style="text-align: center; padding: 20px;">
          <h2 style="color: #FFD700; margin-bottom: 25px;">¡Tu reserva fue confirmada!</h2>

          <div style="background: linear-gradient(135deg, rgba(255,215,0,0.2) 0%, rgba(139,0,0,0.2) 100%); padding: 25px; border-radius: 15px; margin: 25px 0; border: 2px solid #FFD700;">
            <p style="font-size: 1.1rem; margin-bottom: 10px; color: #fff;">Código de Confirmación:</p>
            <p style="font-size: 2.5rem; color: #FFD700; font-weight: bold; letter-spacing: 2px; margin: 10px 0;">
              ${this.booking.confirmationCode}
            </p>
          </div>

          <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 10px; text-align: left; margin: 20px 0;">
            <p style="margin: 12px 0;"><strong>🎬</strong> ${this.booking.movieTitle}</p>
            <p style="margin: 12px 0;"><strong>🏢</strong> ${this.booking.cinemaName}</p>
            <p style="margin: 12px 0;"><strong>📅</strong> ${this.formatDate(this.booking.showDateTime)}</p>
            <p style="margin: 12px 0;"><strong>💺</strong> ${this.booking.seatNumbers.join(', ')}</p>
            <p style="margin: 12px 0;"><strong>👤</strong> ${this.booking.userName}</p>
            <p style="margin: 12px 0; font-size: 1.3rem; color: #FFD700;"><strong>💰</strong> $${this.booking.totalPrice.toLocaleString('es-AR')}</p>
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
}
