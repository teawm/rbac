package com.taxi.userservice.controller;

import com.taxi.userservice.dto.DriverRequest;
import com.taxi.userservice.dto.DriverResponse;
import com.taxi.userservice.entity.Driver;
import com.taxi.userservice.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriverResponse> registerDriver(
            @Valid @RequestBody DriverRequest request) {
        Driver driver = driverService.registerDriver(request);
        return ResponseEntity.ok(DriverResponse.fromEntity(driver));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriver(@PathVariable Long id) {
        Driver driver = driverService.getDriverById(id);
        return ResponseEntity.ok(DriverResponse.fromEntity(driver));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DriverResponse> updateDriverStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {

        Driver.DriverStatus status = Driver.DriverStatus.valueOf(
                statusUpdate.get("status").toUpperCase());

        Driver driver = driverService.updateDriverStatus(id, status);
        return ResponseEntity.ok(DriverResponse.fromEntity(driver));
    }

    @PostMapping("/reserve")
    public ResponseEntity<DriverResponse> reserveDriver() {
        try {
            Driver driver = driverService.reserveAvailableDriver();
            return ResponseEntity.ok(DriverResponse.fromEntity(driver));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }
}