package io.github.bluething.myboostposystem.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/items")
@Slf4j
class ItemController {

    /**
     * Create new Item
     *
     * @param request Item creation request
     * @return Created item details
     */
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemCreateRequest request) {
        log.info("Creating item: name='{}'", request.name());
        //TODO
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
        //TODO
        return ResponseEntity.ok().build();
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
        //TODO
        return ResponseEntity.ok().build();
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
        return ResponseEntity.notFound().build();
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
        //TODO
        return ResponseEntity.noContent().build();
    }
}
