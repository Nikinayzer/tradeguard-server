package korn03.tradeguardserver.model.repository.service;

import korn03.tradeguardserver.model.entity.service.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    List<PushToken> findByUserId(Long userId);
    Optional<PushToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByTokenAndUserId(String token, Long userId);
}