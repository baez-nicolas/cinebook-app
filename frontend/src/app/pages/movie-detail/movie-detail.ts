import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Cinema } from '../../models/cinema.model';
import { Movie } from '../../models/movie.model';
import { Showtime } from '../../models/showtime.model';
import { ApiService } from '../../services/api.service';

interface ShowtimesByCinema {
  cinemaId: number;
  cinemaName: string;
  showtimes: Showtime[];
}

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './movie-detail.html',
  styleUrl: './movie-detail.css',
})
export class MovieDetailComponent implements OnInit {
  movie: Movie | null = null;
  allShowtimes: Showtime[] = [];
  showtimesByCinema: ShowtimesByCinema[] = [];
  cinemas: Cinema[] = [];
  selectedCinemaId: number = 0;
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadMovieDetail(+id);
      this.loadCinemas();
    }
  }

  loadMovieDetail(id: number): void {
    this.apiService.getMovieById(id).subscribe({
      next: (movie) => {
        this.movie = movie;
        this.loadShowtimes(id);
      },
      error: (err) => {
        this.error = 'Error al cargar la película';
        this.loading = false;
        console.error(err);
      },
    });
  }

  loadShowtimes(movieId: number): void {
    this.apiService.getShowtimesByMovie(movieId).subscribe({
      next: (showtimes) => {
        this.allShowtimes = showtimes;
        this.filterShowtimes();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar las funciones';
        this.loading = false;
        console.error(err);
      },
    });
  }

  loadCinemas(): void {
    this.apiService.getCinemas().subscribe({
      next: (cinemas) => {
        this.cinemas = cinemas;
      },
      error: (err) => {
        console.error('Error al cargar cines:', err);
      },
    });
  }

  filterShowtimes(): void {
    let filteredShowtimes = this.allShowtimes;

    if (this.selectedCinemaId !== 0) {
      filteredShowtimes = this.allShowtimes.filter((st) => st.cinemaId === this.selectedCinemaId);
    }

    this.groupShowtimesByCinema(filteredShowtimes);
  }

  onCinemaChange(): void {
    this.filterShowtimes();
  }

  groupShowtimesByCinema(showtimes: Showtime[]): void {
    const grouped = new Map<number, ShowtimesByCinema>();

    showtimes.forEach((showtime) => {
      if (!grouped.has(showtime.cinemaId)) {
        grouped.set(showtime.cinemaId, {
          cinemaId: showtime.cinemaId,
          cinemaName: showtime.cinemaName,
          showtimes: [],
        });
      }
      grouped.get(showtime.cinemaId)!.showtimes.push(showtime);
    });

    this.showtimesByCinema = Array.from(grouped.values());
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

  getAvailabilityClass(availableSeats: number, totalSeats: number): string {
    const percentage = (availableSeats / totalSeats) * 100;
    if (percentage > 50) return 'good';
    if (percentage > 20) return 'medium';
    return 'low';
  }

  goBack(): void {
    this.router.navigate(['/movies']);
  }

  openTrailer(): void {
    if (this.movie?.trailerUrl) {
      window.open(this.movie.trailerUrl, '_blank');
    }
  }
}
