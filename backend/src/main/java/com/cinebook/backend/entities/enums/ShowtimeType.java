package com.cinebook.backend.entities.enums;

public enum ShowtimeType {
    SPANISH_2D("Español Latino 2D"),
    SUBTITLED_2D("Inglés Subtitulado 2D"),
    SPANISH_3D("Español Latino 3D");

    private final String displayName;

    ShowtimeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}