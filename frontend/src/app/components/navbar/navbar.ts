import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs/operators';
import { User } from '../../models/auth.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class NavbarComponent implements OnInit {
  menuOpen = false;
  currentUser: User | null = null;
  isPublicRoute = false;

  constructor(
    public authService: AuthService,
    private router: Router,
  ) {
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.isPublicRoute = event.url === '/login' || event.url === '/register';
      });
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated() && !this.isPublicRoute;
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  logout(): void {
    this.authService.logout();
    this.menuOpen = false;
  }
}
