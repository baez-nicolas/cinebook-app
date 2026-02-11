import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import Swal from 'sweetalert2';
import { FilterPipe } from '../../pipes/filter.pipe';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface Booking {
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
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, FilterPipe],
  templateUrl: './my-bookings.html',
  styleUrl: './my-bookings.css',
})
export class MyBookingsComponent implements OnInit {
  bookings: Booking[] = [];
  allBookings: Booking[] = [];
  loading = false;
  isAdmin = false;
  selectedUserEmail = '';
  uniqueUserEmails: string[] = [];

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();

    if (this.isAdmin) {
      this.loadAllBookingsForAdmin();
    } else {
      this.loadMyBookings();
    }
  }

  loadMyBookings(): void {
    this.loading = true;

    this.apiService.getMyBookings().subscribe({
      next: (data) => {
        this.bookings = data.sort(
          (a, b) => new Date(b.bookingDateTime).getTime() - new Date(a.bookingDateTime).getTime(),
        );
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar reservas:', err);
        this.loading = false;
        Swal.fire({
          title: 'Error',
          text: 'No se pudieron cargar las reservas',
          icon: 'error',
          confirmButtonText: 'Entendido',
          confirmButtonColor: '#8B0000',
          background: '#1a1a1a',
          color: '#fff',
        });
      },
    });
  }

  loadAllBookingsForAdmin(): void {
    this.loading = true;

    this.apiService.getAllBookings().subscribe({
      next: (data) => {
        this.allBookings = data.sort(
          (a, b) => new Date(b.bookingDateTime).getTime() - new Date(a.bookingDateTime).getTime(),
        );

        const emailsSet = new Set(
          this.allBookings.map((b) => this.extractEmailFromUserName(b.userName)),
        );
        this.uniqueUserEmails = Array.from(emailsSet).sort();

        this.bookings = this.allBookings;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar todas las reservas (admin):', err);
        this.loading = false;
        Swal.fire({
          title: 'Error',
          text: 'No se pudieron cargar las reservas',
          icon: 'error',
          confirmButtonText: 'Entendido',
          confirmButtonColor: '#8B0000',
          background: '#1a1a1a',
          color: '#fff',
        });
      },
    });
  }

  private extractEmailFromUserName(userName: string): string {
    return userName;
  }

  onUserFilterChange(): void {
    if (!this.selectedUserEmail) {
      this.bookings = this.allBookings;
    } else {
      this.bookings = this.allBookings.filter((b) => b.userName === this.selectedUserEmail);
    }
  }

  clearFilter(): void {
    this.selectedUserEmail = '';
    this.bookings = this.allBookings;
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

  showBookingDetails(booking: Booking): void {
    Swal.fire({
      title: '🎟️ Detalles de la Reserva',
      html: `
        <div style="text-align: center; padding: 20px;">
          <div style="background: linear-gradient(135deg, rgba(255,215,0,0.2) 0%, rgba(139,0,0,0.2) 100%); padding: 25px; border-radius: 15px; margin: 25px 0; border: 2px solid #FFD700;">
            <p style="font-size: 1.1rem; margin-bottom: 10px; color: #fff;">Código de Confirmación:</p>
            <p style="font-size: 2.5rem; color: #FFD700; font-weight: bold; letter-spacing: 2px; margin: 10px 0;">
              ${booking.confirmationCode}
            </p>
          </div>

          <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 10px; text-align: left; margin: 20px 0;">
            <p style="margin: 12px 0;"><strong>🎬 Película:</strong> ${booking.movieTitle}</p>
            <p style="margin: 12px 0;"><strong>🏢 Cine:</strong> ${booking.cinemaName}</p>
            <p style="margin: 12px 0;"><strong>📍 Dirección:</strong> ${booking.cinemaAddress}</p>
            <p style="margin: 12px 0;"><strong>📅 Función:</strong> ${this.formatDate(booking.showDateTime)}</p>
            <p style="margin: 12px 0;"><strong>🎞️ Tipo:</strong> ${this.getTypeLabel(booking.showtimeType)}</p>
            <p style="margin: 12px 0;"><strong>💺 Asientos:</strong> ${booking.seatNumbers.join(', ')}</p>
            <p style="margin: 12px 0;"><strong>👤 Usuario:</strong> ${booking.userName}</p>
            <p style="margin: 12px 0; font-size: 1.3rem; color: #FFD700;"><strong>💰 Total:</strong> $${booking.totalPrice.toLocaleString('es-AR')}</p>
          </div>

          <p style="margin-top: 25px; color: #ccc; font-size: 1rem;">
            📱 Presenta este código en el cine para retirar tus entradas
          </p>
        </div>
      `,
      icon: 'info',
      confirmButtonText: 'Cerrar',
      confirmButtonColor: '#8B0000',
      background: '#1a1a1a',
      color: '#fff',
      width: '650px',
    });
  }

  isPastShowtime(dateString: string): boolean {
    return new Date(dateString) < new Date();
  }
}
