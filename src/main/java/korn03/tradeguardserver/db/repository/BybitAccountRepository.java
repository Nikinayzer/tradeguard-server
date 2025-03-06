package korn03.tradeguardserver.db.repository;

import korn03.tradeguardserver.db.entity.BybitAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BybitAccountRepository extends JpaRepository<BybitAccount, Long> {
    List<BybitAccount> findByUserId(Long userId);
    Optional<BybitAccount> findByUserIdAndAccountName(Long userId, String accountName);
}
