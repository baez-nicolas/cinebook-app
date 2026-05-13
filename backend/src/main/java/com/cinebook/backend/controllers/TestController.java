package com.cinebook.backend.controllers;

import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import com.cinebook.backend.services.interfaces.IShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test", description = "Endpoints de prueba (ELIMINAR EN PRODUCCIÓN)")
@CrossOrigin(origins = "*")
public class TestController {

    private final IWeeklyScheduleService weeklyScheduleService;
    private final IShowtimeService showtimeService;

    @PostMapping("/daily-update")
    @Operation(summary = "PRUEBA: Forzar actualización diaria de funciones")
    public ResponseEntity<?> testDailyUpdate() {
        log.info("TEST: Ejecutando actualización diaria manualmente...");

        try {
            boolean updated = weeklyScheduleService.checkAndResetIfNeeded();

            if (updated) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Actualización diaria ejecutada exitosamente",
                    "info", "Verifica los logs para ver el proceso completo"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No fue necesario actualizar (la ventana ya está al día)",
                    "info", "Esto es normal si ya se ejecutó hoy"
                ));
            }
        } catch (Exception e) {
            log.error("Error en test de actualización diaria: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/info")
    @Operation(summary = "Ver información del sistema de funciones")
    public ResponseEntity<?> getSystemInfo() {
        log.info("Obteniendo información del sistema...");

        try {
            var currentWeek = weeklyScheduleService.getCurrentWeek();

            return ResponseEntity.ok(Map.of(
                "weekId", currentWeek.getWeekId(),
                "startDate", currentWeek.getWeekStartDate().toString(),
                "endDate", currentWeek.getWeekEndDate().toString(),
                "isActive", currentWeek.getIsActive(),
                "info", "Esta es la ventana actual de 7 días de funciones"
            ));
        } catch (Exception e) {
            log.error("Error obteniendo info: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/regenerate-showtimes")
    @Operation(summary = "EMERGENCIA: Regenerar todas las funciones de la semana actual")
    public ResponseEntity<?> regenerateShowtimes() {
        log.info("EMERGENCIA: Regenerando todas las funciones de la semana actual...");

        try {
            showtimeService.generateShowtimesForCurrentWeek();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Funciones regeneradas exitosamente",
                "info", "Se generaron funciones para todas las películas activas en todos los cines para la semana actual"
            ));
        } catch (Exception e) {
            log.error("Error regenerando funciones: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
}

