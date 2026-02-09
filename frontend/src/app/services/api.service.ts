import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BookingRequest, BookingResponse } from '../models/booking.model';
import { Cinema } from '../models/cinema.model';
import { Movie } from '../models/movie.model';
import { Seat } from '../models/seat.model';
import { Showtime } from '../models/showtime.model';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

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

  // SEATS
  getSeatsByShowtime(showtimeId: number): Observable<Seat[]> {
    return this.http.get<Seat[]>(`${this.apiUrl}/seats/showtime/${showtimeId}`);
  }

  getAvailableSeats(showtimeId: number): Observable<Seat[]> {
    return this.http.get<Seat[]>(`${this.apiUrl}/seats/showtime/${showtimeId}/available`);
  }

  // BOOKINGS
  createBooking(userName: string, booking: BookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${this.apiUrl}/bookings?userName=${userName}`, booking);
  }

  getMyBookings(userName: string): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.apiUrl}/bookings/user/${userName}`);
  }
}
