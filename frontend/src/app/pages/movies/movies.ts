import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Movie } from '../../models/movie.model';

@Component({
  selector: 'app-movies',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './movies.html',
  styleUrl: './movies.css'
})
export class MoviesComponent implements OnInit {
  movies: Movie[] = [];
  loading = true;
  error: string | null = null;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadMovies();
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
      }
    });
  }

  getRatingColor(rating: string): string {
    const colors: { [key: string]: string } = {
      'G': '#4CAF50',
      'PG': '#2196F3',
      'PG_13': '#FF9800',
      'R': '#F44336',
      'NC_17': '#9C27B0'
    };
    return colors[rating] || '#999';
  }
}
