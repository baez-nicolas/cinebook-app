package com.cinebook.backend.dtos;

import com.cinebook.backend.entities.enums.MovieRating;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private String genre;
    private MovieRating rating;
    private String posterUrl;
    private String trailerUrl;
    private LocalDate releaseDate;
}