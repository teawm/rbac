package com.taxi.tripservice.controller;

import com.taxi.tripservice.dto.TripRequest;
import com.taxi.tripservice.dto.RatingRequest;
import com.taxi.tripservice.entity.Trip;
import com.taxi.tripservice.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<Trip> createTrip(@Valid @RequestBody TripRequest request) {
        return ResponseEntity.ok(tripService.createTrip(request));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Trip> rateTrip(
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request) {

        Trip updatedTrip = tripService.rateTrip(id, request.getRating(), request.getFeedback());
        return ResponseEntity.ok(updatedTrip);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTrip(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTripById(id));
    }
}