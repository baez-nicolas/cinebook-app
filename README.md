# 🎬 CineBook

![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

<img src="/img/portada.png" width="700" alt="Banner" />

**CineBook** es una aplicación web full-stack para la reserva de entradas de cine. Explorá películas en cartelera, elegí tu cine favorito, seleccioná asientos y gestioná tus reservas con una interfaz moderna y responsive.

## 🌐 Demo en Vivo

🚀 **[Acceder a la Aplicación](https://natural-curiosity-production-c1bb.up.railway.app/)**

---

## 🎥 Video Marketing

<div>

<a href="https://www.youtube.com/watch?v=62qrqWJDxLY">
  <img src="https://img.youtube.com/vi/62qrqWJDxLY/maxresdefault.jpg" alt="Video Preview" width="500">
</a>

<br><br>

[![Ver video en YouTube](https://img.shields.io/badge/YouTube-Ver_Video-red?style=for-the-badge&logo=youtube)](https://www.youtube.com/watch?v=62qrqWJDxLY)

</div>

> **⚠️ Importante:** Mirá el video antes de usar la aplicación para entender las diferencias entre **Usuario** y **Admin**.

---

### 👥 Tipos de Usuario

| Rol | Funcionalidades |
|-----|-----------------|
| 👤 **Usuario** | Explorar películas, reservar asientos y ver historial de reservas |
| 🔐 **Admin** | Gestión completa de películas, funciones y usuarios |

**Nota:** Para probar la app, registrate con cualquier email (no hace falta que sea real).

---

## ✨ Características

### 🎥 Catálogo de Películas
- Exploración de películas en cartelera
- Información detallada (título, género, duración, sinopsis)
- Búsqueda y filtros por género
- Vista de horarios por película

### 🏢 Selección de Cines
- Múltiples cines disponibles
- Horarios de funciones por cine
- Información de ubicación y salas

### 💺 Sistema de Reservas en Tiempo Real
- Mapa interactivo de asientos con disponibilidad instantánea
- Visualización en tiempo real de disponibilidad
- Notificaciones de éxito/error
- Prevención de double-booking con confirmación atómica

### 👤 Gestión de Usuarios
- Autenticación JWT con tokens de expiración
- Registro, login y roles (USER/ADMIN)
- Historial personal de reservas
- Refresh token strategy

### 🔐 Panel de Administración
- Gestión de películas (CRUD completo)
- Estadísticas de usuarios y reservas
- Gestión de funciones y horarios
- Control de ocupación por sala

### 📱 Diseño & Seguridad
- Interfaz moderna y 100% responsive
- Animaciones suaves y UX optimizada
- Validación de roles en backend
- Protección CSRF (API stateless)

---

## 🚀 Tecnologías

<table>
<tr>
<td width="33%" valign="top">

### Backend
| Tecnología | Versión |
|------------|---------|
| **Java** | 17+ |
| **Spring Boot** | 3.4+ |
| **Spring Security** | 6+ |
| **PostgreSQL** | 17+ |
| **JWT** | 0.12+ |
| **Lombok** | Latest |

</td>
<td width="33%" valign="top">

### Frontend
| Tecnología | Versión |
|------------|---------|
| **Angular** | 19+ |
| **TypeScript** | 5.6+ |
| **RxJS** | 7.8+ |
| **Bootstrap** | 5.3 |
| **Nginx** | Alpine |

</td>
<td width="33%" valign="top">

### DevOps
| Tecnología | Uso |
|------------|-----|
| **Docker** | Containerización |
| **Railway** | Deployment |

</td>
</tr>
</table>

---

## 📝 API Endpoints

### Autenticación
- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/login` - Inicio de sesión

### Películas
- `GET /api/movies` - Listar películas
- `GET /api/movies/{id}` - Obtener película
- `POST /api/admin/movies` - Crear película (ADMIN)
- `PUT /api/admin/movies/{id}` - Actualizar película (ADMIN)
- `DELETE /api/admin/movies/{id}` - Eliminar película (ADMIN)

### Cines
- `GET /api/cinemas` - Listar cines

### Funciones
- `GET /api/showtimes` - Listar horarios
- `GET /api/showtimes/movie/{id}` - Horarios por película

### Reservas
- `POST /api/bookings` - Crear reserva
- `GET /api/bookings/my-bookings` - Mis reservas
- `GET /api/bookings` - Todas las reservas (ADMIN)

### Asientos
- `GET /api/seats/showtime/{id}` - Asientos por función
- `GET /api/seats/showtime/{id}/available` - Asientos disponibles

---

## ⚡ Built With

- **Framework Backend:** [Spring Boot](https://spring.io/projects/spring-boot)
- **Framework Frontend:** [Angular](https://angular.io)
- **Base de Datos:** [PostgreSQL](https://www.postgresql.org)
- **Hosting:** [Railway](https://railway.app)

---

## 👨‍💻 Autor

**Nicolás Baez**

- GitHub: [@baez-nicolas](https://github.com/baez-nicolas)
- LinkedIn: [linkedin.com/in/baez-nicolas](https://www.linkedin.com/in/baez-nicolas/)
- Proyecto: [CineBook](https://github.com/baez-nicolas/cinebook-app)
- Demo: [natural-curiosity-production-c1bb.up.railway.app](https://natural-curiosity-production-c1bb.up.railway.app/)

---

<div align="center">

**[⬆ Volver arriba](#-cinebook)**

Hecho con ❤️ y 🎬 para los amantes del cine

</div>
