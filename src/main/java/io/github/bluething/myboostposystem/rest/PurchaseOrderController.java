package io.github.bluething.myboostposystem.rest;

import io.github.bluething.myboostposystem.common.TimezoneUtil;
import io.github.bluething.myboostposystem.domain.po.*;
import io.github.bluething.myboostposystem.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
@Slf4j
class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    /**
     * Get all po with pagination support
     *
     * @param pageable Pagination parameters
     * @return Page of pos
     */
    @GetMapping
    public ResponseEntity<Page<Response>> getPurchaseOrders(Pageable pageable) {
        log.info("Listing POs with pageable: {}", pageable);

        Page<POData> poData = purchaseOrderService.findAll(pageable);
        Page<Response> responsePage = toResponsePage(poData);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get po by ID
     *
     * @param id po ID
     * @return po details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Response> getPurchaseOrderById(@PathVariable Integer id) {
        log.info("Fetching PO id={}", id);

        return purchaseOrderService.findById(id)
                .map(this::toResponse)
                .map(response -> ResponseEntity.ok().body(response))
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found"));
    }

    /**
     * Create new PO
     *
     * @param request PO creation request
     * @return Created po details
     */
    @PostMapping
    public ResponseEntity<Response> createPurchaseOrder(@Valid @RequestBody CreatePORequest request) {
        log.info("Creating PO");

        return ResponseEntity.ok(toResponse(purchaseOrderService.create(toCreateCommand(request))));
    }

    /**
     * Update an existing po (full replace).
     *
     * @param id      PO ID to update
     * @param request PO payload
     * @return Updated PO
     */
    @PutMapping("/{id}")
    public ResponseEntity<Response> updatePurchaseOrder(@PathVariable Integer id,
                                                          @Valid @RequestBody UpdatePORequest request) {
        log.info("Updating PO id={}", id);

        return ResponseEntity.ok(toResponse(purchaseOrderService.update(id, toUpdateCommand(request))));
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

        purchaseOrderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    CreatePOCommand toCreateCommand(CreatePORequest request) {
        if (request == null) {
            return null;
        }
        List<CreatePODetail> details = request.details().stream()
                .map(d -> new CreatePODetail(d.itemId(), d.quantity(), d.unitPrice(), d.cost()))
                .toList();

        return CreatePOCommand.builder()
                .datetime(request.datetime())
                .description(request.description())
                .totalPrice(request.totalPrice())
                .totalCost(request.totalCost())
                .createdBy("SYSTEM")
                .details(details)
                .build();
    }

    UpdatePOCommand toUpdateCommand(UpdatePORequest request) {
        if (request == null) {
            return null;
        }
        List<CreatePODetail> details = request.details().stream()
                .map(d -> new CreatePODetail(d.itemId(), d.quantity(), d.unitPrice(), d.cost()))
                .toList();

        return UpdatePOCommand.builder()
                .datetime(request.datetime())
                .description(request.description())
                .totalPrice(request.totalPrice())
                .totalCost(request.totalCost())
                .updatedBy("SYSTEM")
                .details(details)
                .build();
    }

    private Response toResponse(POData data) {
        List<DetailResponse> details = data.details().stream()
                .map(d -> new DetailResponse(d.itemId(), d.quantity(), d.unitPrice(), d.cost()))
                .toList();

        return new Response(
                data.id(),
                data.orderDate(),
                data.description(),
                data.totalPrice(),
                data.totalCost(),
                data.createdBy(),
                data.updatedBy(),
                TimezoneUtil.toAppLocalDateTime(data.createdDatetime()),
                TimezoneUtil.toAppLocalDateTime(data.updatedDatetime()),
                details
        );
    }
    private Page<Response> toResponsePage(Page<POData> dataPage) {
        if (dataPage == null) {
            return null;
        }

        return dataPage.map(this::toResponse);
    }


}
