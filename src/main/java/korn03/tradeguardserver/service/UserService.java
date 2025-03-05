package korn03.tradeguardserver.service;

import korn03.tradeguardserver.db.entity.Role;
import korn03.tradeguardserver.db.entity.User;
import korn03.tradeguardserver.db.repository.UserRepository;
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

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User loadUserById(int id) throws UsernameNotFoundException {
        return userRepository.findUserById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User createUser(User user) {
        if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) user.setRoles(Set.of(Role.USER));
        if (user.getRegisteredAt() == null) user.setRegisteredAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user.setRegisteredAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
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
