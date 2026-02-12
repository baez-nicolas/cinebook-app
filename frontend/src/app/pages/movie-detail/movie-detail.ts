import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Cinema } from '../../models/cinema.model';
import { Movie } from '../../models/movie.model';
import { Showtime } from '../../models/showtime.model';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './movie-detail.html',
  styleUrl: './movie-detail.css',
})
export class MovieDetailComponent implements OnInit {
  movie: Movie | null = null;
  cinemas: Cinema[] = [];
  availableDates: { label: string; value: string }[] = [];
  allShowtimes: Showtime[] = [];
  filteredShowtimes: Showtime[] = [];

  selectedCinemaId: number | null = null;
  selectedDate: string = '';

  loading = true;
  loadingShowtimes = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
  ) {}

  ngOnInit(): void {
    const movieId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadMovieDetails(movieId);
    this.loadCinemas();
    this.loadAllShowtimes(movieId);
    this.generateAvailableDates();
  }

  loadMovieDetails(id: number): void {
    this.apiService.getMovieById(id).subscribe({
      next: (data) => {
        this.movie = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar película:', error);
        this.loading = false;
      },
    });
  }

  loadCinemas(): void {
    this.apiService.getCinemas().subscribe({
      next: (data) => {
        this.cinemas = data;
      },
      error: (error) => {
        console.error('Error al cargar cines:', error);
      },
    });
  }

  loadAllShowtimes(movieId: number): void {
    this.apiService.getShowtimesByMovie(movieId).subscribe({
      next: (data) => {
        this.allShowtimes = data;
      },
      error: (error) => {
        console.error('Error al cargar funciones:', error);
      },
    });
  }

  generateAvailableDates(): void {
    const today = new Date();

    for (let i = 0; i < 7; i++) {
      const date = new Date(today);
      date.setDate(today.getDate() + i);

      const dayName = date.toLocaleDateString('es-AR', { weekday: 'long' });
      const dayNumber = date.getDate();
      const month = date.toLocaleDateString('es-AR', { month: '2-digit' });

      const label = `${dayName.charAt(0).toUpperCase() + dayName.slice(1)} ${dayNumber}/${month}`;
      const value = date.toISOString().split('T')[0];

      this.availableDates.push({ label, value });
    }

    this.selectedDate = this.availableDates[0].value;
  }

  onCinemaOrDateChange(): void {
    if (this.selectedCinemaId && this.selectedDate) {
      this.filterShowtimes();
    } else {
      this.filteredShowtimes = [];
    }
  }

  filterShowtimes(): void {
    this.loadingShowtimes = true;

    setTimeout(() => {
      this.filteredShowtimes = this.allShowtimes
        .filter((st) => {
          const showtimeDate = new Date(st.showDateTime).toISOString().split('T')[0];
          return st.cinemaId === this.selectedCinemaId && showtimeDate === this.selectedDate;
        })
        .sort((a, b) => new Date(a.showDateTime).getTime() - new Date(b.showDateTime).getTime());

      this.loadingShowtimes = false;
    }, 300);
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
    return new Date(dateTime).toLocaleTimeString('es-AR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  getTypeLabel(type: string): string {
    const labels: { [key: string]: string } = {
      SPANISH_2D: '2D Español',
      SUBTITLED_2D: '2D Subtitulado',
      SPANISH_3D: '3D Español',
    };
    return labels[type] || type;
  }

  goBack(): void {
    this.router.navigate(['/movies']);
  }
}
