package korn03.tradeguardserver.model.repository.service;

import korn03.tradeguardserver.model.entity.service.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    List<PushNotification> findAllByUserIdOrderBySentAtDesc(Long userId);
}