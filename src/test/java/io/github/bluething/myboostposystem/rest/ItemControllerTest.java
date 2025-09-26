package io.github.bluething.myboostposystem.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bluething.myboostposystem.domain.item.CreateItemCommand;
import io.github.bluething.myboostposystem.domain.item.ItemData;
import io.github.bluething.myboostposystem.domain.item.ItemService;
import io.github.bluething.myboostposystem.domain.item.UpdateItemCommand;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/items";
    private static final Instant FIXED_DATE =
            LocalDateTime.of(2024, 1, 1, 10, 0, 0)
                    .atZone(ZoneId.of("Asia/Jakarta"))
                    .toInstant();

    @Test
    void createItem_ShouldReturnCreatedItem_WhenValidRequest() throws Exception {
        // Given
        ItemCreateRequest request = new ItemCreateRequest(
                "Test Item",
                "Test Description",
                1000L,
                800L
        );

        ItemData createdItem = new ItemData(
                1,
                "Test Item",
                "Test Description",
                1000L,
                800L,
                "SYSTEM",
                null,
                FIXED_DATE,
                null
        );

        when(itemService.create(any(CreateItemCommand.class))).thenReturn(createdItem);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.cost").value(800));

        verify(itemService).create(any(CreateItemCommand.class));
    }

    @Test
    void createItem_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        ItemCreateRequest invalidRequest = new ItemCreateRequest(
                "", // Invalid: empty name
                null,
                -100L, // Invalid: negative price
                null
        );

        // When & Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).create(any());
    }

    @Test
    void getItems_ShouldReturnPagedItems_WhenCalled() throws Exception {
        // Given
        ItemData item1 = new ItemData(1, "Item 1", "Desc 1", 1000L, 800L, "user1", null, FIXED_DATE, null);
        ItemData item2 = new ItemData(2, "Item 2", "Desc 2", 2000L, 1500L, "user2", null, FIXED_DATE, null);

        Page<ItemData> itemPage = new PageImpl<>(List.of(item1, item2), PageRequest.of(0, 10), 2);

        when(itemService.findAll(any(Pageable.class))).thenReturn(itemPage);

        // When & Then
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Item 1"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Item 2"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10));

        verify(itemService).findAll(any(Pageable.class));
    }

    @Test
    void getItemById_ShouldReturnItem_WhenItemExists() throws Exception {
        // Given
        Integer itemId = 1;
        ItemData itemData = new ItemData(
                itemId,
                "Test Item",
                "Test Description",
                1000L,
                800L,
                "testuser",
                null,
                FIXED_DATE,
                null
        );

        when(itemService.findById(itemId)).thenReturn(Optional.of(itemData));

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(itemService).findById(itemId);
    }

    @Test
    void getItemById_ShouldThrowResourceNotFoundException_WhenItemNotExists() throws Exception {
        // Given
        Integer itemId = 999;
        when(itemService.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", itemId))
                .andExpect(status().isNotFound());

        verify(itemService).findById(itemId);
    }

    @Test
    void getItemById_ShouldReturnBadRequest_WhenInvalidId() throws Exception {
        // Given
        String invalidId = "abc";

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", invalidId))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).findById(any());
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem_WhenItemExists() throws Exception {
        // Given
        Integer itemId = 1;
        ItemUpdateRequest request = new ItemUpdateRequest(
                "Updated Item",
                "Updated Description",
                1500L,
                1200L
        );

        ItemData updatedItem = new ItemData(
                itemId,
                "Updated Item",
                "Updated Description",
                1500L,
                1200L,
                "creator",
                "SYSTEM",
                FIXED_DATE.minus(1, ChronoUnit.DAYS),
                FIXED_DATE
        );

        when(itemService.update(eq(itemId), any(UpdateItemCommand.class))).thenReturn(Optional.of(updatedItem));

        // When & Then
        mockMvc.perform(put(BASE_URL + "/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.price").value(1500))
                .andExpect(jsonPath("$.cost").value(1200));

        verify(itemService).update(eq(itemId), any(UpdateItemCommand.class));
    }

    @Test
    void updateItem_ShouldThrowResourceNotFoundException_WhenItemNotExists() throws Exception {
        // Given
        Integer itemId = 999;
        ItemUpdateRequest request = new ItemUpdateRequest(
                "Updated Item",
                "Updated Description",
                1500L,
                1200L
        );

        when(itemService.update(eq(itemId), any(UpdateItemCommand.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put(BASE_URL + "/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(itemService).update(eq(itemId), any(UpdateItemCommand.class));
    }

    @Test
    void updateItem_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        Integer itemId = 1;
        ItemUpdateRequest invalidRequest = new ItemUpdateRequest(
                "", // Invalid: empty name
                null,
                -100L, // Invalid: negative price
                null
        );

        // When & Then
        mockMvc.perform(put(BASE_URL + "/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).update(any(), any());
    }

    @Test
    void deleteItem_ShouldReturnNoContent_WhenItemDeleted() throws Exception {
        // Given
        Integer itemId = 1;
        when(itemService.delete(itemId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/{id}", itemId))
                .andExpect(status().isNoContent());

        verify(itemService).delete(itemId);
    }

    @Test
    void deleteItem_ShouldReturnNotFound_WhenItemNotExists() throws Exception {
        // Given
        Integer itemId = 999;
        when(itemService.delete(itemId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/{id}", itemId))
                .andExpect(status().isNotFound());

        verify(itemService).delete(itemId);
    }
}