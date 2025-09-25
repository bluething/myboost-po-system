package io.github.bluething.myboostposystem.persistence;

import io.github.bluething.myboostposystem.common.TimezoneUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "po_h", indexes = {
        @Index(name = "idx_po_h_datetime", columnList = "datetime"),
        @Index(name = "idx_po_h_total_price", columnList = "total_price"),
        @Index(name = "idx_po_h_total_cost", columnList = "total_cost"),
        @Index(name = "idx_po_h_created_datetime", columnList = "created_datetime"),
        @Index(name = "idx_po_h_created_by", columnList = "created_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, of = "id")
@ToString(exclude = {"details", "createdDatetime", "updatedDatetime"})
class PurchaseOrderHeader extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Purchase order datetime stored as Instant (UTC)
     * PostgreSQL TIMESTAMPTZ ensures proper timezone handling
     */
    @NotNull(message = "Purchase order date is required")
    @Column(name = "datetime", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant datetime;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Total price is required")
    @PositiveOrZero(message = "Total price must be zero or positive")
    @Column(name = "total_price", nullable = false, columnDefinition = "BIGINT")
    @Builder.Default
    private Long totalPrice = 0L;

    @NotNull(message = "Total cost is required")
    @PositiveOrZero(message = "Total cost must be zero or positive")
    @Column(name = "total_cost", nullable = false, columnDefinition = "BIGINT")
    @Builder.Default
    private Long totalCost = 0L;

    /**
     * One-to-many relationship with Purchase Order Details
     * CascadeType.ALL ensures that when PO header is saved/deleted, details follow
     * orphanRemoval = true removes detail records when they are removed from the list
     */
    @OneToMany(
            mappedBy = "purchaseOrderHeader",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<PurchaseOrderDetail> details = new ArrayList<>();

    /**
     * Helper method to add a detail to the purchase order
     * Maintains bidirectional relationship
     * @param detail the purchase order detail to add
     */
    public void addDetail(PurchaseOrderDetail detail) {
        if (detail == null) return;
        details.add(detail);
        detail.setPurchaseOrderHeader(this);
    }

    /**
     * Helper method to remove a detail from the purchase order
     * Maintains bidirectional relationship
     * @param detail the purchase order detail to remove
     */
    public void removeDetail(PurchaseOrderDetail detail) {
        if (detail == null) return;
        details.remove(detail);
        detail.setPurchaseOrderHeader(null);
    }

    /**
     * Clears all details from the purchase order
     * Maintains bidirectional relationship
     */
    public void clearDetails() {
        details.forEach(detail -> detail.setPurchaseOrderHeader(null));
        details.clear();
    }

    /**
     * Calculates total profit for the purchase order
     * @return total profit (total price - total cost)
     */
    public Long getTotalProfit() {
        return (totalPrice != null ? totalPrice : 0L) - (totalCost != null ? totalCost : 0L);
    }

    /**
     * Recalculates totals based on details
     * Should be called when details are modified
     */
    public void calculateTotals() {
        this.totalPrice = details.stream()
                .mapToLong(detail -> detail.getTotalPrice() != null ? detail.getTotalPrice() : 0L)
                .sum();

        this.totalCost = details.stream()
                .mapToLong(detail -> detail.getTotalCost() != null ? detail.getTotalCost() : 0L)
                .sum();
    }

    /**
     * Calculates profit percentage for the purchase order
     * @return profit percentage as double
     */
    public Double getProfitPercentage() {
        if (totalCost == null || totalCost == 0) return 0.0;
        Long profit = getTotalProfit();
        return ((double) profit / totalCost) * 100;
    }

    /**
     * Gets formatted datetime for display in application timezone
     * @return formatted datetime string (Asia/Jakarta)
     */
    public String getFormattedDatetime() {
        return TimezoneUtil.formatForDisplay(datetime);
    }

    /**
     * Sets datetime using current UTC instant
     */
    public void setCurrentDatetime() {
        this.datetime = TimezoneUtil.now();
    }

    /**
     * Pre-persist callback to set audit fields and defaults
     */
    @PrePersist
    protected void onCreate() {
        if (datetime == null) {
            setCurrentDatetime();
        }
        if (getCreatedBy() == null) {
            setCreatedBy("system");
        }
        if (totalPrice == null) {
            totalPrice = 0L;
        }
        if (totalCost == null) {
            totalCost = 0L;
        }
    }

    /**
     * Pre-update callback to set audit fields
     */
    @PreUpdate
    protected void onUpdate() {
        if (getUpdatedBy() == null) {
            setUpdatedBy("system");
        }
        // Recalculate totals before update
        calculateTotals();
    }
}
