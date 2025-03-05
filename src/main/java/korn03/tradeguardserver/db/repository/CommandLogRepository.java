package korn03.tradeguardserver.db.repository;

import korn03.tradeguardserver.db.entity.CommandLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandLogRepository extends JpaRepository<CommandLog, Long> {

    List<CommandLog> findByUserIdOrderByTimestampDesc(String userId);
}
