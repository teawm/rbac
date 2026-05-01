package com.taxi.userservice.dto;

import com.taxi.userservice.entity.Driver;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DriverResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String licenseNumber;
    private String status;
    private LocalDateTime createdAt;

    public static DriverResponse fromEntity(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setName(driver.getName());
        response.setEmail(driver.getEmail());
        response.setPhone(driver.getPhone());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setStatus(driver.getStatus().name());
        response.setCreatedAt(driver.getCreatedAt());
        return response;
    }
}