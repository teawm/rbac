package com.taxi.tripservice.service;

import com.taxi.tripservice.entity.Trip;
import com.taxi.tripservice.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TripStatisticsService {

    private final TripRepository tripRepository;

    public TripStats getTodayStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        var trips = tripRepository.findAll();

        long total = trips.stream()
                .filter(t -> !t.getCreatedAt().isBefore(startOfDay) &&
                        !t.getCreatedAt().isAfter(endOfDay))
                .count();

        BigDecimal revenue = trips.stream()
                .filter(t -> !t.getCreatedAt().isBefore(startOfDay) &&
                        !t.getCreatedAt().isAfter(endOfDay) &&
                        t.getPrice() != null)
                .map(Trip::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long assigned = trips.stream().filter(t -> t.getStatus() == Trip.TripStatus.ASSIGNED).count();
        long completed = trips.stream().filter(t -> t.getStatus() == Trip.TripStatus.COMPLETED).count();

        return new TripStats(total, assigned, completed, revenue);
    }

    public record TripStats(
            long totalTrips,
            long assignedTrips,
            long completedTrips,
            BigDecimal totalRevenue
    ) {}
}