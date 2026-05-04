package com.taxi.tripservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripRequest {
    @NotNull
    private Long passengerId;

    private String origin;
    private String destination;
}