package io.github.bluething.myboostposystem.rest;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@Slf4j
class PurchaseOrderController {

    /**
     * Get all po with pagination support
     *
     * @param pageable Pagination parameters
     * @return Page of pos
     */
    @GetMapping
    public ResponseEntity<Page<PoResponse>> getPurchaseOrders(Pageable pageable) {
        log.info("Listing POs with pageable: {}", pageable);
        // TODO: fetch page of POs and map to PoResponse
        return ResponseEntity.ok(Page.empty(pageable));
    }

    /**
     * Get po by ID
     *
     * @param id po ID
     * @return po details
     */
    @GetMapping("/{id}")
    public ResponseEntity<PoResponse> getPurchaseOrderById(@PathVariable Integer id) {
        log.info("Fetching PO id={}", id);
        // TODO: find by id -> return ResponseEntity.of(optionalPoResponse)
        return ResponseEntity.notFound().build();
    }

    /**
     * Create new PO
     *
     * @param request PO creation request
     * @return Created po details
     */
    @PostMapping
    public ResponseEntity<PoResponse> createPurchaseOrder(@Valid @RequestBody PoCreateRequest request) {
        log.info("Creating PO number={}", request.poNumber());
        // TODO: create -> PoResponse created
        // Example after creation:
        // PoResponse created = ...;
        // URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        //        .path("/{id}").buildAndExpand(created.id()).toUri();
        // return ResponseEntity.created(location).body(created);
        return ResponseEntity.badRequest().build();
    }

    /**
     * Update an existing po (full replace).
     *
     * @param id      PO ID to update
     * @param request PO payload
     * @return Updated PO
     */
    @PutMapping("/{id}")
    public ResponseEntity<PoResponse> updatePurchaseOrder(@PathVariable Integer id,
                                                          @Valid @RequestBody PoUpdateRequest request) {
        log.info("Updating PO id={}", id);
        // TODO: update & return updated response, or 404 if not found
        return ResponseEntity.notFound().build();
    }

    /**
     * Delete po by ID.
     *
     * @param id po ID to delete
     * @return 204 No Content if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Integer id) {
        log.info("Deleting PO id={}", id);
        // TODO: delete -> if existed return noContent(), else notFound()
        return ResponseEntity.noContent().build();
    }

}
