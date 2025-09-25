package io.github.bluething.myboostposystem.rest.po;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bluething.myboostposystem.domain.po.*;
import io.github.bluething.myboostposystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseOrderController.class)
class PurchaseOrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PurchaseOrderService purchaseOrderService;

    private static final Instant FIXED_DATE =
            LocalDateTime.of(2024, 1, 1, 10, 0, 0)
                    .atZone(ZoneId.of("Asia/Jakarta"))
                    .toInstant();

    @Test
    @DisplayName("GET /purchase-orders - Should return paginated purchase orders")
    void getPurchaseOrders_ShouldReturnPaginatedResults() throws Exception {
        // Given
        List<CreatePODetail> details = List.of(
                new CreatePODetail(1, 10, 100L, 80L),
                new CreatePODetail(2, 5, 200L, 150L)
        );

        POData poData = new POData(
                1,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                "Test Purchase Order",
                1500L,
                1150L,
                details,
                "SYSTEM",
                "SYSTEM",
                FIXED_DATE,
                FIXED_DATE
        );

        Page<POData> mockPage = new PageImpl<>(List.of(poData));
        when(purchaseOrderService.findAll(any(Pageable.class))).thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/v1/purchase-orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].description").value("Test Purchase Order"))
                .andExpect(jsonPath("$.content[0].totalPrice").value(1500))
                .andExpect(jsonPath("$.content[0].totalCost").value(1150))
                .andExpect(jsonPath("$.content[0].details").isArray())
                .andExpect(jsonPath("$.content[0].details[0].id").value(1))
                .andExpect(jsonPath("$.content[0].details[0].itemQty").value(10))
                .andExpect(jsonPath("$.content[0].details[1].id").value(2))
                .andExpect(jsonPath("$.content[0].details[1].itemQty").value(5));

        verify(purchaseOrderService).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /purchase-orders - Should return empty page when no data")
    void getPurchaseOrders_ShouldReturnEmptyPage() throws Exception {
        // Given
        Page<POData> emptyPage = new PageImpl<>(List.of());
        when(purchaseOrderService.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/v1/purchase-orders"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /purchase-orders/{id} - Should return purchase order when found")
    void getPurchaseOrderById_ShouldReturnPO() throws Exception {
        // Given
        List<CreatePODetail> details = List.of(new CreatePODetail(1, 10, 100L, 80L));
        POData poData = new POData(
                1,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                "Test Purchase Order",
                1000L,
                800L,
                details,
                "SYSTEM",
                "SYSTEM",
                FIXED_DATE,
                FIXED_DATE
        );

        when(purchaseOrderService.findById(1)).thenReturn(Optional.of(poData));

        // When & Then
        mockMvc.perform(get("/api/v1/purchase-orders/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Purchase Order"))
                .andExpect(jsonPath("$.totalPrice").value(1000))
                .andExpect(jsonPath("$.totalCost").value(800))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[0].id").value(1));

        verify(purchaseOrderService).findById(1);
    }

    @Test
    @DisplayName("GET /purchase-orders/{id} - Should return 404 when not found")
    void getPurchaseOrderById_ShouldReturn404WhenNotFound() throws Exception {
        // Given
        when(purchaseOrderService.findById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/purchase-orders/999"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(purchaseOrderService).findById(999);
    }

    @Test
    @DisplayName("POST /purchase-orders - Should create purchase order successfully")
    void createPurchaseOrder_ShouldCreateSuccessfully() throws Exception {
        // Given
        List<PoDetailRequest> detailRequests = List.of(
                new PoDetailRequest(1, 10, 100L, 80L),
                new PoDetailRequest(2, 5, 200L, 150L)
        );

        CreatePORequest request = new CreatePORequest(
                LocalDateTime.of(2024, 1, 15, 10, 0),
                "New Purchase Order",
                1500L,
                1150L,
                detailRequests
        );

        List<CreatePODetail> responseDetails = List.of(
                new CreatePODetail(1, 10, 100L, 80L),
                new CreatePODetail(2, 5, 200L, 150L)
        );

        POData createdPO = new POData(
                1,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                "New Purchase Order",
                1500L,
                1150L,
                responseDetails,
                "SYSTEM",
                "SYSTEM",
                FIXED_DATE,
                FIXED_DATE
        );

        when(purchaseOrderService.create(any(CreatePOCommand.class))).thenReturn(createdPO);

        // When & Then
        mockMvc.perform(post("/api/v1/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("New Purchase Order"))
                .andExpect(jsonPath("$.totalPrice").value(1500))
                .andExpect(jsonPath("$.totalCost").value(1150))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isNotEmpty());

        verify(purchaseOrderService).create(any(CreatePOCommand.class));
    }

    @Test
    @DisplayName("POST /purchase-orders - Should return 400 for invalid request")
    void createPurchaseOrder_ShouldReturn400ForInvalidRequest() throws Exception {
        // Given - Request with missing required fields
        String invalidJson = """
                {
                    "description": null,
                    "totalPrice": null,
                    "totalCost": null
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(purchaseOrderService, never()).create(any());
    }

    @Test
    @DisplayName("PUT /purchase-orders/{id} - Should update purchase order successfully")
    void updatePurchaseOrder_ShouldUpdateSuccessfully() throws Exception {
        // Given
        List<PoDetailRequest> detailRequests = List.of(
                new PoDetailRequest(1, 15, 120L, 100L)
        );

        UpdatePORequest request = new UpdatePORequest(
                LocalDateTime.of(2024, 1, 15, 10, 0),
                "Updated Purchase Order",
                1800L,
                1500L,
                detailRequests
        );

        List<CreatePODetail> responseDetails = List.of(
                new CreatePODetail(1, 15, 120L, 100L)
        );

        POData updatedPO = new POData(
                1,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                "Updated Purchase Order",
                1800L,
                1500L,
                responseDetails,
                "SYSTEM",
                "SYSTEM",
                FIXED_DATE,
                FIXED_DATE
        );

        when(purchaseOrderService.update(eq(1), any(UpdatePOCommand.class))).thenReturn(updatedPO);

        // When & Then
        mockMvc.perform(put("/api/v1/purchase-orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Updated Purchase Order"))
                .andExpect(jsonPath("$.totalPrice").value(1800))
                .andExpect(jsonPath("$.totalCost").value(1500));

        verify(purchaseOrderService).update(eq(1), any(UpdatePOCommand.class));
    }

    @Test
    @DisplayName("PUT /purchase-orders/{id} - Should return 400 for invalid request")
    void updatePurchaseOrder_ShouldReturn400ForInvalidRequest() throws Exception {
        // Given
        String invalidJson = """
                {
                    "description": "",
                    "totalPrice": -100,
                    "totalCost": -50
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/purchase-orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(purchaseOrderService, never()).update(eq(1), any());
    }

    @Test
    @DisplayName("DELETE /purchase-orders/{id} - Should delete purchase order successfully")
    void deletePurchaseOrder_ShouldDeleteSuccessfully() throws Exception {

        // When & Then
        mockMvc.perform(delete("/api/v1/purchase-orders/1"))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(purchaseOrderService).deleteById(1);
    }

    @Test
    @DisplayName("POST /purchase-orders - Should handle empty details list")
    void createPurchaseOrder_ShouldHandleEmptyDetails() throws Exception {
        // Given
        CreatePORequest request = new CreatePORequest(
                LocalDateTime.of(2024, 1, 15, 10, 0),
                "PO with no items",
                0L,
                0L,
                List.of()
        );

        POData createdPO = new POData(
                1,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                "PO with no items",
                0L,
                0L,
                Collections.emptyList(),
                "SYSTEM",
                "SYSTEM",
                FIXED_DATE,
                FIXED_DATE
        );

        when(purchaseOrderService.create(any(CreatePOCommand.class))).thenReturn(createdPO);

        // When & Then
        mockMvc.perform(post("/api/v1/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle service layer exceptions")
    void getPurchaseOrders_ShouldHandleServiceException() throws Exception {
        // Given
        when(purchaseOrderService.findAll(any(Pageable.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        mockMvc.perform(get("/api/v1/purchase-orders"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException properly")
    void getPurchaseOrderById_ShouldHandleResourceNotFoundException() throws Exception {
        // Given
        when(purchaseOrderService.findById(999))
                .thenThrow(new ResourceNotFoundException("PO not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/purchase-orders/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}