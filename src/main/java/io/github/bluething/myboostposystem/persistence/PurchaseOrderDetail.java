package io.github.bluething.myboostposystem.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "po_d", indexes = {
        @Index(name = "idx_po_d_poh_id", columnList = "poh_id"),
        @Index(name = "idx_po_d_item_id", columnList = "item_id"),
        @Index(name = "idx_po_d_item_qty", columnList = "item_qty")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_po_d_poh_item", columnNames = {"poh_id", "item_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"purchaseOrderHeader", "item"})
class PurchaseOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Many-to-one relationship with Purchase Order Header
     * Each detail belongs to one purchase order header
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poh_id", nullable = false, foreignKey = @ForeignKey(name = "fk_po_d_poh_id"))
    @NotNull(message = "Purchase order header is required")
    private PurchaseOrderHeader purchaseOrderHeader;

    /**
     * Many-to-one relationship with Item
     * Each detail references one item
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_po_d_item_id"))
    @NotNull(message = "Item is required")
    private Item item;

    @NotNull(message = "Item quantity is required")
    @Positive(message = "Item quantity must be positive")
    @Column(name = "item_qty", nullable = false)
    private Integer itemQty;

    @NotNull(message = "Item cost is required")
    @PositiveOrZero(message = "Item cost must be zero or positive")
    @Column(name = "item_cost", nullable = false, columnDefinition = "BIGINT")
    private Long itemCost;

    @NotNull(message = "Item price is required")
    @PositiveOrZero(message = "Item price must be zero or positive")
    @Column(name = "item_price", nullable = false, columnDefinition = "BIGINT")
    private Long itemPrice;

    /**
     * Calculates total cost for this detail line
     * @return total cost (item cost * quantity)
     */
    public Long getTotalCost() {
        if (itemCost == null || itemQty == null) return 0L;
        return itemCost * itemQty;
    }

    /**
     * Calculates total price for this detail line
     * @return total price (item price * quantity)
     */
    public Long getTotalPrice() {
        if (itemPrice == null || itemQty == null) return 0L;
        return itemPrice * itemQty;
    }

    /**
     * Calculates total profit for this detail line
     * @return total profit (total price - total cost)
     */
    public Long getTotalProfit() {
        return getTotalPrice() - getTotalCost();
    }

    /**
     * Calculates profit margin percentage for this detail line
     * @return profit margin percentage
     */
    public Double getProfitPercentage() {
        Long totalCost = getTotalCost();
        if (totalCost == null || totalCost == 0) return 0.0;
        return ((double) getTotalProfit() / totalCost) * 100;
    }

    /**
     * Updates the item cost and price from the related item
     * Useful when creating details from items
     */
    public void updateFromItem() {
        if (item != null) {
            this.itemCost = item.getCost();
            this.itemPrice = item.getPrice();
        }
    }

    /**
     * Validates that the detail has consistent data
     * @return true if detail is valid
     */
    public boolean isValid() {
        return itemQty != null && itemQty > 0 &&
                itemCost != null && itemCost >= 0 &&
                itemPrice != null && itemPrice >= 0 &&
                item != null && purchaseOrderHeader != null;
    }

    /**
     * Gets item name from related item (convenience method)
     * @return item name or null if item is not loaded
     */
    public String getItemName() {
        return item != null ? item.getName() : null;
    }

    /**
     * Gets item description from related item (convenience method)
     * @return item description or null if item is not loaded
     */
    public String getItemDescription() {
        return item != null ? item.getDescription() : null;
    }

    /**
     * Creates a new detail from an item with specified quantity
     * Convenience factory method
     * @param item the item to create detail from
     * @param quantity the quantity
     * @return new PurchaseOrderDetail instance
     */
    public static PurchaseOrderDetail fromItem(Item item, Integer quantity) {
        if (item == null || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Item and positive quantity are required");
        }

        return PurchaseOrderDetail.builder()
                .item(item)
                .itemQty(quantity)
                .itemCost(item.getCost())
                .itemPrice(item.getPrice())
                .build();
    }

    /**
     * Pre-persist validation
     */
    @PrePersist
    protected void onCreate() {
        validateData();
        if (itemCost == null && item != null) {
            itemCost = item.getCost();
        }
        if (itemPrice == null && item != null) {
            itemPrice = item.getPrice();
        }
    }

    /**
     * Pre-update validation
     */
    @PreUpdate
    protected void onUpdate() {
        validateData();
    }

    /**
     * Validates required data before persistence
     */
    private void validateData() {
        if (itemQty == null || itemQty <= 0) {
            throw new IllegalStateException("Item quantity must be positive");
        }
        if (itemCost == null || itemCost < 0) {
            throw new IllegalStateException("Item cost must be zero or positive");
        }
        if (itemPrice == null || itemPrice < 0) {
            throw new IllegalStateException("Item price must be zero or positive");
        }
        if (purchaseOrderHeader == null) {
            throw new IllegalStateException("Purchase order header is required");
        }
        if (item == null) {
            throw new IllegalStateException("Item is required");
        }
    }
}
