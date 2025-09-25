package io.github.bluething.myboostposystem.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bluething.myboostposystem.domain.CreateUserCommand;
import io.github.bluething.myboostposystem.domain.UpdateUserCommand;
import io.github.bluething.myboostposystem.domain.UserData;
import io.github.bluething.myboostposystem.domain.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private static final String BASE_URL = "/api/v1/users";
    private static final Instant FIXED_DATE =
            LocalDateTime.of(2024, 1, 1, 10, 0, 0)
                    .atZone(ZoneId.of("Asia/Jakarta"))
                    .toInstant();

    @Nested
    @DisplayName("GET /api/v1/users - Get All Users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return paginated users successfully")
        void shouldReturnPaginatedUsersSuccessfully() throws Exception {
            // Given
            List<UserData> users = createSampleUserDataList();
            Page<UserData> userPage = new PageImpl<>(users, PageRequest.of(0, 10), users.size());

            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].firstName").value("John"))
                    .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
                    .andExpect(jsonPath("$.content[0].email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(10));

            verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no users exist")
        void shouldReturnEmptyPageWhenNoUsersExist() throws Exception {
            // Given
            Page<UserData> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(emptyPage);

            // When & Then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(userService).getAllUsers(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id} - Get User By ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when found")
        void shouldReturnUserWhenFound() throws Exception {
            // Given
            UserData userData = createSampleUserData(1);
            when(userService.getUserById(1)).thenReturn(Optional.of(userData));

            // When & Then
            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.phone").value("1234567890"))
                    .andExpect(jsonPath("$.createdBy").value("SYSTEM"))
                    .andExpect(jsonPath("$.updatedBy").value("SYSTEM"));

            verify(userService).getUserById(1);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowResourceNotFoundExceptionWhenUserNotFound() throws Exception {
            // Given
            when(userService.getUserById(999)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(userService).getUserById(999);
        }

        @Test
        @DisplayName("Should return bad request for invalid ID format")
        void shouldReturnBadRequestForInvalidIdFormat() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL + "/invalid-id"))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).getUserById(anyInt());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully with valid request")
        void shouldCreateUserSuccessfullyWithValidRequest() throws Exception {
            // Given
            CreateUserRequest request = new CreateUserRequest(
                    "John", "Doe", "john.doe@example.com", "1234567890"
            );
            UserData createdUser = createSampleUserData(1);

            when(userService.createUser(any(CreateUserCommand.class))).thenReturn(createdUser);

            // When & Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.phone").value("1234567890"));

            verify(userService).createUser(any(CreateUserCommand.class));
        }

        @Test
        @DisplayName("Should return bad request when required fields are missing")
        void shouldReturnBadRequestWhenRequiredFieldsAreMissing() throws Exception {
            // Given
            CreateUserRequest invalidRequest = new CreateUserRequest(
                    null, null, null, null
            );

            // When & Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(CreateUserCommand.class));
        }

        @Test
        @DisplayName("Should return bad request when email format is invalid")
        void shouldReturnBadRequestWhenEmailFormatIsInvalid() throws Exception {
            // Given
            CreateUserRequest invalidRequest = new CreateUserRequest(
                    "John", "Doe", "invalid-email", "1234567890"
            );

            // When & Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(CreateUserCommand.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/{id} - Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() throws Exception {
            // Given
            UpdateUserRequest request = new UpdateUserRequest(
                    "Jane", "Smith", "jane.smith@example.com", "0987654321"
            );
            UserData updatedUser = UserData.builder()
                    .id(1)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .phone("0987654321")
                    .createdBy("SYSTEM")
                    .updatedBy("SYSTEM")
                    .createdDatetime(FIXED_DATE)
                    .updatedDatetime(FIXED_DATE.plus(Duration.ofHours(1)))
                    .build();

            when(userService.updateUser(eq(1), any(UpdateUserCommand.class)))
                    .thenReturn(Optional.of(updatedUser));

            // When & Then
            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.lastName").value("Smith"))
                    .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                    .andExpect(jsonPath("$.phone").value("0987654321"));

            verify(userService).updateUser(eq(1), any(UpdateUserCommand.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent user")
        void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentUser() throws Exception {
            // Given
            UpdateUserRequest request = new UpdateUserRequest(
                    "Jane", "Smith", "jane.smith@example.com", "0987654321"
            );
            when(userService.updateUser(eq(999), any(UpdateUserCommand.class)))
                    .thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(put(BASE_URL + "/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(userService).updateUser(eq(999), any(UpdateUserCommand.class));
        }

        @Test
        @DisplayName("Should return bad request when update request is invalid")
        void shouldReturnBadRequestWhenUpdateRequestIsInvalid() throws Exception {
            // Given
            UpdateUserRequest invalidRequest = new UpdateUserRequest(
                    null, null, "invalid-email", null
            );

            // When & Then
            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).updateUser(anyInt(), any(UpdateUserCommand.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id} - Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() throws Exception {
            // Given
            when(userService.deleteUser(1)).thenReturn(true);

            // When & Then
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(1);
        }

        @Test
        @DisplayName("Should return not found when deleting non-existent user")
        void shouldReturnNotFoundWhenDeletingNonExistentUser() throws Exception {
            // Given
            when(userService.deleteUser(999)).thenReturn(false);

            // When & Then
            mockMvc.perform(delete(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(userService).deleteUser(999);
        }

        @Test
        @DisplayName("Should return bad request for invalid ID format in delete")
        void shouldReturnBadRequestForInvalidIdFormatInDelete() throws Exception {
            // When & Then
            mockMvc.perform(delete(BASE_URL + "/invalid-id"))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).deleteUser(anyInt());
        }
    }

    // Helper methods for creating test data
    private UserData createSampleUserData(Integer id) {
        return UserData.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .createdBy("SYSTEM")
                .updatedBy("SYSTEM")
                .createdDatetime(FIXED_DATE)
                .updatedDatetime(FIXED_DATE)
                .build();
    }

    private List<UserData> createSampleUserDataList() {
        UserData user1 = createSampleUserData(1);
        UserData user2 = UserData.builder()
                .id(2)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("0987654321")
                .createdBy("SYSTEM")
                .updatedBy("SYSTEM")
                .createdDatetime(FIXED_DATE)
                .updatedDatetime(FIXED_DATE)
                .build();

        return List.of(user1, user2);
    }

}