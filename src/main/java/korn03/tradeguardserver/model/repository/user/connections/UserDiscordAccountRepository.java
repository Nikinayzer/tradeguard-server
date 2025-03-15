package korn03.tradeguardserver.model.repository.user.connections;

import korn03.tradeguardserver.model.entity.user.connections.UserDiscordAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDiscordAccountRepository extends JpaRepository<UserDiscordAccount, Long> {
    Optional<UserDiscordAccount> findByUserId(Long userId);

    Optional<UserDiscordAccount> findByDiscordId(Long discordId);

    Optional<UserDiscordAccount> findByUserIdAndId(Long userId, Long id);

    void deleteByUserIdAndId(Long userId, Long id);
}
