import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Movie } from '../models/movie.model';
import { Cinema } from '../models/cinema.model';
import { Showtime } from '../models/showtime.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  // MOVIES
  getMovies(): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.apiUrl}/movies`);
  }

  getMovieById(id: number): Observable<Movie> {
    return this.http.get<Movie>(`${this.apiUrl}/movies/${id}`);
  }

  // CINEMAS
  getCinemas(): Observable<Cinema[]> {
    return this.http.get<Cinema[]>(`${this.apiUrl}/cinemas`);
  }

  // SHOWTIMES
  getShowtimes(): Observable<Showtime[]> {
    return this.http.get<Showtime[]>(`${this.apiUrl}/showtimes`);
  }

  getShowtimesByMovie(movieId: number): Observable<Showtime[]> {
    return this.http.get<Showtime[]>(`${this.apiUrl}/showtimes/movie/${movieId}`);
  }

  getShowtimeById(id: number): Observable<Showtime> {
    return this.http.get<Showtime>(`${this.apiUrl}/showtimes/${id}`);
  }
}
