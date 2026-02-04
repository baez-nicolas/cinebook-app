package com.cinebook.backend.entities.enums;

public enum PaymentStatus {
    PENDING("Pendiente"),
    CONFIRMED("Confirmado"),
    CANCELLED("Cancelado");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}