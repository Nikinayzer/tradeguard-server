package korn03.tradeguardserver.model.repository.user.connections;

import korn03.tradeguardserver.model.entity.user.connections.UserBybitAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBybitAccountRepository extends JpaRepository<UserBybitAccount, Long> {
    List<UserBybitAccount> findByUserId(Long userId);
    Optional<UserBybitAccount> findByUserIdAndId(Long userId, Long id);
    void deleteByUserIdAndId(Long userId, Long id);
}
