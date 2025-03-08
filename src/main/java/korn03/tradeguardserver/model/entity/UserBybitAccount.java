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

    @Column(nullable = false)
    private String encryptedApiKey;

    @Column(nullable = false)
    private String encryptedApiSecret;
}
