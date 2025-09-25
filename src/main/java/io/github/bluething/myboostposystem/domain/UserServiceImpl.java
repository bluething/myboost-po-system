package io.github.bluething.myboostposystem.domain;

import io.github.bluething.myboostposystem.exception.DuplicateResourceException;
import io.github.bluething.myboostposystem.persistence.User;
import io.github.bluething.myboostposystem.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserData createUser(CreateUserCommand command) {
        log.debug("Creating user with email: {}", command.email());

        // Business validation - check if email already exists
        validateEmailUniqueness(command.email());

        User userToSave = toEntity(command);
        User savedUser = userRepository.save(userToSave);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return toData(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserData> getUserById(Integer id) {
        log.debug("Fetching user with ID: {}", id);

        return userRepository.findById(id)
                .map(this::toData);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserData> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users");

        Page<User> userPage = userRepository.findAll(pageable);
        return toDataPage(userPage);
    }

    @Override
    public Optional<UserData> updateUser(Integer id, UpdateUserCommand command) {
        log.debug("Updating user with ID: {}", id);

        return userRepository.findById(id)
                .map(existingUser -> {
                    // Business validation - check if email is being updated and if it already exists
                    validateEmailUniquenessForUpdate(command.email(), existingUser.getEmail());

                    User updatedUser = toEntity(command, existingUser);
                    User savedUser = userRepository.save(updatedUser);

                    log.info("User updated successfully with ID: {}", savedUser.getId());
                    return toData(savedUser);
                });
    }

    @Override
    public boolean deleteUser(Integer id) {
        log.debug("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            return false;
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
        return true;
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email " + email + " already exists");
        }
    }

    private void validateEmailUniquenessForUpdate(String newEmail, String currentEmail) {
        if (newEmail != null &&
                !newEmail.equals(currentEmail) &&
                userRepository.existsByEmail(newEmail)) {
            throw new DuplicateResourceException("User with email " + newEmail + " already exists");
        }
    }

    private User toEntity(CreateUserCommand command) {
        if (command == null) {
            return null;
        }

        Instant now = Instant.now(); // UTC timestamp
        User user = User.builder()
                .firstName(command.firstName())
                .lastName(command.lastName())
                .email(command.email())
                .phone(command.phone())
                .build();

        user.setCreatedBy(command.createdBy());
        user.setUpdatedBy(command.createdBy());
        user.setCreatedDatetime(now);
        user.setUpdatedDatetime(now);

        return user;
    }

    User toEntity(UpdateUserCommand command, User existingUser) {
        if (command == null || existingUser == null) {
            return existingUser;
        }

        User updated = existingUser.toBuilder()
                .firstName(command.firstName() != null ? command.firstName() : existingUser.getFirstName())
                .lastName(command.lastName() != null ? command.lastName() : existingUser.getLastName())
                .email(command.email() != null ? command.email() : existingUser.getEmail())
                .phone(command.phone() != null ? command.phone() : existingUser.getPhone())
                .build();
        updated.setUpdatedBy(command.updatedBy());
        updated.setUpdatedDatetime(Instant.now());

        return updated;
    }

    private UserData toData(User entity) {
        if (entity == null) {
            return null;
        }

        return UserData.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdDatetime(entity.getCreatedDatetime())
                .updatedDatetime(entity.getUpdatedDatetime())
                .build();
    }

    private Page<UserData> toDataPage(Page<User> entityPage) {
        if (entityPage == null) {
            return null;
        }

        return entityPage.map(this::toData); // Spring's Page.map() handles transformation
    }
}
