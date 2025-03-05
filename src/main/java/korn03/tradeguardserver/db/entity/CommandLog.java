package korn03.tradeguardserver.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "command_logs")
public class CommandLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "command", nullable = false, columnDefinition = "TEXT")
    private String command;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters; // JSON stored as String

    @Column(name = "status", nullable = false)
    private String status; // SUCCESS, FAILURE, PENDING

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs; // Store execution time for monitoring

    @Column(name = "ip_address")
    private String ipAddress; // Track IP of user

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent; // Browser or device info

}
