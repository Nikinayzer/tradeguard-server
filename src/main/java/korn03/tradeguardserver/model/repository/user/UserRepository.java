package korn03.tradeguardserver.model.repository.user;

import jakarta.persistence.LockModeType;
import korn03.tradeguardserver.model.entity.user.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @NotNull
    List<User> findAll();

    Optional<User> findUserById(long id);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndEmailVerifiedTrue(String email);

    @Lock(LockModeType.WRITE)
    void deleteById(long id);


    Optional<User> findByUsername(String username);
}

