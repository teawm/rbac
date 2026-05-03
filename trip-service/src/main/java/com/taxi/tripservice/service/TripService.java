package com.taxi.tripservice.service;

import com.taxi.tripservice.dto.TripRequest;
import com.taxi.tripservice.entity.Trip;
import com.taxi.tripservice.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final PricingService pricingService;

    @Value("${user.service.url:http://user-service:8081}")
    private String userServiceUrl;

    @Value("${notification.service.url:http://notification-service:8083}")
    private String notificationServiceUrl;

    @Transactional
    public Trip createTrip(TripRequest request) {
        try {
            restTemplate.getForEntity(
                    userServiceUrl + "/passengers/" + request.getPassengerId(),
                    Object.class
            );
        } catch (RestClientException e) {
            throw new RuntimeException("Passenger not found with id: " + request.getPassengerId());
        }

        Map<String, Object> driverResponse;
        try {
            driverResponse = restTemplate.postForEntity(
                    userServiceUrl + "/drivers/reserve",
                    null,
                    Map.class
            ).getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("No available drivers");
        }

        Long driverId = Long.valueOf(driverResponse.get("id").toString());
        String driverName = (String) driverResponse.get("name");

        Trip trip = Trip.builder()
                .passengerId(request.getPassengerId())
                .driverId(driverId)
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .status(Trip.TripStatus.ASSIGNED)
                .build();

        trip = tripRepository.save(trip);
        log.info("Trip created: id={}, passenger={}, driver={}",
                trip.getId(), request.getPassengerId(), driverId);

        createNotificationForPassenger(trip, driverName);
        createNotificationForDriver(trip);

        Double distance = 5.0;
        Integer duration = 20;
        BigDecimal price = pricingService.calculatePrice(distance, duration);

        trip.setDistanceKm(distance);
        trip.setDurationMin(duration);
        trip.setPrice(price);

        log.info("Trip #{} price calculated: {} RUB ({} km, {} min)",
                trip.getId(), price, distance, duration);

        return trip;
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + id));
    }

    private void createNotificationForPassenger(Trip trip, String driverName) {
        try {
            restTemplate.postForEntity(
                    notificationServiceUrl + "/notifications",
                    Map.of(
                            "tripId", trip.getId(),
                            "recipientId", trip.getPassengerId(),
                            "type", "PUSH",
                            "message", "Водитель найден! " + driverName + " прибудет через 5 минут."
                    ),
                    Void.class
            );
            log.info("Passenger notification created for trip {}", trip.getId());
        } catch (Exception e) {
            log.warn("Failed to create passenger notification: {}", e.getMessage());
        }
    }

    private void createNotificationForDriver(Trip trip) {
        try {
            restTemplate.postForEntity(
                    notificationServiceUrl + "/notifications",
                    Map.of(
                            "tripId", trip.getId(),
                            "recipientId", trip.getDriverId(),
                            "type", "PUSH",
                            "message", "Новый заказ! Поездка #" + trip.getId()
                    ),
                    Void.class
            );
            log.info("Driver notification created for trip {}", trip.getId());
        } catch (Exception e) {
            log.warn("Failed to create driver notification: {}", e.getMessage());
        }
    }
}