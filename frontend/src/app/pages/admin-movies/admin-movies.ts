import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import Swal from 'sweetalert2';
import { FooterComponent } from '../../components/footer/footer';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-movies',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, FooterComponent],
  templateUrl: './admin-movies.html',
  styleUrls: ['./admin-movies.css'],
})
export class AdminMoviesComponent implements OnInit {
  currentUser: any = null;
  movies: any[] = [];
  loading = true;
  menuOpen = false;

  activeMoviesCount = 0;
  maxMovies = 12;
  orphanShowtimesCount = 0;

  showForm = false;
  isEditing = false;
  movieForm: any = {
    id: null,
    title: '',
    description: '',
    duration: null,
    genre: '',
    rating: '',
    posterUrl: '',
    trailerUrl: '',
    releaseDate: '',
  };

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    if (!this.authService.isAdmin()) {
      this.router.navigate(['/movies']);
      return;
    }

    this.loadData();
  }

  loadData(): void {
    this.loadMovies();
    this.loadCounts();
  }

  loadMovies(): void {
    this.apiService.getMovies().subscribe({
      next: (data) => {
        this.movies = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar películas:', error);
        this.loading = false;
      },
    });
  }

  loadCounts(): void {
    this.apiService.getActiveMoviesCount().subscribe({
      next: (data) => {
        this.activeMoviesCount = data.activeMovies;
        this.maxMovies = data.maxMovies;
      },
    });

    this.apiService.getOrphanShowtimesCount().subscribe({
      next: (data) => {
        this.orphanShowtimesCount = data.orphanShowtimes;
      },
    });
  }

  openAddForm(): void {
    if (this.activeMoviesCount >= this.maxMovies) {
      Swal.fire({
        icon: 'warning',
        title: 'Límite alcanzado',
        text: `Ya hay ${this.maxMovies} películas activas. Elimina una antes de agregar otra.`,
        confirmButtonColor: '#d4af37',
      });
      return;
    }

    this.isEditing = false;
    this.resetForm();
    this.showForm = true;
  }

  openEditForm(movie: any): void {
    this.isEditing = true;
    this.movieForm = {
      id: movie.id,
      title: movie.title,
      description: movie.description,
      duration: movie.duration,
      genre: movie.genre,
      rating: movie.rating,
      posterUrl: movie.posterUrl,
      trailerUrl: movie.trailerUrl,
      releaseDate: movie.releaseDate,
    };
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.resetForm();
  }

  resetForm(): void {
    this.movieForm = {
      id: null,
      title: '',
      description: '',
      duration: null,
      genre: '',
      rating: '',
      posterUrl: '',
      trailerUrl: '',
      releaseDate: '',
    };
  }

  saveMovie(): void {
    if (this.isEditing) {
      this.updateMovie();
    } else {
      this.createMovie();
    }
  }

  createMovie(): void {
    this.apiService.createMovie(this.movieForm).subscribe({
      next: (response) => {
        Swal.fire({
          icon: 'success',
          title: 'Película creada',
          text: response.message,
          confirmButtonColor: '#d4af37',
        }).then(() => {
          if (response.orphanShowtimesAvailable > 0) {
            this.askToReassignShowtimes(response.movie);
          }
          this.closeForm();
          this.loadData();
        });
      },
      error: (error) => {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: error.error.message || 'No se pudo crear la película',
          confirmButtonColor: '#d4af37',
        });
      },
    });
  }

  updateMovie(): void {
    this.apiService.updateMovie(this.movieForm.id, this.movieForm).subscribe({
      next: (response) => {
        Swal.fire({
          icon: 'success',
          title: 'Película actualizada',
          text: response.message,
          confirmButtonColor: '#d4af37',
        });
        this.closeForm();
        this.loadData();
      },
      error: (error) => {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo actualizar la película',
          confirmButtonColor: '#d4af37',
        });
      },
    });
  }

  deleteMovie(movie: any): void {
    Swal.fire({
      title: '¿Eliminar película?',
      text: `Se eliminará "${movie.title}". Las funciones quedarán disponibles para reasignar.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d4af37',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.apiService.deleteMovie(movie.id).subscribe({
          next: (response) => {
            Swal.fire({
              icon: 'success',
              title: 'Película eliminada',
              text: `${response.orphanShowtimes} funciones disponibles para reasignar`,
              confirmButtonColor: '#d4af37',
            });
            this.loadData();
          },
          error: (error) => {
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: 'No se pudo eliminar la película',
              confirmButtonColor: '#d4af37',
            });
          },
        });
      }
    });
  }

  askToReassignShowtimes(movie: any): void {
    Swal.fire({
      title: `Película "${movie.title}" creada`,
      html: `
        <p>Se encontraron <strong>${this.orphanShowtimesCount}</strong> funciones disponibles.</p>
        <p>¿Deseas asignar funciones automáticamente a esta película?</p>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#d4af37',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, asignar',
      cancelButtonText: 'No, después',
    }).then((result) => {
      if (result.isConfirmed) {
        this.reassignShowtimes(movie.id, movie.title);
      }
    });
  }

  reassignShowtimes(movieId: number, movieTitle: string): void {
    this.apiService.reassignShowtimes(movieId).subscribe({
      next: (response) => {
        Swal.fire({
          icon: 'success',
          title: 'Funciones asignadas',
          html: `
            <p><strong>${response.reassignedCount}</strong> funciones asignadas a "${movieTitle}"</p>
            <p>Funciones huérfanas restantes: <strong>${response.remainingOrphanShowtimes}</strong></p>
          `,
          confirmButtonColor: '#d4af37',
        });
        this.loadData();
      },
      error: (error) => {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: error.error.error || 'No se pudieron reasignar las funciones',
          confirmButtonColor: '#d4af37',
        });
      },
    });
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  logout(): void {
    this.authService.logout();
  }
}
