package io.github.bluething.myboostposystem.rest.user;

import io.github.bluething.myboostposystem.persistence.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@Testcontainers
class UserControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/users";
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/v1/users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully with valid data")
        void shouldCreateUserSuccessfully() {
            // Given
            CreateUserRequest request = new CreateUserRequest(
                    "John",
                    "Doe",
                    "john.doe@example.com",
                    "123456789"
            );

            // When
            ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                    baseUrl,
                    request,
                    UserResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            UserResponse user = response.getBody();
            assertThat(user.id()).isNotNull();
            assertThat(user.firstName()).isEqualTo("John");
            assertThat(user.lastName()).isEqualTo("Doe");
            assertThat(user.email()).isEqualTo("john.doe@example.com");
            assertThat(user.phone()).isEqualTo("123456789");
            assertThat(user.createdBy()).isEqualTo("SYSTEM");
            assertThat(user.createdDatetime()).isNotNull();

            // Verify in database
            assertThat(userRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return 400 when creating user with invalid data")
        void shouldReturn400ForInvalidData() {
            // Given - Empty first name
            CreateUserRequest request = new CreateUserRequest(
                    "",
                    "Doe",
                    "invalid-email",
                    "123456789"
            );

            // When
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl,
                    request,
                    Map.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(userRepository.count()).isZero();
        }

        @Test
        @DisplayName("Should return 409 when creating user with duplicate email")
        void shouldReturn409ForDuplicateEmail() {
            // Given - Create first user
            CreateUserRequest firstRequest = new CreateUserRequest(
                    "John",
                    "Doe",
                    "duplicate@example.com",
                    "123456789"
            );
            restTemplate.postForEntity(baseUrl, firstRequest, UserResponse.class);

            // When - Try to create second user with same email
            CreateUserRequest duplicateRequest = new CreateUserRequest(
                    "Jane",
                    "Smith",
                    "duplicate@example.com",
                    "987654321"
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl,
                    duplicateRequest,
                    Map.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(userRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users - Get Users")
    class GetUsersTests {

        @Test
        @DisplayName("Should get all users with pagination")
        void shouldGetAllUsersWithPagination() {
            // Given - Create multiple users
            for (int i = 1; i <= 5; i++) {
                CreateUserRequest request = new CreateUserRequest(
                        "User" + i,
                        "Test" + i,
                        "user" + i + "@example.com",
                        "12345678" + i
                );
                restTemplate.postForEntity(baseUrl, request, UserResponse.class);
            }

            // When
            String url = baseUrl + "?page=0&size=3&sort=firstName,asc";
            ResponseEntity<PageDto<UserResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageDto<UserResponse>>() {}
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PageDto<UserResponse> page = response.getBody();
            assertThat(page).isNotNull();
            Assertions.assertThat(page.content()).hasSize(3);
            assertThat(page.page().totalElements()).isEqualTo(5);
            assertThat(page.page().totalPages()).isEqualTo(2);
            assertThat(page.page().number()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return empty page when no users exist")
        void shouldReturnEmptyPageWhenNoUsers() {
            // When
            String url = baseUrl + "?page=0&size=10";
            ResponseEntity<PageDto<UserResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageDto<UserResponse>>() {}
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PageDto<UserResponse> page = response.getBody();
            assertThat(page).isNotNull();
            Assertions.assertThat(page.content()).isEmpty();
            assertThat(page.page().totalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id} - Get User by ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() {
            // Given - Create a user
            CreateUserRequest createRequest = new CreateUserRequest(
                    "Alice",
                    "Johnson",
                    "alice.johnson@example.com",
                    "555123456"
            );
            ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                    baseUrl,
                    createRequest,
                    UserResponse.class
            );
            Integer userId = createResponse.getBody().id();

            // When
            String url = baseUrl + "/" + userId;
            ResponseEntity<UserResponse> response = restTemplate.getForEntity(
                    url,
                    UserResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            UserResponse user = response.getBody();
            assertThat(user.id()).isEqualTo(userId);
            assertThat(user.firstName()).isEqualTo("Alice");
            assertThat(user.lastName()).isEqualTo("Johnson");
            assertThat(user.email()).isEqualTo("alice.johnson@example.com");
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() {
            // When
            String url = baseUrl + "/999";
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    url,
                    Map.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/{id} - Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // Given - Create a user
            CreateUserRequest createRequest = new CreateUserRequest(
                    "Bob",
                    "Wilson",
                    "bob.wilson@example.com",
                    "999888777"
            );
            ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                    baseUrl,
                    createRequest,
                    UserResponse.class
            );
            Integer userId = createResponse.getBody().id();

            // When - Update the user
            UpdateUserRequest updateRequest = new UpdateUserRequest(
                    "Robert",
                    "Wilson-Smith",
                    "robert.wilson@example.com",
                    "111222333"
            );
            String url = baseUrl + "/" + userId;
            HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(updateRequest);
            ResponseEntity<UserResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    UserResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            UserResponse updatedUser = response.getBody();
            assertThat(updatedUser.id()).isEqualTo(userId);
            assertThat(updatedUser.firstName()).isEqualTo("Robert");
            assertThat(updatedUser.lastName()).isEqualTo("Wilson-Smith");
            assertThat(updatedUser.email()).isEqualTo("robert.wilson@example.com");
            assertThat(updatedUser.phone()).isEqualTo("111222333");
            assertThat(updatedUser.updatedDatetime()).isAfter(updatedUser.createdDatetime());
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent user")
        void shouldReturn404WhenUpdatingNonExistentUser() {
            // Given
            UpdateUserRequest updateRequest = new UpdateUserRequest(
                    "Ghost",
                    "User",
                    "ghost@example.com",
                    "000000000"
            );

            // When
            String url = baseUrl + "/999";
            HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(updateRequest);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 409 when updating with duplicate email")
        void shouldReturn409ForDuplicateEmailOnUpdate() {
            // Given - Create two users
            CreateUserRequest user1Request = new CreateUserRequest(
                    "User",
                    "One",
                    "user1@example.com",
                    "111111111"
            );
            ResponseEntity<UserResponse> user1Response = restTemplate.postForEntity(
                    baseUrl, user1Request, UserResponse.class);

            CreateUserRequest user2Request = new CreateUserRequest(
                    "User",
                    "Two",
                    "user2@example.com",
                    "222222222"
            );
            ResponseEntity<UserResponse> user2Response = restTemplate.postForEntity(
                    baseUrl, user2Request, UserResponse.class);

            // When - Try to update user2 with user1's email
            UpdateUserRequest updateRequest = new UpdateUserRequest(
                    "User",
                    "Two",
                    "user1@example.com",
                    "222222222"
            );
            String url = baseUrl + "/" + user2Response.getBody().id();
            HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(updateRequest);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id} - Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            // Given - Create a user
            CreateUserRequest createRequest = new CreateUserRequest(
                    "ToDelete",
                    "User",
                    "todelete@example.com",
                    "555666777"
            );
            ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                    baseUrl,
                    createRequest,
                    UserResponse.class
            );
            Integer userId = createResponse.getBody().id();

            // When
            String url = baseUrl + "/" + userId;
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(userRepository.existsById(userId)).isFalse();

            // Verify user is deleted by trying to get it
            ResponseEntity<Map> getResponse = restTemplate.getForEntity(url, Map.class);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent user")
        void shouldReturn404WhenDeletingNonExistentUser() {
            // When
            String url = baseUrl + "/999";
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("End-to-End Workflow Tests")
    class EndToEndWorkflowTests {

        @Test
        @DisplayName("Should handle multiple users and pagination correctly")
        void shouldHandleMultipleUsersAndPagination() {
            // Given - Create 15 users
            for (int i = 1; i <= 15; i++) {
                CreateUserRequest request = new CreateUserRequest(
                        "User" + String.format("%02d", i),
                        "Test",
                        "user" + i + "@example.com",
                        "555000" + String.format("%03d", i)
                );
                ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                        baseUrl, request, UserResponse.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            }

            // When - Get first page
            String url1 = baseUrl + "?page=0&size=5&sort=firstName,asc";
            ResponseEntity<PageDto<UserResponse>> page1Response = restTemplate.exchange(
                    url1, HttpMethod.GET, null,
                    new ParameterizedTypeReference<PageDto<UserResponse>>() {}
            );

            // Then - Verify first page
            PageDto<UserResponse> page1 = page1Response.getBody();
            Assertions.assertThat(page1.content()).hasSize(5);
            assertThat(page1.page().totalElements()).isEqualTo(15);
            assertThat(page1.page().totalPages()).isEqualTo(3);
            assertThat(page1.page().number()).isEqualTo(0);

            // Second page
            String url2 = baseUrl + "?page=1&size=5&sort=firstName,asc";
            ResponseEntity<PageDto<UserResponse>> page2Response = restTemplate.exchange(
                    url2, HttpMethod.GET, null,
                    new ParameterizedTypeReference<PageDto<UserResponse>>() {}
            );
            PageDto<UserResponse> page2 = page2Response.getBody();
            Assertions.assertThat(page2.content()).hasSize(5);
            assertThat(page2.page().number()).isEqualTo(1);

            // Last page
            String url3 = baseUrl + "?page=2&size=5&sort=firstName,asc";
            ResponseEntity<PageDto<UserResponse>> page3Response = restTemplate.exchange(
                    url3, HttpMethod.GET, null,
                    new ParameterizedTypeReference<PageDto<UserResponse>>() {}
            );
            PageDto<UserResponse> page3 = page3Response.getBody();
            Assertions.assertThat(page3.content()).hasSize(5);
            assertThat(page3.page().number()).isEqualTo(2);
        }
    }

    record PageDto<T>(
            List<T> content,
            PageMetadata page
    ) {
        public record PageMetadata(
                int size,
                long totalElements,
                int totalPages,
                int number
        ) {}
    }

}