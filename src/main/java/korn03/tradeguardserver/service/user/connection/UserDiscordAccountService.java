package korn03.tradeguardserver.service.user.connection;

import korn03.tradeguardserver.model.entity.user.connections.UserDiscordAccount;
import korn03.tradeguardserver.model.repository.user.connections.UserDiscordAccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDiscordAccountService {

    private final UserDiscordAccountRepository discordAccountRepository;

    public UserDiscordAccountService(UserDiscordAccountRepository discordAccountRepository) {
        this.discordAccountRepository = discordAccountRepository;
    }

    /**
     * Finds a Discord account by Discord ID.
     */
    public Optional<UserDiscordAccount> findByDiscordId(Long discordId) {
        return discordAccountRepository.findByDiscordId(discordId);
    }

    /**
     * Adds a new Discord account connection for a user.
     */
    public UserDiscordAccount addDiscordAccount(Long userId, Long discordId, String discordUsername, String discordAvatar) {
        UserDiscordAccount account = new UserDiscordAccount();
        account.setUserId(userId);
        account.setDiscordId(discordId);
        account.setDiscordUsername(discordUsername);
        account.setDiscordAvatar(discordAvatar);
        return discordAccountRepository.save(account);
    }

    public void updateDiscordAccount(Long userId, Long discordId, String discordUsername, String discordAvatar) {
        UserDiscordAccount account = discordAccountRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new IllegalArgumentException("Discord account not found"));
        account.setDiscordUsername(discordUsername);
        account.setDiscordAvatar(discordAvatar);
        discordAccountRepository.save(account);
    }

    /**
     * Retrieves Discord account of a user by user ID.
     */
    public Optional<UserDiscordAccount> getUserDiscordAccounts(Long userId) {
        return discordAccountRepository.findByUserId(userId);
    }

    /**
     * Retrieves a specific Discord account by user ID and account ID.
     */
    public Optional<UserDiscordAccount> getDiscordAccount(Long userId) {
        return discordAccountRepository.findByUserId(userId);
    }

    /**
     * Deletes a Discord account for a user.
     */
    public void deleteDiscordAccount(Long userId, Long id) {
        discordAccountRepository.deleteByUserIdAndId(userId, id);
    }
}
