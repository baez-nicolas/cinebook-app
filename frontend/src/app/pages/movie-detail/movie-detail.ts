import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Movie } from '../../models/movie.model';
import { Showtime } from '../../models/showtime.model';

interface ShowtimesByCinema {
  cinemaId: number;
  cinemaName: string;
  showtimes: Showtime[];
}

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './movie-detail.html',
  styleUrl: './movie-detail.css'
})
export class MovieDetailComponent implements OnInit {
  movie: Movie | null = null;
  showtimesByCinema: ShowtimesByCinema[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadMovieDetail(+id);
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
      }
    });
  }

  loadShowtimes(movieId: number): void {
    this.apiService.getShowtimesByMovie(movieId).subscribe({
      next: (showtimes) => {
        this.groupShowtimesByCinema(showtimes);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar las funciones';
        this.loading = false;
        console.error(err);
      }
    });
  }

  groupShowtimesByCinema(showtimes: Showtime[]): void {
    const grouped = new Map<number, ShowtimesByCinema>();

    showtimes.forEach(showtime => {
      if (!grouped.has(showtime.cinemaId)) {
        grouped.set(showtime.cinemaId, {
          cinemaId: showtime.cinemaId,
          cinemaName: showtime.cinemaName,
          showtimes: []
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
      minute: '2-digit'
    };
    return date.toLocaleDateString('es-AR', options);
  }

  getTypeLabel(type: string): string {
    const labels: { [key: string]: string } = {
      'SPANISH_2D': '2D Español',
      'SUBTITLED_2D': '2D Subtitulada',
      'SPANISH_3D': '3D Español'
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
