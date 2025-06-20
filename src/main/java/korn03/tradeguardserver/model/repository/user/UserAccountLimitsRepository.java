package korn03.tradeguardserver.model.repository.user;

import korn03.tradeguardserver.model.entity.user.UserAccountLimits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountLimitsRepository extends JpaRepository<UserAccountLimits, Long> {
    Optional<UserAccountLimits> findByUserId(Long userId);
}
