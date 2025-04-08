package korn03.tradeguardserver.model.entity.service;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter

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

    @Setter
    @Column(nullable = false)
    private Long userId;

    @Setter
    @Column(nullable = false)
    private String title;

    @Setter
    @Column(nullable = false)
    @JsonValue
    private String body;

    @Setter
    @Column(length = 1024)
    private String data;

    // ANDROID SPECIFIC
    //todo refactor later, probably decoupled service
    @Column(length = 100)
    private String channelId;

    // ANDROID SPECIFIC
    @Column(length = 100)
    private Integer notificationLevel;

    @Column(length = 512)
    private String deepLink;

    @Setter
    private Instant sentAt = Instant.now();

}