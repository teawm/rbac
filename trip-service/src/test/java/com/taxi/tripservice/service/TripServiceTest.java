package com.taxi.tripservice.service;

import com.taxi.tripservice.dto.TripRequest;
import com.taxi.tripservice.entity.Trip;
import com.taxi.tripservice.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private TripService tripService;

    private TripRequest tripRequest;

    @BeforeEach
    void setUp() {
        tripRequest = new TripRequest();
        tripRequest.setPassengerId(1L);
        tripRequest.setOrigin("Дом");
        tripRequest.setDestination("Офис");

        // 🔧 Гарантируем инициализацию URL (обход @Value в тестах)
        ReflectionTestUtils.setField(tripService, "userServiceUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(tripService, "notificationServiceUrl", "http://localhost:8083");
    }

    @Test
    @DisplayName("Создание поездки: успешный сценарий")
    void createTrip_success() {
        // 🔧 Используем doReturn/when для надёжного мокинга
        // Порядок аргументов postForEntity: (url, requestBody, responseType)

        doReturn(ResponseEntity.ok(Map.of("id", 1, "name", "Пассажир")))
                .when(restTemplate).getForEntity(anyString(), eq(Object.class));

        doReturn(ResponseEntity.ok(Map.of("id", 2L, "name", "Водитель")))
                .when(restTemplate).postForEntity(anyString(), isNull(), eq(Map.class));

        doReturn(new BigDecimal("325.00"))
                .when(pricingService).calculatePrice(anyDouble(), anyInt());

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Trip result = tripService.createTrip(tripRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPassengerId());
        assertEquals(2L, result.getDriverId());
        assertEquals(Trip.TripStatus.ASSIGNED, result.getStatus());
        assertEquals(new BigDecimal("325.00"), result.getPrice());

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    @DisplayName("Создание поездки: ошибка при проверке пассажира")
    void createTrip_passengerError() {
        doThrow(new RestClientException("Passenger not found"))
                .when(restTemplate).getForEntity(anyString(), eq(Object.class));

        assertThrows(RuntimeException.class, () -> tripService.createTrip(tripRequest));
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    @DisplayName("Создание поездки: ошибка при резервировании водителя")
    void createTrip_driverError() {
        doReturn(ResponseEntity.ok(Map.of("id", 1)))
                .when(restTemplate).getForEntity(anyString(), eq(Object.class));

        doThrow(new RestClientException("No drivers"))
                .when(restTemplate).postForEntity(anyString(), isNull(), eq(Map.class));

        assertThrows(RuntimeException.class, () -> tripService.createTrip(tripRequest));
        verify(tripRepository, never()).save(any(Trip.class));
    }
}