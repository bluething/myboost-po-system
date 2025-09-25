package io.github.bluething.myboostposystem.domain.po;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PurchaseOrderService {
    /**
     * Retrieves all purchase orders with pagination support.
     *
     * @param pageable pagination parameters
     * @return page of purchase orders
     */
    Page<POData> findAll(Pageable pageable);

    /**
     * Retrieves a purchase order by its ID.
     *
     * @param id purchase order ID
     * @return optional containing the purchase order if found
     */
    Optional<POData> findById(Integer id);

    /**
     * Creates a new purchase order.
     * Validates business rules and calculates totals.
     *
     * @param createDto purchase order creation data
     * @return created purchase order
     * @throws IllegalArgumentException if validation fails
     */
    POData create(CreatePOCommand createDto);

    /**
     * Updates an existing purchase order.
     * Performs full replacement of the purchase order data.
     *
     * @param id purchase order ID to update
     * @param updateDto updated purchase order data
     * @return updated purchase order
     * @throws IllegalArgumentException if validation fails
     * @throws io.github.bluething.myboostposystem.exception.PoNotFoundException if PO not found
     */
    POData update(Integer id, UpdatePOCommand updateDto);

    /**
     * Deletes a purchase order by its ID.
     *
     * @param id purchase order ID to delete
     * @return true if deleted, false if not found
     */
    boolean deleteById(Integer id);
}
