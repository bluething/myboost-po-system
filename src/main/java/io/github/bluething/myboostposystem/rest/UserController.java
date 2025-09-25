package io.github.bluething.myboostposystem.rest;

import io.github.bluething.myboostposystem.domain.CreateUserCommand;
import io.github.bluething.myboostposystem.domain.UpdateUserCommand;
import io.github.bluething.myboostposystem.domain.UserData;
import io.github.bluething.myboostposystem.domain.UserService;
import io.github.bluething.myboostposystem.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
class UserController {

    private final UserService userService;

    /**
     * Get all users with pagination support
     *
     * @param pageable Pagination parameters
     * @return Page of users
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination: {}", pageable);

        Page<UserData> userPage = userService.getAllUsers(pageable);
        Page<UserResponse> responsePage = toResponsePage(userPage);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get user by ID
     *
     * @param id User ID
     * @return User details
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        log.info("Fetching user with ID: {}", id);

        return userService.getUserById(id)
                .map(this::toResponse)
                .map(response -> ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found"));
    }

    /**
     * Create new user
     *
     * @param request User creation request
     * @return Created user details
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.email());

        UserData createdUser = userService.createUser(toCreateCommand(request));
        UserResponse response = toResponse(createdUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update existing user
     *
     * @param id User ID to update
     * @param request User update request
     * @return Updated user details
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        return userService.updateUser(id, toUpdateCommand(request))
                .map(this::toResponse)
                .map(response -> ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found"));
    }

    /**
     * Delete user by ID
     *
     * @param id User ID to delete
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        log.info("Deleting user with ID: {}", id);

        boolean deleted = userService.deleteUser(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    CreateUserCommand toCreateCommand(CreateUserRequest request) {
        if (request == null) {
            return null;
        }

        return CreateUserCommand.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .createdBy("SYSTEM")
                .build();
    }

    UpdateUserCommand toUpdateCommand(UpdateUserRequest request) {
        if (request == null) {
            return null;
        }

        return UpdateUserCommand.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .updatedBy("SYSTEM")
                .build();
    }

    UserResponse toResponse(UserData data) {
        if (data == null) {
            return null;
        }

        return new UserResponse(
                data.id(),
                data.firstName(),
                data.lastName(),
                data.email(),
                data.phone(),
                data.createdBy(),
                data.updatedBy(),
                data.createdDatetime(),
                data.updatedDatetime()
        );
    }

    private Page<UserResponse> toResponsePage(Page<UserData> dataPage) {
        if (dataPage == null) {
            return null;
        }

        return dataPage.map(this::toResponse);
    }
}
