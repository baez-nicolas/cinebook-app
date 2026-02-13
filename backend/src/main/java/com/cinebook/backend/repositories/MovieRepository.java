package com.cinebook.backend.repositories;

import com.cinebook.backend.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByIsActiveTrue();
    List<Movie> findByGenre(String genre);
    long countByIsActiveTrue();
    List<Movie> findByIsActiveTrueOrderByIdAsc();
}