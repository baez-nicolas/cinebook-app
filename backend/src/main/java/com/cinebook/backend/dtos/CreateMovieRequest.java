package com.cinebook.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMovieRequest {
    private String title;
    private String description;
    private Integer duration;
    private String genre;
    private String rating;
    private String posterUrl;
    private String trailerUrl;
    private LocalDate releaseDate;
}

