package com.taxi.userservice.service;

import com.taxi.userservice.dto.DriverRequest;
import com.taxi.userservice.dto.DriverResponse;
import com.taxi.userservice.entity.Driver;
import com.taxi.userservice.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;

    @Transactional
    public Driver registerDriver(DriverRequest request) {
        if (driverRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Driver with this email already exists");
        }

        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new RuntimeException("Driver with this license number already exists");
        }

        Driver driver = Driver.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .licenseNumber(request.getLicenseNumber())
                .status(Driver.DriverStatus.AVAILABLE)
                .build();

        return driverRepository.save(driver);
    }

    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));
    }

    @Transactional
    public Driver updateDriverStatus(Long id, Driver.DriverStatus status) {
        Driver driver = getDriverById(id);
        driver.setStatus(status);
        return driver;
    }

    public Driver findAvailableDriver() {
        return driverRepository.findFirstAvailableDriver()
                .orElseThrow(() -> new RuntimeException("No available drivers"));
    }

    @Transactional
    public Driver reserveAvailableDriver() {
        Optional<Driver> driverOpt = driverRepository.findFirstAvailableDriver();

        if (driverOpt.isEmpty()) {
            throw new RuntimeException("No available drivers");
        }

        Driver driver = driverOpt.get();
        driver.setStatus(Driver.DriverStatus.BUSY);
        return driverRepository.save(driver);
    }
}