import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewEncapsulation } from '@angular/core';
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
  encapsulation: ViewEncapsulation.None,
})
export class AdminMoviesComponent implements OnInit {
  currentUser: any = null;
  movies: any[] = [];
  filteredMovies: any[] = [];
  searchTerm = '';
  loading = true;
  menuOpen = false;

  activeMoviesCount = 0;
  maxMovies = 12;
  orphanShowtimesCount = 0;
  moviesWithShowtimes: Set<number> = new Set();

  showForm = false;
  isEditing = false;
  private savedScrollPos = 0;
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
    this.loadShowtimesMap();
  }

  loadShowtimesMap(): void {
    this.apiService.getShowtimes().subscribe({
      next: (showtimes) => {
        this.moviesWithShowtimes = new Set(
          showtimes.filter((s) => s.movieId).map((s) => s.movieId),
        );
      },
    });
  }

  hasShowtimes(movieId: number): boolean {
    return this.moviesWithShowtimes.has(movieId);
  }

  loadMovies(): void {
    this.apiService.getMovies().subscribe({
      next: (data) => {
        this.movies = data;
        this.filteredMovies = [...this.movies];
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar películas:', error);
        this.loading = false;
      },
    });
  }

  filterMovies(): void {
    const term = this.searchTerm.toLowerCase().trim();
    if (!term) {
      this.filteredMovies = [...this.movies];
      return;
    }
    this.filteredMovies = this.movies.filter(
      (movie) =>
        movie.title?.toLowerCase().includes(term) ||
        movie.genre?.toLowerCase().includes(term) ||
        movie.rating?.toLowerCase().includes(term) ||
        movie.releaseDate?.includes(term),
    );
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
        scrollbarPadding: false,
        heightAuto: false,
        returnFocus: false,
      });
      return;
    }

    this.isEditing = false;
    this.resetForm();
    this.savedScrollPos = window.scrollY;
    document.body.style.overflow = 'hidden';
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
    this.savedScrollPos = window.scrollY;
    document.body.style.overflow = 'hidden';
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    document.body.style.overflow = '';
    window.scrollTo(0, this.savedScrollPos);
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
    const scrollPos = window.scrollY;
    this.apiService.createMovie(this.movieForm).subscribe({
      next: (response) => {
        Swal.fire({
          icon: 'success',
          title: 'Película creada',
          text: response.message,
          confirmButtonColor: '#d4af37',
          scrollbarPadding: false,
          heightAuto: false,
          returnFocus: false,
        }).then(() => {
          window.scrollTo(0, scrollPos);
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
          scrollbarPadding: false,
          heightAuto: false,
          returnFocus: false,
        });
      },
    });
  }

  updateMovie(): void {
    const scrollPos = window.scrollY;
    this.apiService.updateMovie(this.movieForm.id, this.movieForm).subscribe({
      next: (response) => {
        Swal.fire({
          icon: 'success',
          title: 'Película actualizada',
          text: response.message,
          confirmButtonColor: '#d4af37',
          scrollbarPadding: false,
          heightAuto: false,
          returnFocus: false,
        }).then(() => {
          window.scrollTo(0, scrollPos);
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
          scrollbarPadding: false,
          heightAuto: false,
          returnFocus: false,
        });
      },
    });
  }

  deleteMovie(movie: any, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    (event.target as HTMLElement)?.blur();

    const scrollPos = window.scrollY;
    document.body.style.top = `-${scrollPos}px`;

    Swal.fire({
      title: '¿Eliminar película?',
      text: `Se eliminará "${movie.title}". Las funciones quedarán disponibles para reasignar.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d4af37',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      scrollbarPadding: false,
      heightAuto: false,
      returnFocus: false,
      focusConfirm: false,
      focusCancel: false,
      allowOutsideClick: false,
      didClose: () => {
        document.body.style.top = '';
        window.scrollTo(0, scrollPos);
      },
    }).then((result) => {
      if (result.isConfirmed) {
        document.body.style.top = `-${scrollPos}px`;
        this.apiService.deleteMovie(movie.id).subscribe({
          next: (response) => {
            Swal.fire({
              icon: 'success',
              title: 'Película eliminada',
              text: `${response.orphanShowtimes} funciones disponibles para reasignar`,
              confirmButtonColor: '#d4af37',
              scrollbarPadding: false,
              heightAuto: false,
              returnFocus: false,
              focusConfirm: false,
              didClose: () => {
                document.body.style.top = '';
                window.scrollTo(0, scrollPos);
              },
            });
            this.loadData();
          },
          error: () => {
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: 'No se pudo eliminar la película',
              confirmButtonColor: '#d4af37',
              scrollbarPadding: false,
              heightAuto: false,
              returnFocus: false,
              focusConfirm: false,
              didClose: () => {
                document.body.style.top = '';
                window.scrollTo(0, scrollPos);
              },
            });
          },
        });
      }
    });
  }

  askToReassignShowtimes(movie: any): void {
    const scrollPos = window.scrollY;
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
      scrollbarPadding: false,
      heightAuto: false,
      returnFocus: false,
    }).then((result) => {
      window.scrollTo(0, scrollPos);
      if (result.isConfirmed) {
        this.reassignShowtimes(movie.id, movie.title);
      }
    });
  }

  reassignShowtimes(movieId: number, movieTitle: string): void {
    const scrollPos = window.scrollY;
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
          scrollbarPadding: false,
          heightAuto: false,
          returnFocus: false,
        }).then(() => {
          window.scrollTo(0, scrollPos);
        });
        this.loadData();
      },
      error: (error) => {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: error.error.error || 'No se pudieron reasignar las funciones',
          confirmButtonColor: '#d4af37',
          scrollbarPadding: false,
          heightAuto: false,
          returnFocus: false,
        }).then(() => {
          window.scrollTo(0, scrollPos);
        });
      },
    });
  }

  openAssignShowtimes(movie: any, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    (event.target as HTMLElement)?.blur();

    const scrollPos = window.scrollY;
    Swal.fire({
      title: `Asignar funciones a "${movie.title}"`,
      html: `
        <p>Hay <strong>${this.orphanShowtimesCount}</strong> funciones disponibles.</p>
        <p>¿Deseas asignarlas a esta película?</p>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#d4af37',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, asignar',
      cancelButtonText: 'Cancelar',
      scrollbarPadding: false,
      heightAuto: false,
      returnFocus: false,
    }).then((result) => {
      window.scrollTo(0, scrollPos);
      if (result.isConfirmed) {
        this.reassignShowtimes(movie.id, movie.title);
      }
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
