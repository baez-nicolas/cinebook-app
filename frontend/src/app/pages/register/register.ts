import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import Swal from 'sweetalert2';
import { RegisterRequest } from '../../models/auth.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {
  userData: RegisterRequest = {
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
  };
  confirmPassword = '';
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
    if (
      !this.userData.email ||
      !this.userData.password ||
      !this.userData.firstName ||
      !this.userData.lastName
    ) {
      Swal.fire({
        title: 'Campos Incompletos',
        text: 'Por favor, completa todos los campos obligatorios',
        icon: 'warning',
        confirmButtonText: 'Entendido',
        confirmButtonColor: '#8B0000',
        background: '#1a1a1a',
        color: '#fff',
      });
      return;
    }

    if (this.userData.password !== this.confirmPassword) {
      Swal.fire({
        title: 'Contraseñas no coinciden',
        text: 'Las contraseñas deben ser iguales',
        icon: 'error',
        confirmButtonText: 'Entendido',
        confirmButtonColor: '#8B0000',
        background: '#1a1a1a',
        color: '#fff',
      });
      return;
    }

    if (this.userData.password.length < 6) {
      Swal.fire({
        title: 'Contraseña muy corta',
        text: 'La contraseña debe tener al menos 6 caracteres',
        icon: 'error',
        confirmButtonText: 'Entendido',
        confirmButtonColor: '#8B0000',
        background: '#1a1a1a',
        color: '#fff',
      });
      return;
    }

    this.loading = true;

    this.authService.register(this.userData).subscribe({
      next: (response) => {
        this.loading = false;

        Swal.fire({
          title: '¡Registro Exitoso!',
          text: `Bienvenido ${response.firstName} ${response.lastName}`,
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
        console.error('Error de registro:', error);

        let errorMessage = 'Ocurrió un error al registrar';
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.status === 400) {
          errorMessage = 'El email ya está registrado';
        }

        Swal.fire({
          title: 'Error de Registro',
          text: errorMessage,
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
