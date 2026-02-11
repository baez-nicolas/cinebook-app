import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/movies',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then((m) => m.RegisterComponent),
  },
  {
    path: 'movies',
    loadComponent: () => import('./pages/movies/movies').then((m) => m.MoviesComponent),
  },
  {
    path: 'movies/:id',
    loadComponent: () =>
      import('./pages/movie-detail/movie-detail').then((m) => m.MovieDetailComponent),
  },
  {
    path: 'booking/:showtimeId',
    loadComponent: () => import('./pages/booking/booking').then((m) => m.BookingComponent),
  },
  {
    path: 'my-bookings',
    loadComponent: () =>
      import('./pages/my-bookings/my-bookings').then((m) => m.MyBookingsComponent),
  },
  {
    path: '**',
    redirectTo: '/movies',
  },
];
