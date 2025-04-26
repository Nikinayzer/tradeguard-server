package korn03.tradeguardserver.model.entity.user.connections;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "exchange_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "account_name"}))
public class UserExchangeAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private ExchangeProvider provider;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "demo", nullable = false)
    private boolean demo;

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
