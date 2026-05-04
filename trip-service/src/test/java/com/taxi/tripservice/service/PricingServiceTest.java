package com.taxi.tripservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }

    @Test
    @DisplayName("Расчет цены: базовый сценарий")
    void calculatePrice_basicScenario() {
        BigDecimal result = pricingService.calculatePrice(5.0, 20);

        assertEquals(new BigDecimal("325.00"), result);
    }

    @Test
    @DisplayName("Расчет цены: минимальная цена")
    void calculatePrice_minimumPrice() {
        BigDecimal result = pricingService.calculatePrice(0.0, 0);

        assertEquals(new BigDecimal("150.00"), result);
    }

    @Test
    @DisplayName("Расчет цены: с коэффициентом (час пик)")
    void calculatePriceWithMultiplier() {
        BigDecimal base = pricingService.calculatePrice(10.0, 30);
        BigDecimal withSurge = pricingService.calculatePriceWithMultiplier(10.0, 30, 1.5);

        assertEquals(new BigDecimal("750.00"), withSurge);
        assertTrue(withSurge.compareTo(base) > 0);
    }

    @Test
    @DisplayName("Расчет цены: нулевые значения")
    void calculatePrice_nullValues() {
        BigDecimal result = pricingService.calculatePrice(null, null);

        assertEquals(new BigDecimal("100"), result);
    }

    @Test
    @DisplayName("Округление до 2 знаков")
    void calculatePrice_rounding() {
        BigDecimal result = pricingService.calculatePrice(1.3, 2);

        assertEquals(2, result.scale());
    }
}