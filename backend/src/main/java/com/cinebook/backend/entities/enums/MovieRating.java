package com.cinebook.backend.entities.enums;

public enum MovieRating {
    G("Apto para todo público"),
    PG("Se sugiere orientación de los padres"),
    PG_13("Mayores de 13 años"),
    R("Restringida - Menores de 17 requieren acompañante"),
    NC_17("Prohibida para menores de 17 años");

    private final String description;

    MovieRating(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}