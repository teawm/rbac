package com.taxi.notificationservice.controller;

import com.taxi.notificationservice.entity.NotificationTask;
import com.taxi.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationTask> createNotification(
            @RequestBody CreateNotificationRequest request) {

        NotificationTask task = notificationService.createNotification(
                request.getTripId(),
                request.getRecipientId(),
                request.getType(),
                request.getMessage()
        );
        return ResponseEntity.ok(task);
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> triggerProcessing() {
        notificationService.processPendingNotifications();
        return ResponseEntity.ok(Map.of("status", "Processing started"));
    }

    @GetMapping("/stats")
    public ResponseEntity<NotificationService.NotificationStats> getStats() {
        return ResponseEntity.ok(notificationService.getStats());
    }

    public static class CreateNotificationRequest {
        private Long tripId;
        private Long recipientId;
        private String type;
        private String message;

        public Long getTripId() { return tripId; }
        public void setTripId(Long tripId) { this.tripId = tripId; }

        public Long getRecipientId() { return recipientId; }
        public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}