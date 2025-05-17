package korn03.tradeguardserver.model.entity.service.notifications;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "push_notifications")
public class PushNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private NotificationCategory category = NotificationCategory.SYSTEM;

    @Column(nullable = false)
    private NotificationType type = NotificationType.INFO;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    @Column(length = 1024)
    private String data;

    // ANDROID SPECIFIC
    @Column(length = 100)
    private String channelId;

    // IOS SPECIFIC
    @Column(length = 100)
    private Integer notificationLevel;

    @Column(length = 512)
    private String deepLink;

    private Instant sentAt = Instant.now();

}