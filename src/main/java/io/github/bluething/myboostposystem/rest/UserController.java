package io.github.bluething.myboostposystem.rest;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
class UserController {
    /**
     * Get all users with pagination support
     *
     * @param pageable Pagination parameters
     * @return Page of users
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination: {}", pageable);
        //TODO
        return ResponseEntity.ok().build();
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
        //TODO
        return ResponseEntity.ok().build();
    }

    /**
     * Create new user
     *
     * @param request User creation request
     * @return Created user details
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Creating new user with email: {}", request.email());
        //TODO
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);
        //TODO
        return ResponseEntity.ok().build();
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
        //TODO
        return ResponseEntity.noContent().build();
    }
}
