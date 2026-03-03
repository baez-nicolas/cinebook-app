import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './movie-detail.html',
  styleUrl: './movie-detail.css',
})
export class MovieDetailComponent implements OnInit {
  movie: any = null;
  cinemas: any[] = [];
  availableDates: { label: string; value: string }[] = [];
  allShowtimes: any[] = [];
  filteredShowtimes: any[] = [];

  selectedCinemaId: number | null = null;
  selectedDate: string = '';

  loading = true;
  loadingShowtimes = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
  ) {}

  private parseArgentinaDate(dateString: string): Date {
    const hasTimezone =
      dateString.includes('Z') || dateString.includes('+') || dateString.includes('-', 11);
    const correctedDateString = hasTimezone ? dateString : dateString + '-03:00';
    return new Date(correctedDateString);
  }

  private getDateString(date: Date): string {
    const year = date.toLocaleDateString('es-AR', {
      year: 'numeric',
      timeZone: 'America/Argentina/Buenos_Aires',
    });
    const month = date.toLocaleDateString('es-AR', {
      month: '2-digit',
      timeZone: 'America/Argentina/Buenos_Aires',
    });
    const day = date.toLocaleDateString('es-AR', {
      day: '2-digit',
      timeZone: 'America/Argentina/Buenos_Aires',
    });
    return `${year}-${month}-${day}`;
  }

  ngOnInit(): void {
    const movieId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadMovieDetails(movieId);
    this.loadCinemas();
  }

  loadMovieDetails(id: number): void {
    this.apiService.getMovieById(id).subscribe({
      next: (data) => {
        this.movie = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  loadCinemas(): void {
    this.apiService.getCinemas().subscribe({
      next: (data) => {
        this.cinemas = data;
      },
      error: () => {},
    });
  }

  onCinemaChange(): void {
    if (!this.selectedCinemaId || !this.movie) {
      this.availableDates = [];
      this.filteredShowtimes = [];
      return;
    }

    this.loadShowtimesForCinema();
  }

  loadShowtimesForCinema(): void {
    if (!this.movie || !this.selectedCinemaId) return;

    this.loadingShowtimes = true;

    setTimeout(() => {
      this.apiService
        .getShowtimesByCinemaAndMovie(this.selectedCinemaId!, this.movie.id)
        .subscribe({
          next: (showtimes) => {
            this.allShowtimes = showtimes;

            this.generateAvailableDatesFromShowtimes();

            if (this.availableDates.length > 0) {
              this.selectedDate = this.availableDates[0].value;
              this.filterShowtimesByDate();
            } else {
              this.filteredShowtimes = [];
            }

            this.loadingShowtimes = false;
          },
          error: () => {
            this.allShowtimes = [];
            this.availableDates = [];
            this.filteredShowtimes = [];
            this.loadingShowtimes = false;
          },
        });
    }, 1500);
  }

  generateAvailableDatesFromShowtimes(): void {
    if (this.allShowtimes.length === 0) {
      this.availableDates = [];
      return;
    }

    const today = new Date();

    const uniqueDates = new Set<string>();

    this.allShowtimes.forEach((showtime) => {
      const showtimeDate = this.parseArgentinaDate(showtime.showDateTime);
      const todayStr = this.getDateString(new Date());
      const showtimeDateStr = this.getDateString(showtimeDate);

      if (showtimeDateStr >= todayStr) {
        uniqueDates.add(showtimeDateStr);
      }
    });

    const sortedDates = Array.from(uniqueDates).sort();

    this.availableDates = sortedDates.map((dateStr) => {
      const date = this.parseArgentinaDate(dateStr + 'T12:00:00');
      const options: Intl.DateTimeFormatOptions = {
        weekday: 'long',
        timeZone: 'America/Argentina/Buenos_Aires',
      };
      const dayName = date.toLocaleDateString('es-AR', options);
      const monthOptions: Intl.DateTimeFormatOptions = {
        day: '2-digit',
        month: '2-digit',
        timeZone: 'America/Argentina/Buenos_Aires',
      };
      const dayMonth = date.toLocaleDateString('es-AR', monthOptions);

      const label = `${dayName.charAt(0).toUpperCase() + dayName.slice(1)} ${dayMonth}`;

      return { label, value: dateStr };
    });
  }

  onDateChange(): void {
    this.loadingShowtimes = true;
    setTimeout(() => {
      this.filterShowtimesByDate();
      this.loadingShowtimes = false;
    }, 1000);
  }

  filterShowtimesByDate(): void {
    if (!this.selectedDate) {
      this.filteredShowtimes = [];
      return;
    }

    this.filteredShowtimes = this.allShowtimes
      .filter((showtime) => {
        const showtimeDate = this.getDateString(this.parseArgentinaDate(showtime.showDateTime));
        return showtimeDate === this.selectedDate;
      })
      .sort((a, b) => {
        return (
          this.parseArgentinaDate(a.showDateTime).getTime() -
          this.parseArgentinaDate(b.showDateTime).getTime()
        );
      });
  }

  selectShowtime(showtimeId: number): void {
    this.router.navigate(['/booking', showtimeId]);
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }

  formatTime(dateTime: string): string {
    const date = this.parseArgentinaDate(dateTime);
    return date.toLocaleTimeString('es-AR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
      timeZone: 'America/Argentina/Buenos_Aires',
    });
  }

  getTypeLabel(type: string): string {
    const labels: { [key: string]: string } = {
      SPANISH_2D: '2D Castellano',
      SUBTITLED_2D: '2D Subtitulado',
      SPANISH_3D: '3D Castellano',
    };
    return labels[type] || type;
  }

  goBack(): void {
    this.router.navigate(['/movies']);
  }
}
