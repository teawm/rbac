package com.taxi.userservice.controller;

import com.taxi.userservice.dto.PassengerRequest;
import com.taxi.userservice.entity.Passenger;
import com.taxi.userservice.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @PostMapping
    public ResponseEntity<Passenger> registerPassenger(
            @Valid @RequestBody PassengerRequest request) {
        Passenger passenger = passengerService.registerPassenger(request);
        return ResponseEntity.ok(passenger);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Passenger> getPassenger(@PathVariable Long id) {
        return ResponseEntity.ok(passengerService.getPassengerById(id));
    }
}