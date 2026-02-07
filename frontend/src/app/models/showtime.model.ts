export interface Showtime {
  id: number;
  movieId: number;
  movieTitle: string;
  moviePosterUrl: string;
  cinemaId: number;
  cinemaName: string;
  showDateTime: string;
  type: string;
  price: number;
  availableSeats: number;
  totalSeats: number;
}
