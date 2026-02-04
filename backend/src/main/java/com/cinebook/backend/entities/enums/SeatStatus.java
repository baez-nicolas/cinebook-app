package com.cinebook.backend.entities.enums;

public enum SeatStatus {
    AVAILABLE("Disponible"),
    RESERVED_RANDOM("Ocupado (Simulado)"),
    RESERVED_USER("Reservado");

    private final String displayName;

    SeatStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}