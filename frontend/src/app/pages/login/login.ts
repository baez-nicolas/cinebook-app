import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import Swal from 'sweetalert2';
import { LoginRequest } from '../../models/auth.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  credentials: LoginRequest = {
    email: '',
    password: '',
  };
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/movies']);
    }
  }

  onSubmit(): void {
    if (!this.credentials.email || !this.credentials.password) {
      Swal.fire({
        title: 'Campos Incompletos',
        text: 'Por favor, completa todos los campos',
        icon: 'warning',
        confirmButtonText: 'Entendido',
        confirmButtonColor: '#8B0000',
        background: '#1a1a1a',
        color: '#fff',
      });
      return;
    }

    this.loading = true;

    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        this.loading = false;

        Swal.fire({
          title: '¡Bienvenido!',
          text: `Hola ${response.firstName} ${response.lastName}`,
          icon: 'success',
          confirmButtonText: 'Continuar',
          confirmButtonColor: '#8B0000',
          background: '#1a1a1a',
          color: '#fff',
          timer: 2000,
        });

        this.router.navigate(['/movies']);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error de login:', error);

        Swal.fire({
          title: 'Error de Login',
          text: error.error?.message || 'Email o contraseña incorrectos',
          icon: 'error',
          confirmButtonText: 'Reintentar',
          confirmButtonColor: '#8B0000',
          background: '#1a1a1a',
          color: '#fff',
        });
      },
    });
  }
}
