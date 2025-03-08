package korn03.tradeguardserver.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "bybit_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "account_name"}))
public class UserBybitAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    // Read-only pair
    @Column(name = "readonly_api_key", nullable = false)
    private String encryptedReadOnlyApiKey;

    @Column(name = "readonly_api_secret", nullable = false)
    private String encryptedReadOnlyApiSecret;

    // Read-write pair
    @Column(name = "readwrite_api_key", nullable = false)
    private String encryptedReadWriteApiKey;

    @Column(name = "readwrite_api_secret", nullable = false)
    private String encryptedReadWriteApiSecret;
}
