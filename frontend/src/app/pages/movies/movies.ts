import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Movie } from '../../models/movie.model';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-movies',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './movies.html',
  styleUrl: './movies.css',
})
export class MoviesComponent implements OnInit, OnDestroy {
  movies: Movie[] = [];
  loading = true;
  error: string | null = null;
  currentSlide = 0;
  carouselInterval: any;

  carouselImages = [
    {
      url: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1600&h=600&fit=crop',
      title: '¡Bienvenido a CineBook!',
      subtitle: 'Las mejores películas te esperan',
    },
    {
      url: 'https://images.unsplash.com/photo-1542204165-65bf26472b9b?w=1600&h=600&fit=crop',
      title: 'Experiencia Cinematográfica',
      subtitle: 'Reserva tus entradas en línea',
    },
    {
      url: 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1600&h=600&fit=crop',
      title: 'Estrenos de la Semana',
      subtitle: 'No te pierdas las mejores funciones',
    },
  ];

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadMovies();
    this.startCarousel();
  }

  ngOnDestroy(): void {
    if (this.carouselInterval) {
      clearInterval(this.carouselInterval);
    }
  }

  startCarousel(): void {
    this.carouselInterval = setInterval(() => {
      this.nextSlide();
    }, 5000);
  }

  nextSlide(): void {
    this.currentSlide = (this.currentSlide + 1) % this.carouselImages.length;
  }

  prevSlide(): void {
    this.currentSlide =
      this.currentSlide === 0 ? this.carouselImages.length - 1 : this.currentSlide - 1;
  }

  goToSlide(index: number): void {
    this.currentSlide = index;
  }

  loadMovies(): void {
    this.apiService.getMovies().subscribe({
      next: (data) => {
        this.movies = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar las películas';
        this.loading = false;
        console.error(err);
      },
    });
  }

  getRatingColor(rating: string): string {
    const colors: { [key: string]: string } = {
      G: '#4CAF50',
      PG: '#2196F3',
      PG_13: '#FF9800',
      R: '#F44336',
      NC_17: '#9C27B0',
    };
    return colors[rating] || '#999';
  }
}
