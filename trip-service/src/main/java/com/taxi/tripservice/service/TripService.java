package com.taxi.tripservice.service;

import com.taxi.tripservice.dto.TripRequest;
import com.taxi.tripservice.entity.Trip;
import com.taxi.tripservice.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Transactional
    public Trip createTrip(TripRequest request) {
        try {
            restTemplate.getForEntity(
                    userServiceUrl + "/passengers/" + request.getPassengerId(),
                    Object.class
            );
        } catch (RestClientException e) {
            throw new RuntimeException("Passenger not found");
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

        Trip trip = Trip.builder()
                .passengerId(request.getPassengerId())
                .driverId(driverId)
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .status(Trip.TripStatus.ASSIGNED)
                .build();

        return tripRepository.save(trip);
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
    }
}