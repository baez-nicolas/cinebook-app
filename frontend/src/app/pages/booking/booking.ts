import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingRequest } from '../../models/booking.model';
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
  seats: Seat[] = [];
  selectedSeats: Seat[] = [];
  userName: string = 'user1';
  loading = true;
  error: string | null = null;
  processing = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('showtimeId');
    if (id) {
      this.loadShowtime(+id);
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

  confirmBooking(): void {
    if (!this.userName.trim()) {
      alert('Por favor ingresá tu nombre de usuario');
      return;
    }

    if (this.selectedSeats.length === 0) {
      alert('Por favor seleccioná al menos un asiento');
      return;
    }

    if (!this.showtime) return;

    this.processing = true;

    const request: BookingRequest = {
      showtimeId: this.showtime.id,
      seatIds: this.selectedSeats.map((s) => s.id),
    };

    this.apiService.createBooking(this.userName, request).subscribe({
      next: (booking) => {
        this.router.navigate(['/booking-confirmation'], {
          state: { booking },
        });
      },
      error: (err) => {
        this.processing = false;
        alert('Error al confirmar la reserva: ' + (err.error?.message || 'Intenta nuevamente'));
        console.error(err);
      },
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

  getRowSeats(row: string): Seat[] {
    return this.seats.filter((seat) => seat.seatNumber.startsWith(row));
  }

  get rows(): string[] {
    return ['A', 'B', 'C', 'D', 'E'];
  }
}
