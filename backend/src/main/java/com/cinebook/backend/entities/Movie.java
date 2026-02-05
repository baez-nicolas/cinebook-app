package com.cinebook.backend.entities;

import com.cinebook.backend.entities.enums.MovieRating;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer duration;

    @Column(length = 50)
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MovieRating rating;

    @Column(length = 500)
    private String posterUrl;

    @Column(length = 500)
    private String trailerUrl;

    private LocalDate releaseDate;

    @Column(nullable = false)
    private Boolean isActive = true;
}