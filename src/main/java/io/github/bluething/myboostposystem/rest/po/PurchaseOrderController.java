package io.github.bluething.myboostposystem.rest.po;

import io.github.bluething.myboostposystem.common.TimezoneUtil;
import io.github.bluething.myboostposystem.domain.po.*;
import io.github.bluething.myboostposystem.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * @param page Page number (0-based)
     * @param size Page size
     * @return Page of pos
     */
    @Operation(
            summary = "Get all purchase orders with pagination",
            description = "Retrieve a paginated list of purchase orders ordered by creation date (newest first)"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved purchase orders")
    @GetMapping
    public ResponseEntity<Page<Response>> getPurchaseOrders(@Parameter(description = "Page number (0-based)", example = "0")
                                                                @RequestParam(defaultValue = "0") @Min(0) Integer page,

                                                            @Parameter(description = "Page size", example = "10")
                                                                @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        log.info("Listing POs - page: {}, size: {}", page, size);

        // Default sort by ID descending (newest purchase orders first)
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, defaultSort);

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
                .map(d -> new DetailResponse(d.itemId(), d.quantity(), d.cost(), d.unitPrice()))
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
