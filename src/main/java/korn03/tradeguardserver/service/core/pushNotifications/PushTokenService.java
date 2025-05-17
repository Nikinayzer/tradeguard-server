package korn03.tradeguardserver.service.core.pushNotifications;

import korn03.tradeguardserver.model.entity.service.PushToken;
import korn03.tradeguardserver.model.repository.service.PushTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PushTokenService {

    private final PushTokenRepository pushTokenRepository;

    @Autowired
    public PushTokenService(PushTokenRepository pushTokenRepository) {
        this.pushTokenRepository = pushTokenRepository;
    }

    /**
     * Upsert the push token for the given user.
     * Doesn't check for already existing token since user can have multiple clients.
     */
    public void registerPushToken(Long userId, String pushToken) {
        PushToken token = new PushToken();
        token.setUserId(userId);
        token.setToken(pushToken);
        pushTokenRepository.save(token);
    }

    @Transactional
    public void unregisterPushToken(String pushToken) {
        try {
            pushTokenRepository.deleteByToken(pushToken);
        } catch (Exception e) {
           throw new RuntimeException("Failed to unregister push token", e);
        }
    }
    public List<PushToken> getAllPushTokens() {
        return pushTokenRepository.findAll();
    }
    public List<Long> getAllUserIds() {
        return pushTokenRepository.findDistinctUserIds();
    }
    public List<PushToken> getPushTokensByUserId(Long userId) {
        return pushTokenRepository.findByUserId(userId);
    }

    public Optional<PushToken> getPushTokenByToken(String token) {
        return pushTokenRepository.findByToken(token);
    }
}