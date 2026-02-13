import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
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
    canActivate: [authGuard],
  },
  {
    path: 'movies/:id',
    loadComponent: () =>
      import('./pages/movie-detail/movie-detail').then((m) => m.MovieDetailComponent),
    canActivate: [authGuard],
  },
  {
    path: 'booking/:showtimeId',
    loadComponent: () => import('./pages/booking/booking').then((m) => m.BookingComponent),
    canActivate: [authGuard],
  },
  {
    path: 'my-bookings',
    loadComponent: () =>
      import('./pages/my-bookings/my-bookings').then((m) => m.MyBookingsComponent),
    canActivate: [authGuard],
  },
  {
    path: 'users',
    loadComponent: () => import('./pages/users/users').then((m) => m.UsersComponent),
    canActivate: [authGuard],
  },
  {
    path: 'admin/movies',
    loadComponent: () =>
      import('./pages/admin-movies/admin-movies').then((m) => m.AdminMoviesComponent),
    canActivate: [authGuard],
  },
  {
    path: '**',
    redirectTo: '/login',
  },
];
