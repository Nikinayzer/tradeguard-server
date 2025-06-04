package korn03.tradeguardserver.service.user;

import korn03.tradeguardserver.model.entity.user.Role;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountLimitsService userAccountLimitsService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserAccountLimitsService userAccountLimitsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAccountLimitsService = userAccountLimitsService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getById(Long id) throws UsernameNotFoundException {
        return userRepository.findUserById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Optional<User> getByEmail(String email){
        return userRepository.findUserByEmail(email);
    }

    public Optional<User> findByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username);
    }

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("User with this username already exists"); //todo change to custom exception
        }
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists"); //todo change to custom exception
        }
        user.setRoles(new HashSet<>(Set.of(Role.USER)));
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword())); //todo handle password validation/random password
        }
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user.setRegisteredAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        userAccountLimitsService.createDefaultLimits(user);
        return userRepository.save(user); //todo fix this mess
    }

    public void updateUser(User user) {
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    public User addUserRole(Long userId, Role role) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.addRole(role);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return user;
    }

    public User removeUserRole(Long userId, Role role) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.removeRole(role);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return user;
    }

    public void changeUserPassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }
}
