package com.taxi.notificationservice.repository;

import com.taxi.notificationservice.entity.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findByStatus(NotificationTask.NotificationStatus status);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NotificationTask n WHERE n.status = 'PENDING' ORDER BY n.createdAt ASC LIMIT 1")
    Optional<NotificationTask> findNextPendingTask();

    List<NotificationTask> findByStatusIn(List<NotificationTask.NotificationStatus> statuses);
}