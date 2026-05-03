package com.taxi.tripservice.controller;

import com.taxi.tripservice.service.TripStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trips/statistics")
@RequiredArgsConstructor
public class TripStatisticsController {

    private final TripStatisticsService statisticsService;

    @GetMapping("/today")
    public ResponseEntity<TripStatisticsService.TripStats> getTodayStats() {
        return ResponseEntity.ok(statisticsService.getTodayStats());
    }

    @GetMapping("/summary")
    public ResponseEntity<String> getSummary() {
        var stats = statisticsService.getTodayStats();
        String summary = String.format(
                "Статистика за сегодня:\n" +
                        "- Всего поездок: %d\n" +
                        "- Назначено: %d\n" +
                        "- Завершено: %d\n" +
                        "- Выручка: %.2f RUB",
                stats.totalTrips(),
                stats.assignedTrips(),
                stats.completedTrips(),
                stats.totalRevenue()
        );
        return ResponseEntity.ok(summary);
    }
}