package korn03.tradeguardserver.model.repository;

import jakarta.persistence.LockModeType;
import korn03.tradeguardserver.model.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @NotNull
    List<User> findAll();

    Optional<User> findUserById(long id);

    Optional<User> findUserByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Lock(LockModeType.WRITE)
    void deleteById(long id);


    Optional<User> findByUsername(String username);
}

