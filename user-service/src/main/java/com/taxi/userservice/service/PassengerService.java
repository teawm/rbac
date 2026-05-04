package com.taxi.userservice.service;

import com.taxi.userservice.dto.PassengerRequest;
import com.taxi.userservice.entity.Passenger;
import com.taxi.userservice.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassengerService {

    private final PassengerRepository passengerRepository;

    @Transactional
    public Passenger registerPassenger(PassengerRequest request) {
        if (passengerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Passenger with this email already exists");
        }

        Passenger passenger = Passenger.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        return passengerRepository.save(passenger);
    }

    public Passenger getPassengerById(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Passenger not found with id: " + id));
    }
}