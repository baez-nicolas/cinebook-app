import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FooterComponent } from '../../components/footer/footer';
import { Movie } from '../../models/movie.model';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-movies',
  standalone: true,
  imports: [CommonModule, RouterModule, FooterComponent],
  templateUrl: './movies.html',
  styleUrl: './movies.css',
})
export class MoviesComponent implements OnInit {
  movies: Movie[] = [];
  currentSlideIndex = 0;
  loading = true;
  currentUser: any = null;
  menuOpen = false;

  carouselImages = [
    'https://upload.wikimedia.org/wikipedia/commons/2/2f/Sala_de_cine.jpg',
    'https://cloudfront-us-east-1.images.arcpublishing.com/infobae/JAQSNZX36FHHNFEG3KJTVBB7DY.jpg',
    'https://www.fmdos.cl/wp-content/uploads/2017/06/GettyImages-611759730-e1497662793728.jpg',
  ];

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadMovies();
    this.startCarousel();
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

  startCarousel(): void {
    setInterval(() => {
      this.currentSlideIndex = (this.currentSlideIndex + 1) % this.carouselImages.length;
    }, 4000);
  }

  viewMovieDetails(movieId: number): void {
    this.router.navigate(['/movies', movieId]);
  }

  logout(): void {
    this.authService.logout();
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }
}
