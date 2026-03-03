package com.cinebook.backend.config;

import com.cinebook.backend.entities.Cinema;
import com.cinebook.backend.entities.Movie;
import com.cinebook.backend.entities.enums.MovieRating;
import com.cinebook.backend.repositories.CinemaRepository;
import com.cinebook.backend.repositories.MovieRepository;
import com.cinebook.backend.services.interfaces.ISeatService;
import com.cinebook.backend.services.interfaces.IShowtimeService;
import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class DataLoader implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final IWeeklyScheduleService weeklyScheduleService;
    private final IShowtimeService showtimeService;
    private final ISeatService seatService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando carga de datos...");

        // weeklyScheduleService.checkAndResetIfNeeded();

        if (movieRepository.count() == 0) {
            loadMovies();
        } else {
            log.info("Las películas ya están cargadas");
        }

        if (cinemaRepository.count() == 0) {
            loadCinemas();
        } else {
            log.info("Los cines ya están cargados");
        }

        showtimeService.generateShowtimesForCurrentWeek();

        log.info("Verificando y generando asientos faltantes...");
        seatService.generateSeatsForAllShowtimes();

        log.info("Carga de datos completada exitosamente");
    }

    private void loadMovies() {
        log.info("Cargando películas...");

        Movie movie1 = new Movie();
        movie1.setTitle("Avengers: Endgame");
        movie1.setDescription("Después de los eventos devastadores de Infinity War, los Vengadores se reúnen una vez más para deshacer las acciones de Thanos y restaurar el equilibrio del universo.");
        movie1.setDuration(181);
        movie1.setGenre("Acción");
        movie1.setRating(MovieRating.PG_13);
        movie1.setPosterUrl("https://m.media-amazon.com/images/I/91-UCbbhoiL._AC_SL1500_.jpg");
        movie1.setTrailerUrl("https://www.youtube.com/watch?v=TcMBFSGVi1c");
        movie1.setReleaseDate(LocalDate.of(2019, 4, 26));
        movie1.setIsActive(true);

        Movie movie2 = new Movie();
        movie2.setTitle("Transformers: Rise of the Beasts");
        movie2.setDescription("Los Autobots se unen a una nueva facción de Transformers, los Maximals, para enfrentarse a una amenaza terrorífica que podría destruir el planeta.");
        movie2.setDuration(127);
        movie2.setGenre("Acción");
        movie2.setRating(MovieRating.PG_13);
        movie2.setPosterUrl("https://m.media-amazon.com/images/I/913qFhjEciL.jpg");
        movie2.setTrailerUrl("https://www.youtube.com/watch?v=itnqEauWQZM");
        movie2.setReleaseDate(LocalDate.of(2023, 6, 9));
        movie2.setIsActive(true);

        Movie movie3 = new Movie();
        movie3.setTitle("John Wick 4");
        movie3.setDescription("John Wick descubre un camino para derrotar a la Alta Mesa. Pero antes de ganarse la libertad, deberá enfrentarse a un nuevo enemigo con poderosas alianzas.");
        movie3.setDuration(169);
        movie3.setGenre("Acción");
        movie3.setRating(MovieRating.R);
        movie3.setPosterUrl("https://image.tmdb.org/t/p/original/mj2Z9HnRSIEk3n7yVPoOY4Uzzfh.jpg");
        movie3.setTrailerUrl("https://www.youtube.com/watch?v=yjRHZEUamCc");
        movie3.setReleaseDate(LocalDate.of(2023, 3, 24));
        movie3.setIsActive(true);

        Movie movie4 = new Movie();
        movie4.setTitle("My Hero Academia: You're Next");
        movie4.setDescription("Deku y sus amigos de la Clase 1-A se enfrentan a una nueva amenaza que pone en peligro el futuro de los héroes y la sociedad.");
        movie4.setDuration(110);
        movie4.setGenre("Animación");
        movie4.setRating(MovieRating.PG_13);
        movie4.setPosterUrl("https://es.web.img3.acsta.net/img/8a/5a/8a5ac40a94828e11d968c91be9440f2c.jpg");
        movie4.setTrailerUrl("https://www.youtube.com/watch?v=22hBq1cvemE");
        movie4.setReleaseDate(LocalDate.of(2024, 8, 2));
        movie4.setIsActive(true);

        Movie movie5 = new Movie();
        movie5.setTitle("Godzilla x Kong: The New Empire");
        movie5.setDescription("Los dos titanes más poderosos de la historia se unen para enfrentar una amenaza colosal oculta en las profundidades de la Tierra.");
        movie5.setDuration(115);
        movie5.setGenre("Acción");
        movie5.setRating(MovieRating.PG_13);
        movie5.setPosterUrl("https://w0.peakpx.com/wallpaper/719/37/HD-wallpaper-godzilla-vs-kong-boat-boats-china-ships-viking.jpg");
        movie5.setTrailerUrl("https://www.youtube.com/watch?v=qqrpMRDuPfc");
        movie5.setReleaseDate(LocalDate.of(2024, 3, 29));
        movie5.setIsActive(true);

        Movie movie6 = new Movie();
        movie6.setTitle("Deadpool & Wolverine");
        movie6.setDescription("Wade Wilson y Logan se unen en una aventura épica llena de acción, humor irreverente y caos absoluto que cambiará el Universo Cinematográfico de Marvel para siempre.");
        movie6.setDuration(128);
        movie6.setGenre("Acción");
        movie6.setRating(MovieRating.R);
        movie6.setPosterUrl("https://moviecrazyplanet.com/wp-content/uploads/2024/04/Deadpool-Wolverine-poster-04.jpg");
        movie6.setTrailerUrl("https://www.youtube.com/watch?v=UzFZR2dRsSY");
        movie6.setReleaseDate(LocalDate.of(2024, 7, 26));
        movie6.setIsActive(true);

        Movie movie7 = new Movie();
        movie7.setTitle("Pacific Rim 2: Uprising");
        movie7.setDescription("Jake Pentecost, hijo de Stacker Pentecost, se une a un nuevo grupo de pilotos de Jaegers para enfrentar una nueva amenaza Kaiju que pone en riesgo a la humanidad.");
        movie7.setDuration(111);
        movie7.setGenre("Acción");
        movie7.setRating(MovieRating.PG_13);
        movie7.setPosterUrl("https://oyster.ignimgs.com/wordpress/stg.ign.com/2017/09/PRU_Tsr1Sheet8_John_RGB_3.jpg");
        movie7.setTrailerUrl("https://www.youtube.com/watch?v=fUjicxMPDzs");
        movie7.setReleaseDate(LocalDate.of(2018, 3, 23));
        movie7.setIsActive(true);

        Movie movie8 = new Movie();
        movie8.setTitle("Spider-Man: No Way Home");
        movie8.setDescription("Peter Parker recurre al Doctor Strange para hacer que todos olviden que es Spider-Man, pero cuando el hechizo sale mal, villanos peligrosos de otros universos comienzan a aparecer.");
        movie8.setDuration(148);
        movie8.setGenre("Acción");
        movie8.setRating(MovieRating.PG_13);
        movie8.setPosterUrl("https://postercity.com.ar/wp-content/uploads/2022/03/Spiderman-No-way-home-60x90-1.jpg");
        movie8.setTrailerUrl("https://www.youtube.com/watch?v=JfVOs4VSpmA");
        movie8.setReleaseDate(LocalDate.of(2021, 12, 17));
        movie8.setIsActive(true);

        Movie movie9 = new Movie();
        movie9.setTitle("Five Nights at Freddy's 2");
        movie9.setDescription("Mike Schmidt regresa a Freddy Fazbear's Pizza para descubrir más secretos oscuros mientras nuevos animatrónicos aterradores cobran vida durante la noche.");
        movie9.setDuration(120);
        movie9.setGenre("Terror");
        movie9.setRating(MovieRating.PG_13);
        movie9.setPosterUrl("https://cdn.andro4all.com/andro4all/2025/11/fnaf-2-poster.jpg");
        movie9.setTrailerUrl("https://www.youtube.com/watch?v=E8M-iJ0p-Xk");
        movie9.setReleaseDate(LocalDate.of(2025, 12, 5));
        movie9.setIsActive(true);

        Movie movie10 = new Movie();
        movie10.setTitle("Rápidos y Furiosos");
        movie10.setDescription("Dominic Toretto y su familia de corredores callejeros deben enfrentar al adversario más letal que jamás hayan encontrado: un enemigo que emerge de las sombras del pasado.");
        movie10.setDuration(142);
        movie10.setGenre("Acción");
        movie10.setRating(MovieRating.PG_13);
        movie10.setPosterUrl("https://image.tmdb.org/t/p/original/x3zlm6VxPvVrYWE3bHkYUQMR798.jpg");
        movie10.setTrailerUrl("https://www.youtube.com/watch?v=O5BOxn8Go8U");
        movie10.setReleaseDate(LocalDate.of(2023, 5, 19));
        movie10.setIsActive(true);

        Movie movie11 = new Movie();
        movie11.setTitle("Minecraft Movie");
        movie11.setDescription("Una aventura épica basada en el popular videojuego donde los jugadores deben sobrevivir y construir en un mundo lleno de posibilidades infinitas y criaturas peligrosas.");
        movie11.setDuration(110);
        movie11.setGenre("Aventura");
        movie11.setRating(MovieRating.PG);
        movie11.setPosterUrl("https://m.media-amazon.com/images/M/MV5BYzFjMzNjOTktNDBlNy00YWZhLWExYTctZDcxNDA4OWVhOTJjXkEyXkFqcGc@._V1_.jpg");
        movie11.setTrailerUrl("https://www.youtube.com/watch?v=wJO_vIDZn-I");
        movie11.setReleaseDate(LocalDate.of(2025, 4, 4));
        movie11.setIsActive(true);

        Movie movie12 = new Movie();
        movie12.setTitle("Supergirl");
        movie12.setDescription("Kara Zor-El debe usar sus poderes kryptonianos para proteger la Tierra mientras descubre su lugar en un mundo que necesita un nuevo tipo de héroe.");
        movie12.setDuration(125);
        movie12.setGenre("Acción");
        movie12.setRating(MovieRating.PG_13);
        movie12.setPosterUrl("https://image.tmdb.org/t/p/original/aWwUDeGTta7kslsZMKxyy7j4ZBh.jpg");
        movie12.setTrailerUrl("https://www.youtube.com/watch?v=VfUVEB0IpYk");
        movie12.setReleaseDate(LocalDate.of(2026, 6, 25));
        movie12.setIsActive(true);

        movieRepository.save(movie1);
        movieRepository.save(movie2);
        movieRepository.save(movie3);
        movieRepository.save(movie4);
        movieRepository.save(movie5);
        movieRepository.save(movie6);
        movieRepository.save(movie7);
        movieRepository.save(movie8);
        movieRepository.save(movie9);
        movieRepository.save(movie10);
        movieRepository.save(movie11);
        movieRepository.save(movie12);

        log.info("12 películas cargadas exitosamente");
    }

    private void loadCinemas() {
        log.info("Cargando cines...");

        Cinema cinema1 = new Cinema();
        cinema1.setName("Cine Nuevo Centro");
        cinema1.setAddress("Av. Colón 5000");
        cinema1.setCity("Córdoba");
        cinema1.setPhone("0351-4711111");
        cinema1.setIsActive(true);

        Cinema cinema2 = new Cinema();
        cinema2.setName("Cine Patio Olmos");
        cinema2.setAddress("Bv. San Juan 49");
        cinema2.setCity("Córdoba");
        cinema2.setPhone("0351-4222222");
        cinema2.setIsActive(true);

        Cinema cinema3 = new Cinema();
        cinema3.setName("Cine Malvinas Argentinas");
        cinema3.setAddress("Av. Fuerza Aérea 2950");
        cinema3.setCity("Córdoba");
        cinema3.setPhone("0351-4333333");
        cinema3.setIsActive(true);

        Cinema cinema4 = new Cinema();
        cinema4.setName("Cine Dinosaurio Mall");
        cinema4.setAddress("Av. Vélez Sarsfield 361");
        cinema4.setCity("Córdoba");
        cinema4.setPhone("0351-4444444");
        cinema4.setIsActive(true);

        Cinema cinema5 = new Cinema();
        cinema5.setName("Cine Santa Fe");
        cinema5.setAddress("Av. Brig. Gral. Juan Manuel de Rosas 2019");
        cinema5.setCity("Santa Fe");
        cinema5.setPhone("0342-4555555");
        cinema5.setIsActive(true);

        Cinema cinema6 = new Cinema();
        cinema6.setName("Cine Rosario");
        cinema6.setAddress("Av. Carlos Pellegrini 3330");
        cinema6.setCity("Rosario");
        cinema6.setPhone("0341-4666666");
        cinema6.setIsActive(true);

        cinemaRepository.save(cinema1);
        cinemaRepository.save(cinema2);
        cinemaRepository.save(cinema3);
        cinemaRepository.save(cinema4);
        cinemaRepository.save(cinema5);
        cinemaRepository.save(cinema6);

        log.info("6 cines cargados exitosamente");
    }
}