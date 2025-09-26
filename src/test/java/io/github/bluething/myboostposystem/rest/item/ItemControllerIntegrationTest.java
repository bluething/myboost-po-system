package io.github.bluething.myboostposystem.rest.item;

import io.github.bluething.myboostposystem.persistence.Item;
import io.github.bluething.myboostposystem.persistence.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@Testcontainers
class ItemControllerIntegrationTest {
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
    private ItemRepository itemRepository;

    private static final String BASE_URL = "/api/v1/items";
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + BASE_URL;
        itemRepository.deleteAll();
    }

    @Test
    void createItem_ShouldReturnCreatedItem_WhenValidRequest() {
        // Given
        ItemCreateRequest request = new ItemCreateRequest(
                "MacBook Pro M3",
                "Latest MacBook with M3 chip",
                25000000L,
                20000000L
        );

        // When
        ResponseEntity<ItemResponse> response = restTemplate.postForEntity(
                baseUrl,
                request,
                ItemResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("MacBook Pro M3");
        assertThat(response.getBody().description()).isEqualTo("Latest MacBook with M3 chip");
        assertThat(response.getBody().price()).isEqualTo(25000000);
        assertThat(response.getBody().cost()).isEqualTo(20000000);
        assertThat(response.getBody().createdBy()).isEqualTo("SYSTEM");
        assertThat(response.getBody().createdDatetime()).isNotNull();

        // Verify database persistence
        assertThat(itemRepository.count()).isEqualTo(1);
        Item savedItem = itemRepository.findAll().get(0);
        assertThat(savedItem.getName()).isEqualTo("MacBook Pro M3");
        assertThat(savedItem.getPrice()).isEqualTo(25000000L);
        assertThat(savedItem.getCost()).isEqualTo(20000000L);
    }

    @Test
    void createItem_ShouldReturnBadRequest_WhenInvalidData() {
        // Given
        ItemCreateRequest invalidRequest = new ItemCreateRequest(
                "", // Invalid: empty name
                null,
                -1000L, // Invalid: negative price
                null
        );

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                invalidRequest,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(itemRepository.count()).isEqualTo(0);
    }

    @Test
    void getItems_ShouldReturnPagedItems_WhenItemsExist() {
        // Given
        createTestItems();

        // When
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("page", 0)
                .queryParam("size", 2)
                .queryParam("sort", "name,asc")
                .toUriString();

        ResponseEntity<PageDto<ItemResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PageDto<ItemResponse> page = response.getBody();
        assertThat(page.page().totalElements()).isEqualTo(3);
        assertThat(page.page().totalPages()).isEqualTo(2);
        assertThat(page.page().number()).isEqualTo(0);

        List<ItemResponse> content = page.content();
        assertThat(content).hasSize(2);
        assertThat(content.get(0).name()).isEqualTo("Item A");
        assertThat(content.get(1).name()).isEqualTo("Item B");
    }

    @Test
    void getItems_ShouldReturnEmptyPage_WhenNoItems() {
        // When
        ResponseEntity<PageDto<ItemResponse>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PageDto<ItemResponse> pageData = response.getBody();
        assertThat(pageData.page().totalElements).isEqualTo(0);

        List<ItemResponse> content = pageData.content;
        assertThat(content).isEmpty();
    }

    @Test
    void getItemById_ShouldReturnItem_WhenItemExists() {
        // Given
        Item savedItem = createSingleTestItem();

        // When
        ResponseEntity<ItemResponse> response = restTemplate.getForEntity(
                baseUrl + "/{id}",
                ItemResponse.class,
                savedItem.getId()
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(savedItem.getId());
        assertThat(response.getBody().name()).isEqualTo("Test Item");
        assertThat(response.getBody().description()).isEqualTo("Test Description");
        assertThat(response.getBody().price()).isEqualTo(1000);
        assertThat(response.getBody().cost()).isEqualTo(800);
    }

    @Test
    void getItemById_ShouldReturnNotFound_WhenItemNotExists() {
        // Given
        Integer nonExistentId = 999;

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/{id}",
                String.class,
                nonExistentId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem_WhenItemExists() {
        // Given
        Item existingItem = createSingleTestItem();
        ItemUpdateRequest updateRequest = new ItemUpdateRequest(
                "Updated Item Name",
                "Updated description",
                1500L,
                1200L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ItemUpdateRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        // When
        ResponseEntity<ItemResponse> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.PUT,
                requestEntity,
                ItemResponse.class,
                existingItem.getId()
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(existingItem.getId());
        assertThat(response.getBody().name()).isEqualTo("Updated Item Name");
        assertThat(response.getBody().description()).isEqualTo("Updated description");
        assertThat(response.getBody().price()).isEqualTo(1500);
        assertThat(response.getBody().cost()).isEqualTo(1200);
        assertThat(response.getBody().updatedBy()).isEqualTo("SYSTEM");
        assertThat(response.getBody().updatedDatetime()).isNotNull();

        // Verify database update
        Item updatedItem = itemRepository.findById(existingItem.getId()).orElseThrow();
        assertThat(updatedItem.getName()).isEqualTo("Updated Item Name");
        assertThat(updatedItem.getPrice()).isEqualTo(1500L);
        assertThat(updatedItem.getCost()).isEqualTo(1200L);
        assertThat(updatedItem.getUpdatedBy()).isEqualTo("SYSTEM");
        assertThat(updatedItem.getUpdatedDatetime()).isNotNull();
    }

    @Test
    void updateItem_ShouldReturnNotFound_WhenItemNotExists() {
        // Given
        Integer nonExistentId = 999;
        ItemUpdateRequest updateRequest = new ItemUpdateRequest(
                "Updated Item",
                "Updated description",
                1500L,
                1200L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ItemUpdateRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.PUT,
                requestEntity,
                String.class,
                nonExistentId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateItem_ShouldReturnBadRequest_WhenInvalidData() {
        // Given
        Item existingItem = createSingleTestItem();
        ItemUpdateRequest invalidRequest = new ItemUpdateRequest(
                "", // Invalid: empty name
                null,
                -1000L, // Invalid: negative price
                null
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ItemUpdateRequest> requestEntity = new HttpEntity<>(invalidRequest, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.PUT,
                requestEntity,
                String.class,
                existingItem.getId()
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Verify item was not updated
        Item unchangedItem = itemRepository.findById(existingItem.getId()).orElseThrow();
        assertThat(unchangedItem.getName()).isEqualTo("Test Item"); // Original name
    }

    @Test
    void deleteItem_ShouldReturnNoContent_WhenItemExists() {
        // Given
        Item existingItem = createSingleTestItem();
        assertThat(itemRepository.existsById(existingItem.getId())).isTrue();

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                existingItem.getId()
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(itemRepository.existsById(existingItem.getId())).isFalse();
    }

    @Test
    void deleteItem_ShouldReturnNotFound_WhenItemNotExists() {
        // Given
        Integer nonExistentId = 999;

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                nonExistentId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void fullCrudFlow_ShouldWorkEndToEnd() {
        // 1. Create item
        ItemCreateRequest createRequest = new ItemCreateRequest(
                "Full CRUD Test Item",
                "Testing complete CRUD flow",
                2000L,
                1600L
        );

        ResponseEntity<ItemResponse> createResponse = restTemplate.postForEntity(
                baseUrl,
                createRequest,
                ItemResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        Integer itemId = createResponse.getBody().id();

        // 2. Get the created item
        ResponseEntity<ItemResponse> getResponse = restTemplate.getForEntity(
                baseUrl + "/{id}",
                ItemResponse.class,
                itemId
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().name()).isEqualTo("Full CRUD Test Item");

        // 3. Update the item
        ItemUpdateRequest updateRequest = new ItemUpdateRequest(
                "Updated CRUD Test Item",
                "Updated description",
                2500L,
                2000L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ItemUpdateRequest> updateEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<ItemResponse> updateResponse = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.PUT,
                updateEntity,
                ItemResponse.class,
                itemId
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().name()).isEqualTo("Updated CRUD Test Item");

        // 4. Verify the update
        ResponseEntity<ItemResponse> verifyResponse = restTemplate.getForEntity(
                baseUrl + "/{id}",
                ItemResponse.class,
                itemId
        );

        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(verifyResponse.getBody()).isNotNull();
        assertThat(verifyResponse.getBody().name()).isEqualTo("Updated CRUD Test Item");
        assertThat(verifyResponse.getBody().price()).isEqualTo(2500);

        // 5. Delete the item
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                itemId
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 6. Verify deletion
        ResponseEntity<String> finalGetResponse = restTemplate.getForEntity(
                baseUrl + "/{id}",
                String.class,
                itemId
        );

        assertThat(finalGetResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(itemRepository.count()).isEqualTo(0);
    }

    // Helper methods
    private void createTestItems() {
        Instant now = Instant.now();
        Item itemA = Item.builder()
                .name("Item A")
                .description("Description A")
                .price(1000L)
                .cost(800L).build();
        itemA.setCreatedBy("user1");
        itemA.setUpdatedBy("user1");
        itemA.setCreatedDatetime(now);
        itemA.setUpdatedDatetime(now);
        itemRepository.save(itemA);

        Item itemB = Item.builder()
                .name("Item B")
                .description("Description B")
                .price(2000L)
                .cost(1600L).build();
        itemB.setCreatedBy("user2");
        itemB.setUpdatedBy("user2");
        itemB.setCreatedDatetime(now);
        itemB.setUpdatedDatetime(now);
        itemRepository.save(itemB);

        Item itemC = Item.builder()
                .name("Item C")
                .description("Description C")
                .price(3000L)
                .cost(2400L).build();
        itemC.setCreatedBy("user3");
        itemC.setUpdatedBy("user3");
        itemC.setCreatedDatetime(now);
        itemC.setUpdatedDatetime(now);
        itemRepository.save(itemC);
    }

    private Item createSingleTestItem() {
        Instant now = Instant.now();
        Item itemA = Item.builder()
                .name("Test Item")
                .description("Test Description")
                .price(1000L)
                .cost(800L).build();
        itemA.setCreatedBy("user1");
        itemA.setUpdatedBy("user1");
        itemA.setCreatedDatetime(now);
        itemA.setUpdatedDatetime(now);
        return itemRepository.save(itemA);
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
