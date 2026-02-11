import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { FooterComponent } from './components/footer/footer';
import { NavbarComponent } from './components/navbar/navbar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent, CommonModule],
  template: `
    @if (showNavbar) {
      <app-navbar></app-navbar>
    }
    <main>
      <router-outlet></router-outlet>
    </main>
    @if (showFooter) {
      <app-footer></app-footer>
    }
  `,
  styles: [
    `
      main {
        min-height: calc(100vh - 200px);
      }
    `,
  ],
})
export class AppComponent {
  title = 'CineBook';
  showNavbar = true;
  showFooter = true;

  constructor(private router: Router) {
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        const hideFooterRoutes = ['/login', '/register'];
        this.showFooter = !hideFooterRoutes.includes(event.urlAfterRedirects);
      });
  }
}
