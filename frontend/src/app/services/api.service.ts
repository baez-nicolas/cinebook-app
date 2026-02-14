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
  private apiUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8080/api'
      : 'https://cinebook-app-production.up.railway.app/api';

  constructor(private http: HttpClient) {}

  getMovies(): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.apiUrl}/movies`);
  }

  getMovieById(id: number): Observable<Movie> {
    return this.http.get<Movie>(`${this.apiUrl}/movies/${id}`);
  }

  getCinemas(): Observable<Cinema[]> {
    return this.http.get<Cinema[]>(`${this.apiUrl}/cinemas`);
  }

  getShowtimes(): Observable<Showtime[]> {
    return this.http.get<Showtime[]>(`${this.apiUrl}/showtimes`);
  }

  getShowtimesByMovie(movieId: number): Observable<Showtime[]> {
    return this.http.get<Showtime[]>(`${this.apiUrl}/showtimes/movie/${movieId}`);
  }

  getShowtimeById(id: number): Observable<Showtime> {
    return this.http.get<Showtime>(`${this.apiUrl}/showtimes/${id}`);
  }

  getSeatsByShowtime(showtimeId: number): Observable<Seat[]> {
    return this.http.get<Seat[]>(`${this.apiUrl}/seats/showtime/${showtimeId}`);
  }

  getAvailableSeats(showtimeId: number): Observable<Seat[]> {
    return this.http.get<Seat[]>(`${this.apiUrl}/seats/showtime/${showtimeId}/available`);
  }

  createBooking(booking: BookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${this.apiUrl}/bookings`, booking);
  }

  getMyBookings(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.apiUrl}/bookings/my-bookings`);
  }

  getAllBookings(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.apiUrl}/bookings`);
  }

  getBookingsByUser(userEmail: string): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.apiUrl}/bookings/user/${userEmail}`);
  }

  getShowtimesByFilters(movieId: number, cinemaId: number, date: string): Observable<Showtime[]> {
    const url = `${this.apiUrl}/showtimes/filter?movieId=${movieId}&cinemaId=${cinemaId}&date=${date}`;
    return this.http.get<Showtime[]>(url);
  }

  getShowtimesByCinemaAndMovie(cinemaId: number, movieId: number): Observable<any[]> {
    const url = `${this.apiUrl}/showtimes/cinema/${cinemaId}/movie/${movieId}`;
    return this.http.get<any[]>(url);
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/admin/users`);
  }

  getUsersCount(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/admin/users/count`);
  }

  getActiveMoviesCount(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/admin/movies/count`);
  }

  createMovie(movieData: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/admin/movies`, movieData);
  }

  updateMovie(id: number, movieData: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/admin/movies/${id}`, movieData);
  }

  deleteMovie(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/admin/movies/${id}`);
  }

  getOrphanShowtimesCount(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/admin/movies/orphan-showtimes/count`);
  }

  reassignShowtimes(movieId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/admin/movies/showtimes/reassign`, { movieId });
  }
}
