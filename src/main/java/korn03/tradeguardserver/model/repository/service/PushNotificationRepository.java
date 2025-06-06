package korn03.tradeguardserver.model.repository.service;

import korn03.tradeguardserver.model.entity.service.notifications.NotificationCategory;
import korn03.tradeguardserver.model.entity.service.notifications.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    List<PushNotification> findAllByUserIdOrderBySentAtDesc(Long userId);
    List<PushNotification> findAllByUserIdAndCategoryOrderBySentAtDesc(Long userId, NotificationCategory category);
    Optional<PushNotification> findByIdAndUserId(Long id, Long userId);
    Integer countByUserIdAndReadFalse(Long userId);
}