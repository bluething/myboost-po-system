package io.github.bluething.myboostposystem.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PurchaseOrderHeaderRepository extends JpaRepository<PurchaseOrderHeader, Integer> {
    @Query("SELECT p FROM PurchaseOrderHeader p LEFT JOIN FETCH p.details d LEFT JOIN FETCH d.item WHERE p.id = :id")
    Optional<PurchaseOrderHeader> findByIdWithDetails(Integer id);
}
