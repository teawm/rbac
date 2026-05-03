package com.taxi.tripservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class PricingService {

    private static final BigDecimal BASE_PRICE = new BigDecimal("100");
    private static final BigDecimal PRICE_PER_KM = new BigDecimal("25");
    private static final BigDecimal PRICE_PER_MIN = new BigDecimal("5");
    private static final BigDecimal MIN_PRICE = new BigDecimal("150");

    public BigDecimal calculatePrice(Double distanceKm, Integer durationMin) {
        if (distanceKm == null || durationMin == null) {
            log.warn("Missing distance or duration, using base price");
            return BASE_PRICE;
        }

        BigDecimal distanceCost = PRICE_PER_KM.multiply(BigDecimal.valueOf(distanceKm));
        BigDecimal durationCost = PRICE_PER_MIN.multiply(BigDecimal.valueOf(durationMin));
        BigDecimal total = BASE_PRICE.add(distanceCost).add(durationCost);

        BigDecimal result = total.max(MIN_PRICE);

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    // Коэффициент ПУ
    public BigDecimal calculatePriceWithMultiplier(Double distanceKm, Integer durationMin, double multiplier) {
        BigDecimal base = calculatePrice(distanceKm, durationMin);
        return base.multiply(BigDecimal.valueOf(multiplier))
                .setScale(2, RoundingMode.HALF_UP);
    }
}