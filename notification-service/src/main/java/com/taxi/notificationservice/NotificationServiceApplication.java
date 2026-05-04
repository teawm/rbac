package com.taxi.notificationservice;

import com.taxi.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceApplication {

    private final NotificationService notificationService;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(
                NotificationServiceApplication.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gracefully...");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Shutdown complete");
        }));
    }
}