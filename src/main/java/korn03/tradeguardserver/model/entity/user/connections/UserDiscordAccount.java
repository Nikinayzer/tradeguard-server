package korn03.tradeguardserver.model.entity.user.connections;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "discord_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "discord_id"}))
public class UserDiscordAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "discord_id", nullable = false)
    private Long discordId;

    @Column(name = "discord_username", nullable = false)
    private String discordUsername;

    @Column(name = "discord_avatar")
    private String discordAvatar;
}
