package korn03.tradeguardserver.service.user;

import korn03.tradeguardserver.model.entity.user.Role;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
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

    public User getUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User createUser(User user) {
        user.setRoles(Set.of(Role.USER));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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


    public User addUserRole(String username, Role role) {
        User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.addRole(role);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return user;
    }

    public User removeUserRole(String username, Role role) {
        User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
}
