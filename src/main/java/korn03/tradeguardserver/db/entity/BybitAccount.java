package korn03.tradeguardserver.db.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "bybit_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "account_name"}))
public class BybitAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(nullable = false)
    private String encryptedApiKey;

    @Column(nullable = false)
    private String encryptedApiSecret;
}
