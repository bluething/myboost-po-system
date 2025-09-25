package io.github.bluething.myboostposystem.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
abstract class BaseEntity {

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Created timestamp stored as Instant (UTC)
     * PostgreSQL TIMESTAMPTZ handles UTC storage automatically
     */
    @CreationTimestamp
    @Column(name = "created_datetime", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant createdDatetime;

    /**
     * Updated timestamp stored as Instant (UTC)
     * Automatically updated on entity modification
     */
    @UpdateTimestamp
    @Column(name = "updated_datetime", columnDefinition = "TIMESTAMPTZ")
    private Instant updatedDatetime;
}
