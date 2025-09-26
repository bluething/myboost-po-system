package io.github.bluething.myboostposystem.rest;

import io.github.bluething.myboostposystem.common.TimezoneUtil;
import io.github.bluething.myboostposystem.domain.item.CreateItemCommand;
import io.github.bluething.myboostposystem.domain.item.ItemData;
import io.github.bluething.myboostposystem.domain.item.ItemService;
import io.github.bluething.myboostposystem.domain.item.UpdateItemCommand;
import io.github.bluething.myboostposystem.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
class ItemController {
    private final ItemService itemService;

    /**
     * Create new Item
     *
     * @param request Item creation request
     * @return Created item details
     */
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemCreateRequest request) {
        log.info("Creating item: name='{}'", request.name());

        CreateItemCommand itemDto = toCreateCommand(request);
        ItemData itemData = itemService.create(itemDto);
        ItemResponse response = toResponse(itemData);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all items with pagination support
     *
     * @param pageable Pagination parameters
     * @return Page of items
     */
    @GetMapping
    public ResponseEntity<Page<ItemResponse>> getItems(Pageable pageable) {
        log.info("Fetching all items with pagination: {}", pageable);

        Page<ItemData> itemDataPage = itemService.findAll(pageable);
        Page<ItemResponse> responsePage = itemDataPage.map(this::toResponse);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get item by ID
     *
     * @param id Item ID
     * @return Item details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable @Positive Integer id) {
        log.info("Fetching item with id={}", id);

        return itemService.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Item with ID " + id + " not found"));
    }

    /**
     * Update an existing item (full replace).
     *
     * @param id      Item ID to update
     * @param request ItemUpdateRequest payload
     * @return Updated ItemResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable Integer id,
                                                   @Valid @RequestBody ItemUpdateRequest request) {
        log.info("Updating item with ID: {}", id);
        // TODO: update and return updated response, or 404 if not found
        return itemService.update(id, toUpdateCommand(request))
                .map(this::toResponse)
                .map(response -> ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResourceNotFoundException("Item with ID " + id + " not found"));
    }

    /**
     * Delete item by ID.
     *
     * @param id Item ID to delete
     * @return 204 No Content if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Integer id) {
        log.info("Deleting item with ID: {}", id);

        boolean deleted = itemService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    CreateItemCommand toCreateCommand(ItemCreateRequest request) {
        if (request == null) {
            return null;
        }

        return new CreateItemCommand(request.name(),
                request.description(),
                request.price(),
                request.cost(),
                "SYSTEM");
    }
    UpdateItemCommand toUpdateCommand(ItemUpdateRequest request) {
        if (request == null) {
            return null;
        }

        return new UpdateItemCommand(request.name(),
                request.description(),
                request.price(),
                request.cost(),
                "SYSTEM");
    }

    ItemResponse toResponse(ItemData itemData) {
        if (itemData == null) {
            return null;
        }

        return new ItemResponse(itemData.id(),
                itemData.name(),
                itemData.description(),
                itemData.price(),
                itemData.cost(),
                itemData.createdBy(),
                itemData.updatedBy(),
                TimezoneUtil.toAppLocalDateTime(itemData.createdDatetime()),
                TimezoneUtil.toAppLocalDateTime(itemData.updatedDatetime()));
    }
}
