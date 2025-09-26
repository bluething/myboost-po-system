package io.github.bluething.myboostposystem.rest.user;

import io.github.bluething.myboostposystem.domain.user.CreateUserCommand;
import io.github.bluething.myboostposystem.domain.user.UpdateUserCommand;
import io.github.bluething.myboostposystem.domain.user.UserData;
import io.github.bluething.myboostposystem.domain.user.UserService;
import io.github.bluething.myboostposystem.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * @return Page of users
     */
    @Operation(
            summary = "Get all users with pagination",
            description = "Retrieve a paginated list of users ordered by ID"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@Parameter(description = "Page number (0-based)", example = "0")
                                                              @RequestParam(defaultValue = "0") @Min(0) Integer page,

                                                          @Parameter(description = "Page size", example = "10")
                                                              @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        log.info("Getting users - page: {}, size: {}", page, size);

        Sort defaultSort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, size, defaultSort);

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
