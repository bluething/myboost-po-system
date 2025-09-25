package io.github.bluething.myboostposystem.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_items_name", columnList = "name"),
        @Index(name = "idx_items_price", columnList = "price"),
        @Index(name = "idx_items_cost", columnList = "cost"),
        @Index(name = "idx_items_created_datetime", columnList = "created_datetime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, of = "id")
@ToString(exclude = {"createdDatetime", "updatedDatetime"})
class Item extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Item name is required")
    @Size(max = 500, message = "Item name must not exceed 500 characters")
    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be zero or positive")
    @Column(name = "price", nullable = false, columnDefinition = "BIGINT")
    private Long price;

    @NotNull(message = "Cost is required")
    @PositiveOrZero(message = "Cost must be zero or positive")
    @Column(name = "cost", nullable = false, columnDefinition = "BIGINT")
    private Long cost;

    /**
     * Calculates profit margin for the item
     * @return profit margin as Long (price - cost)
     */
    public Long getProfitMargin() {
        if (price == null || cost == null) return 0L;
        return price - cost;
    }

    /**
     * Calculates profit percentage for the item
     * @return profit percentage as double
     */
    public Double getProfitPercentage() {
        if (cost == null || cost == 0 || price == null) return 0.0;
        return ((double) (price - cost) / cost) * 100;
    }

    /**
     * Checks if item is profitable
     * @return true if price is greater than cost
     */
    public boolean isProfitable() {
        return getProfitMargin() > 0;
    }

    /**
     * Pre-persist callback to set audit fields
     */
    @PrePersist
    protected void onCreate() {
        if (getCreatedBy() == null) {
            setCreatedBy("system");
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
    }
}
