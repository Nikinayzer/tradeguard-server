package korn03.tradeguardserver.model.repository.user.connections;

import korn03.tradeguardserver.model.entity.user.connections.UserExchangeAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBybitAccountRepository extends JpaRepository<UserExchangeAccount, Long> {
    List<UserExchangeAccount> findByUserId(Long userId);
    Optional<UserExchangeAccount> findByUserIdAndId(Long userId, Long id);
    void deleteByUserIdAndId(Long userId, Long id);
}
