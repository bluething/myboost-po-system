package io.github.bluething.myboostposystem.rest.po;

import io.github.bluething.myboostposystem.persistence.Item;
import io.github.bluething.myboostposystem.persistence.ItemRepository;
import io.github.bluething.myboostposystem.persistence.PurchaseOrderHeaderRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PurchaseOrderControllerIntegrationTest {
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
    private PurchaseOrderHeaderRepository poHeaderRepository;

    @Autowired
    private ItemRepository itemRepository;

    private static Item testItem1;
    private static Item testItem2;
    private static Integer createdPOId;
    private static final Instant FIXED_DATE =
            LocalDateTime.of(2024, 1, 1, 10, 0, 0)
                    .atZone(ZoneId.of("Asia/Jakarta"))
                    .toInstant();

    @BeforeEach
    void setUp() {
        // Create test data only once
        if (testItem1 == null) {
            setupTestData();
        }
    }

    private void setupTestData() {

        // Create test items
        testItem1 = Item.builder()
                .name("Test Item 1")
                .description("Test Item 1 Description")
                .price(100L)
                .cost(80L)
                .build();
        testItem1.setCreatedBy("SYSTEM");
        testItem1.setUpdatedBy("SYSTEM");
        testItem1.setCreatedDatetime(FIXED_DATE);
        testItem1.setUpdatedDatetime(FIXED_DATE);
        testItem1 = itemRepository.save(testItem1);

        testItem2 = Item.builder()
                .name("Test Item 2")
                .description("Test Item 2 Description")
                .price(200L)
                .cost(160L)
                .build();
        testItem2.setCreatedBy("SYSTEM");
        testItem2.setUpdatedBy("SYSTEM");
        testItem2.setCreatedDatetime(FIXED_DATE);
        testItem2.setUpdatedDatetime(FIXED_DATE);
        testItem2 = itemRepository.save(testItem2);
    }


    @Test
    @Order(1)
    @DisplayName("Should create purchase order successfully")
    void shouldCreatePurchaseOrderSuccessfully() throws Exception {
        // Given
        var request = CreatePORequest.builder()
                .datetime(LocalDateTime.now())
                .description("Test Purchase Order")
                .totalPrice(400L)
                .totalCost(320L)
                .details(List.of(
                        new PoDetailRequest(testItem1.getId(), 2, 100L, 80L),
                        new PoDetailRequest(testItem2.getId(), 1, 200L, 160L)
                ))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreatePORequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<Response> response = restTemplate.exchange(
                "/api/v1/purchase-orders",
                HttpMethod.POST,
                entity,
                Response.class
        );

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());

        Response responseBody = response.getBody();
        Assertions.assertNotNull(responseBody.id());
        Assertions.assertEquals("Test Purchase Order", responseBody.description());
        Assertions.assertEquals(400, responseBody.totalPrice());
        Assertions.assertEquals(320, responseBody.totalCost());
        Assertions.assertEquals("SYSTEM", responseBody.createdBy());
        Assertions.assertEquals(2, responseBody.details().size());

        // Verify first detail
        DetailResponse detail1 = responseBody.details().get(0);
        Assertions.assertEquals(testItem1.getId(), detail1.id());
        Assertions.assertEquals(2, detail1.itemQty());
        Assertions.assertEquals(100, detail1.itemPrice());
        Assertions.assertEquals(80, detail1.itemCost());

        // Verify second detail
        DetailResponse detail2 = responseBody.details().get(1);
        Assertions.assertEquals(testItem2.getId(), detail2.id());
        Assertions.assertEquals(1, detail2.itemQty());
        Assertions.assertEquals(200, detail2.itemPrice());
        Assertions.assertEquals(160, detail2.itemCost());

        // Store created PO ID for subsequent tests
        createdPOId = responseBody.id();

        // Verify data is persisted in database
        var savedPO = poHeaderRepository.findById(createdPOId);
        Assertions.assertTrue(savedPO.isPresent());
        Assertions.assertEquals("Test Purchase Order", savedPO.get().getDescription());
    }

    @Test
    @Order(2)
    @DisplayName("Should get purchase order by ID successfully")
    void shouldGetPurchaseOrderByIdSuccessfully() throws Exception {
        // Given - Create a PO first
        createTestPurchaseOrder();

        // When
        ResponseEntity<Response> response = restTemplate.getForEntity(
                "/api/v1/purchase-orders/{id}",
                Response.class,
                createdPOId
        );

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());

        Response responseBody = response.getBody();
        Assertions.assertEquals(createdPOId, responseBody.id());
        Assertions.assertEquals("Test Purchase Order", responseBody.description());
        Assertions.assertEquals(400, responseBody.totalPrice());
        Assertions.assertEquals(320, responseBody.totalCost());
        Assertions.assertEquals(2, responseBody.details().size());
    }

    @Test
    @Order(3)
    @DisplayName("Should get all purchase orders with pagination")
    void shouldGetAllPurchaseOrdersWithPagination() throws Exception {
        // Given - Create multiple POs
        createTestPurchaseOrder();
        createAnotherTestPurchaseOrder();

        // When
        ResponseEntity<PagedResponse> response = restTemplate.exchange(
                "/api/v1/purchase-orders?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedResponse>() {}
        );

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());

        PagedResponse pagedResponse = response.getBody();
        Assertions.assertNotNull(pagedResponse.content);
        Assertions.assertTrue(pagedResponse.content.size() >= 2);
        Assertions.assertNotNull(pagedResponse.pageable);
        Assertions.assertTrue(pagedResponse.totalElements >= 2);
        Assertions.assertTrue(pagedResponse.totalPages >= 1);
        Assertions.assertTrue(pagedResponse.first);
        Assertions.assertEquals(10, pagedResponse.size);
    }

    @Test
    @Order(4)
    @DisplayName("Should delete purchase order successfully")
    void shouldDeletePurchaseOrderSuccessfully() throws Exception {
        // Given - Create a PO first
        createTestPurchaseOrder();

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/purchase-orders/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                createdPOId
        );

        // Then
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify deletion in database
        var deletedPO = poHeaderRepository.findById(createdPOId);
        Assertions.assertTrue(deletedPO.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("Should return 404 when purchase order not found")
    void shouldReturn404WhenPurchaseOrderNotFound() throws Exception {
        // Given - Non-existent PO ID
        int nonExistentId = 99999;

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/purchase-orders/{id}",
                String.class,
                nonExistentId
        );

        // Then
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("Should return 400 for invalid create request")
    void shouldReturn400ForInvalidCreateRequest() throws Exception {
        // Given - Invalid request (missing required fields)
        var invalidRequest = CreatePORequest.builder()
                .datetime(null) // Invalid: null datetime
                .description("") // Invalid: empty description
                .totalPrice(-100L) // Invalid: negative price
                .totalCost(0L)
                .details(List.of()) // Invalid: empty details
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreatePORequest> entity = new HttpEntity<>(invalidRequest, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/purchase-orders",
                HttpMethod.POST,
                entity,
                String.class
        );

        // Then
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private void createTestPurchaseOrder() throws Exception {
        if (createdPOId == null) {
            var request = CreatePORequest.builder()
                    .datetime(LocalDateTime.now())
                    .description("Test Purchase Order")
                    .totalPrice(300L)
                    .totalCost(240L)
                    .details(List.of(
                            new PoDetailRequest(testItem1.getId(), 3, 100L, 80L)
                    ))
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreatePORequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Response> response = restTemplate.exchange(
                    "/api/v1/purchase-orders",
                    HttpMethod.POST,
                    entity,
                    Response.class
            );

            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assertions.assertNotNull(response.getBody());
            createdPOId = response.getBody().id();
        }
    }
    private void createAnotherTestPurchaseOrder() throws Exception {
        var request = CreatePORequest.builder()
                .datetime(LocalDateTime.now())
                .description("Another Test Purchase Order")
                .totalPrice(200L)
                .totalCost(160L)
                .details(List.of(
                        new PoDetailRequest(testItem2.getId(), 1, 200L, 160L)
                ))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreatePORequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Response> response = restTemplate.exchange(
                "/api/v1/purchase-orders",
                HttpMethod.POST,
                entity,
                Response.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    public static class PagedResponse {
        public List<Response> content;
        public Map<String, Object> pageable;
        public int totalElements;
        public int totalPages;
        public boolean first;
        public boolean last;
        public int size;
        public int number;
        public Map<String, Object> sort;
        public int numberOfElements;
        public boolean empty;

        // Default constructor for Jackson
        public PagedResponse() {}
    }
}
