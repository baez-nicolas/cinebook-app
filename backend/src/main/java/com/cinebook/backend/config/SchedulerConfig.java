package com.cinebook.backend.config;

import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final IWeeklyScheduleService weeklyScheduleService;

    @Scheduled(cron = "0 0 23 * * WED")
    public void weeklyReset() {
        log.info("🔄 RESET AUTOMÁTICO - Miércoles 23:00");
        weeklyScheduleService.checkAndResetIfNeeded();
    }
}

