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
        console.log('Pelicula cargada:', this.movie);
      },
      error: (error) => {
        console.error('Error al cargar pelicula:', error);
        this.loading = false;
      },
    });
  }

  loadCinemas(): void {
    this.apiService.getCinemas().subscribe({
      next: (data) => {
        this.cinemas = data;
        console.log('Cines cargados:', this.cinemas.length);
      },
      error: (error) => {
        console.error('Error al cargar cines:', error);
      },
    });
  }

  onCinemaChange(): void {
    console.log('Cambio de cine detectado:', this.selectedCinemaId);

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
    console.log('Cargando funciones para cine:', this.selectedCinemaId, 'pelicula:', this.movie.id);

    setTimeout(() => {
      this.apiService
        .getShowtimesByCinemaAndMovie(this.selectedCinemaId!, this.movie.id)
        .subscribe({
          next: (showtimes) => {
            this.allShowtimes = showtimes;
            console.log('Funciones cargadas:', showtimes.length);

            this.generateAvailableDatesFromShowtimes();

            if (this.availableDates.length > 0) {
              this.selectedDate = this.availableDates[0].value;
              this.filterShowtimesByDate();
            } else {
              this.filteredShowtimes = [];
            }

            this.loadingShowtimes = false;
          },
          error: (error) => {
            console.error('Error al cargar funciones:', error);
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

    const uniqueDates = new Set<string>();
    this.allShowtimes.forEach((showtime) => {
      const date = new Date(showtime.showDateTime).toISOString().split('T')[0];
      uniqueDates.add(date);
    });

    const sortedDates = Array.from(uniqueDates).sort();

    this.availableDates = sortedDates.map((dateStr) => {
      const date = new Date(dateStr + 'T12:00:00');
      const dayName = date.toLocaleDateString('es-AR', { weekday: 'long' });
      const dayNumber = date.getDate();
      const month = date.toLocaleDateString('es-AR', { month: '2-digit' });

      const label = `${dayName.charAt(0).toUpperCase() + dayName.slice(1)} ${dayNumber}/${month}`;

      return { label, value: dateStr };
    });

    console.log('Fechas disponibles:', this.availableDates);
  }

  onDateChange(): void {
    console.log('Cambio de fecha detectado:', this.selectedDate);
    this.filterShowtimesByDate();
  }

  filterShowtimesByDate(): void {
    if (!this.selectedDate) {
      this.filteredShowtimes = [];
      return;
    }

    console.log('Filtrando funciones para fecha:', this.selectedDate);

    this.filteredShowtimes = this.allShowtimes
      .filter((showtime) => {
        const showtimeDate = new Date(showtime.showDateTime).toISOString().split('T')[0];
        return showtimeDate === this.selectedDate;
      })
      .sort((a, b) => {
        return new Date(a.showDateTime).getTime() - new Date(b.showDateTime).getTime();
      });

    console.log('Funciones filtradas:', this.filteredShowtimes.length);
  }

  selectShowtime(showtimeId: number): void {
    console.log('Seleccionando funcion:', showtimeId);
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
      hour12: false,
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
