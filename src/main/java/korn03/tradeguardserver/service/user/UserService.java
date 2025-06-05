package korn03.tradeguardserver.service.user;

import korn03.tradeguardserver.endpoints.dto.user.UserRegisterRequestDTO;
import korn03.tradeguardserver.exception.registration.EmailTakenException;
import korn03.tradeguardserver.exception.registration.UsernameTakenException;
import korn03.tradeguardserver.model.entity.user.Role;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
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

    public Optional<User> getByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public User getByEmailOrThrow(String email) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("No user found"));
    }

    public Optional<User> findByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username);
    }
    public void createUserFromDTO(UserRegisterRequestDTO request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        createUser(user);
    }

    public User createUser(User user) {
        validateUserDoesNotExist(user.getUsername(), user.getEmail());
        user.setRoles(new HashSet<>(Set.of(Role.USER)));
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
//        if (Objects.equals(user.getEmail(), "korn03@vse.cz")){
//            return user; //todo testing
//        }
        user.setEmailVerified(user.isEmailVerified());
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
    public User verifyUserEmail(String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEmailVerified(true);
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    public void updateUser(User user) {
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    public void addUserRole(Long userId, Role role) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.addRole(role);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    public User removeUserRole(Long userId, Role role) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.removeRole(role);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return user;
    }

    public void toggleUserTwoFactor(Long id, boolean enableTwoFactor) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setTwoFactorEnabled(enableTwoFactor);
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
    }

    public void changeUserPassword(Long id, String newPassword) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        user.setPasswordUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean userExists(String username, String email) {
        return userExistsByEmail(email) && userExistsByUsername(username);
    }

    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Validates that a user with the given username and email (verified) does not already exist. <br>
     *
     * @param username username of the user
     * @param email    email of the user
     * @throws UsernameTakenException if the username is already taken
     * @throws EmailTakenException    if the email is already taken and verified
     * @apiNote This method is used during user registration to ensure that the username and email are unique.
     */
    public void validateUserDoesNotExist(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameTakenException("Username is already taken.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailTakenException("Email is already taken.");
        }
    }

    /**
     * Validates the date of birth of a user. Expects the dateOfBirth in ISO format (YYYY-MM-DD).
     *
     * @param userId      ID of user
     * @param dateOfBirth Date of birth in ISO format (YYYY-MM-DD)
     * @return true if the date of birth is valid, false otherwise
     */
    public boolean validateDateOfBirth(Long userId, String dateOfBirth) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        LocalDate userDob = user.getDateOfBirth();
        if (userDob == null || dateOfBirth == null) {
            return false;
        }
        try {
            LocalDate inputDob = LocalDate.parse(dateOfBirth);
            return userDob.equals(inputDob);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
