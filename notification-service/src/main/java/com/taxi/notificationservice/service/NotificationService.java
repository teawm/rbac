package com.taxi.notificationservice.service;

import com.taxi.notificationservice.entity.NotificationTask;
import com.taxi.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationTask createNotification(Long tripId, Long recipientId,
                                               String type, String message) {
        NotificationTask task = NotificationTask.builder()
                .tripId(tripId)
                .recipientId(recipientId)
                .type(type)
                .message(message)
                .status(NotificationTask.NotificationStatus.PENDING)
                .build();

        log.info("Created notification task: {}", task.getId());
        return notificationRepository.save(task);
    }

    @Async("notificationExecutor")
    public void processPendingNotifications() {
        log.info("Starting notification processing...");

        while (true) {
            NotificationTask task = fetchAndLockTask();
            if (task == null) {
                log.info("No more pending tasks");
                break;
            }

            processTask(task);
        }
    }

    @Transactional
    public NotificationTask fetchAndLockTask() {
        return notificationRepository.findNextPendingTask().orElse(null);
    }

    @Transactional
    public void processTask(NotificationTask task) {
        try {
            log.info("Processing task {}: {}", task.getId(), task.getMessage());

            task.setStatus(NotificationTask.NotificationStatus.IN_PROGRESS);
            notificationRepository.save(task);

            simulateSending(task);

            task.setStatus(NotificationTask.NotificationStatus.SENT);
            task.setProcessedAt(LocalDateTime.now());
            log.info("Task {} sent successfully", task.getId());

        } catch (Exception e) {
            log.error("Failed to process task {}: {}", task.getId(), e.getMessage());
            task.setStatus(NotificationTask.NotificationStatus.FAILED);
            task.setErrorMessage(e.getMessage());
        } finally {
            notificationRepository.save(task);
        }
    }

    private void simulateSending(NotificationTask task) throws InterruptedException {
        Thread.sleep(100 + (long)(Math.random() * 400));

        if (Math.random() < 0.05) {
            throw new RuntimeException("Simulated network error");
        }

        log.info("[msg] [{}] To #{}: {}", task.getType(), task.getRecipientId(), task.getMessage());
    }

    public NotificationStats getStats() {
        List<NotificationTask> all = notificationRepository.findAll();
        return new NotificationStats(
                countByStatus(all, NotificationTask.NotificationStatus.PENDING),
                countByStatus(all, NotificationTask.NotificationStatus.IN_PROGRESS),
                countByStatus(all, NotificationTask.NotificationStatus.SENT),
                countByStatus(all, NotificationTask.NotificationStatus.FAILED)
        );
    }

    private long countByStatus(List<NotificationTask> tasks, NotificationTask.NotificationStatus status) {
        return tasks.stream().filter(t -> t.getStatus() == status).count();
    }

    public static class NotificationStats {
        private long pending;
        private long inProgress;
        private long sent;
        private long failed;

        public NotificationStats(long pending, long inProgress, long sent, long failed) {
            this.pending = pending;
            this.inProgress = inProgress;
            this.sent = sent;
            this.failed = failed;
        }

        public long getPending() { return pending; }
        public long getInProgress() { return inProgress; }
        public long getSent() { return sent; }
        public long getFailed() { return failed; }
    }
}