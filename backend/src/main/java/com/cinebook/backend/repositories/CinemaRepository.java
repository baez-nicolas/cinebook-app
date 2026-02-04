package com.cinebook.backend.repositories;

import com.cinebook.backend.entities.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {
    List<Cinema> findByIsActiveTrue();
    Cinema findByName(String name);
}