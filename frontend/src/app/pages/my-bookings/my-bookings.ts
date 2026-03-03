import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import Swal from 'sweetalert2';
import { FooterComponent } from '../../components/footer/footer';
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
  imports: [CommonModule, RouterModule, FormsModule, FooterComponent],
  templateUrl: './my-bookings.html',
  styleUrl: './my-bookings.css',
})
export class MyBookingsComponent implements OnInit, OnDestroy {
  bookings: Booking[] = [];
  allBookings: Booking[] = [];
  filteredBookings: Booking[] = [];
  searchTerm = '';
  loading = false;
  isAdmin = false;
  currentUser: any = null;
  menuOpen = false;
  private refreshSubscription?: Subscription;
  private readonly MOVIE_DURATION_HOURS = 2.5;
  private readonly EXPIRY_HOURS = 5;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.isAdmin = this.authService.isAdmin();

    if (this.isAdmin) {
      this.loadAllBookingsForAdmin();
    } else {
      this.loadMyBookings();
    }

    // Actualizar lista cada 5 minutos para mantener sincronía con el filtro de expiración
    this.refreshSubscription = interval(5 * 60 * 1000).subscribe(() => {
      this.applyExpirationFilter();
    });
  }

  ngOnDestroy(): void {
    this.refreshSubscription?.unsubscribe();
  }

  /**
   * Filtra las reservas excluyendo aquellas cuyas funciones terminaron hace más de 5 horas.
   * Se considera que una función termina después de showDateTime + duración de la película.
   */
  private filterExpiredBookings(bookings: Booking[]): Booking[] {
    const now = new Date();
    const cutoffTime = new Date(now.getTime() - this.EXPIRY_HOURS * 60 * 60 * 1000);

    return bookings.filter((booking) => {
      const showtimeStart = new Date(booking.showDateTime);
      const showtimeEnd = new Date(
        showtimeStart.getTime() + this.MOVIE_DURATION_HOURS * 60 * 60 * 1000,
      );
      // Mostrar solo si la función no terminó hace más de 5 horas
      return showtimeEnd > cutoffTime;
    });
  }

  /**
   * Re-aplica el filtro de expiración a las reservas actuales (para actualización reactiva)
   */
  private applyExpirationFilter(): void {
    if (this.isAdmin) {
      this.allBookings = this.filterExpiredBookings(this.allBookings);
      this.filteredBookings = this.filterExpiredBookings(this.filteredBookings);
      this.bookings = this.allBookings;
    } else {
      this.bookings = this.filterExpiredBookings(this.bookings);
    }
  }

  loadMyBookings(): void {
    this.loading = true;

    this.apiService.getMyBookings().subscribe({
      next: (data) => {
        const sortedBookings = data.sort(
          (a, b) => new Date(b.bookingDateTime).getTime() - new Date(a.bookingDateTime).getTime(),
        );
        // Filtrar reservas de funciones finalizadas hace más de 5 horas
        this.bookings = this.filterExpiredBookings(sortedBookings);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        Swal.fire({
          title: 'Error',
          text: 'No se pudieron cargar las reservas',
          icon: 'error',
          confirmButtonText: 'Entendido',
          confirmButtonColor: '#8B0000',
          background: '#1a1a1a',
          color: '#fff',
          scrollbarPadding: false,
          heightAuto: false,
          returnFocus: false,
        });
      },
    });
  }

  loadAllBookingsForAdmin(): void {
    this.loading = true;

    this.apiService.getAllBookings().subscribe({
      next: (data) => {
        const sortedBookings = data.sort(
          (a, b) => new Date(b.bookingDateTime).getTime() - new Date(a.bookingDateTime).getTime(),
        );
        // Filtrar reservas de funciones finalizadas hace más de 5 horas
        this.allBookings = this.filterExpiredBookings(sortedBookings);
        this.bookings = this.allBookings;
        this.filteredBookings = [...this.allBookings];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        Swal.fire({
          title: 'Error',
          text: 'No se pudieron cargar las reservas',
          icon: 'error',
          confirmButtonText: 'Entendido',
          confirmButtonColor: '#8B0000',
          background: '#1a1a1a',
          color: '#fff',
          scrollbarPadding: false,
          heightAuto: false,
          returnFocus: false,
        });
      },
    });
  }

  private extractEmailFromUserName(userName: string): string {
    return userName;
  }

  private normalizeText(text: string): string {
    return text
      .toLowerCase()
      .replace(/,/g, '')
      .replace(/\./g, '')
      .replace(/ de /g, ' ')
      .replace(/ del /g, ' ')
      .replace(/ a las /g, ' ')
      .replace(/ las /g, ' ')
      .replace(/ los /g, ' ')
      .replace(/ el /g, ' ')
      .replace(/ la /g, ' ')
      .replace(/\s+/g, ' ')
      .trim();
  }

  filterBookings(): void {
    const term = this.normalizeText(this.searchTerm);
    if (!term) {
      this.filteredBookings = [...this.allBookings];
      return;
    }
    this.filteredBookings = this.allBookings.filter(
      (booking) =>
        this.normalizeText(booking.userName || '').includes(term) ||
        this.normalizeText(booking.movieTitle || '').includes(term) ||
        this.normalizeText(booking.cinemaName || '').includes(term) ||
        this.normalizeText(booking.showDateTime || '').includes(term) ||
        this.normalizeText(this.formatDate(booking.showDateTime)).includes(term) ||
        this.normalizeText(booking.showtimeType || '').includes(term) ||
        this.normalizeText(this.getTypeLabel(booking.showtimeType)).includes(term) ||
        this.normalizeText(booking.confirmationCode || '').includes(term) ||
        booking.totalPrice?.toString().includes(term),
    );
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
      timeZone: 'America/Argentina/Buenos_Aires',
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

  showBookingDetails(booking: Booking, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    (event.target as HTMLElement)?.blur();

    const scrollPos = window.scrollY;
    document.body.style.top = `-${scrollPos}px`;

    Swal.fire({
      title: 'Detalles de la Reserva',
      html: `
        <div style="text-align: center; padding: 10px;">
          <div style="background: rgba(34, 197, 94, 0.1); padding: 20px; border-radius: 12px; margin: 15px 0; border: 2px solid #22c55e;">
            <p style="font-size: 0.9rem; margin-bottom: 8px; color: #a3a3a3;">Código de Confirmación</p>
            <p style="font-size: clamp(1.5rem, 5vw, 2rem); color: #4ade80; font-weight: bold; letter-spacing: 2px; margin: 5px 0; word-break: break-all;">
              ${booking.confirmationCode}
            </p>
          </div>

          <div style="background: rgba(255,255,255,0.05); padding: 15px; border-radius: 12px; text-align: left; margin: 15px 0; border: 1px solid rgba(255,255,255,0.1);">
            <p style="margin: 10px 0; color: #e5e5e5; font-size: clamp(0.85rem, 3vw, 0.95rem);"><strong style="color: #fff;">Película:</strong> ${booking.movieTitle}</p>
            <p style="margin: 10px 0; color: #e5e5e5; font-size: clamp(0.85rem, 3vw, 0.95rem);"><strong style="color: #fff;">Cine:</strong> ${booking.cinemaName}</p>
            <p style="margin: 10px 0; color: #e5e5e5; font-size: clamp(0.85rem, 3vw, 0.95rem);"><strong style="color: #fff;">Dirección:</strong> ${booking.cinemaAddress}</p>
            <p style="margin: 10px 0; color: #e5e5e5; font-size: clamp(0.85rem, 3vw, 0.95rem);"><strong style="color: #fff;">Función:</strong> ${this.formatDate(booking.showDateTime)}</p>
            <p style="margin: 10px 0; color: #e5e5e5; font-size: clamp(0.85rem, 3vw, 0.95rem);"><strong style="color: #fff;">Formato:</strong> ${this.getTypeLabel(booking.showtimeType)}</p>
            <p style="margin: 10px 0; color: #e5e5e5; font-size: clamp(0.85rem, 3vw, 0.95rem);"><strong style="color: #fff;">Asientos:</strong> ${booking.seatNumbers.join(', ')}</p>
            <p style="margin: 10px 0; font-size: clamp(1rem, 3.5vw, 1.2rem); color: #4ade80;"><strong>Total:</strong> $${booking.totalPrice.toLocaleString('es-AR')}</p>
          </div>

          <p style="margin-top: 15px; color: #a3a3a3; font-size: clamp(0.8rem, 2.5vw, 0.9rem);">
            Presenta este código en el cine para retirar tus entradas
          </p>
        </div>
      `,
      icon: 'info',
      confirmButtonText: 'Cerrar',
      confirmButtonColor: '#22c55e',
      background: '#0a0a0a',
      color: '#fff',
      width: 'auto',
      scrollbarPadding: false,
      heightAuto: false,
      focusConfirm: false,
      returnFocus: false,
      didClose: () => {
        document.body.style.top = '';
        window.scrollTo(0, scrollPos);
      },
    });
  }

  isPastShowtime(dateString: string): boolean {
    return new Date(dateString) < new Date();
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
