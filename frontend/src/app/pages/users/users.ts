import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { FooterComponent } from '../../components/footer/footer';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, RouterModule, FooterComponent, FormsModule],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  searchTerm = '';
  userStats: any = null;
  loading = true;
  currentUser: any = null;
  menuOpen = false;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    if (!this.authService.isAdmin()) {
      this.router.navigate(['/movies']);
      return;
    }

    this.loadUsers();
    this.loadUsersCount();
  }

  loadUsers(): void {
    this.apiService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data.sort((a: User, b: User) => {
          if (a.role === 'ADMIN' && b.role !== 'ADMIN') return -1;
          if (a.role !== 'ADMIN' && b.role === 'ADMIN') return 1;
          return a.id - b.id;
        });
        this.filteredUsers = [...this.users];
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar usuarios:', error);
        this.loading = false;
      },
    });
  }

  filterUsers(): void {
    const term = this.searchTerm.toLowerCase().trim();
    if (!term) {
      this.filteredUsers = [...this.users];
      return;
    }
    this.filteredUsers = this.users.filter(
      (user) =>
        user.firstName.toLowerCase().includes(term) ||
        user.lastName.toLowerCase().includes(term) ||
        user.email.toLowerCase().includes(term) ||
        this.getRoleLabel(user.role).toLowerCase().includes(term),
    );
  }

  loadUsersCount(): void {
    this.apiService.getUsersCount().subscribe({
      next: (data) => {
        this.userStats = data;
      },
      error: (error) => {
        console.error('Error al cargar estadísticas:', error);
      },
    });
  }

  getRoleBadgeClass(role: string): string {
    return role === 'ADMIN' ? 'badge-admin' : 'badge-user';
  }

  getRoleLabel(role: string): string {
    return role === 'ADMIN' ? 'Administrador' : 'Usuario';
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  logout(): void {
    this.authService.logout();
  }
}
