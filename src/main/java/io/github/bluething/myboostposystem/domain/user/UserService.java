package io.github.bluething.myboostposystem.domain.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {
    UserData createUser(CreateUserCommand command);

    Optional<UserData> getUserById(Integer id);

    Page<UserData> getAllUsers(Pageable pageable);

    Optional<UserData> updateUser(Integer id, UpdateUserCommand command);

    boolean deleteUser(Integer id);
}
