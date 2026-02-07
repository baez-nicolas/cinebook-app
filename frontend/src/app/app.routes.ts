import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/movies',
    pathMatch: 'full'
  },
  {
    path: 'movies',
    loadComponent: () => import('./pages/movies/movies').then(m => m.MoviesComponent)
  },
  {
    path: 'movies/:id',
    loadComponent: () => import('./pages/movie-detail/movie-detail').then(m => m.MovieDetailComponent)
  },
  {
    path: '**',
    redirectTo: '/movies'
  }
];
