package com.factory.inventory.repository;

import com.factory.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {

    Optional<Inventory> findByOfficeIdAndProductId(String officeId, String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.office.id = :officeId AND i.product.id = :productId")
    Optional<Inventory> findByOfficeIdAndProductIdForUpdate(@Param("officeId") String officeId, @Param("productId") String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.id = :id")
    Optional<Inventory> findByIdForUpdate(@Param("id") String id);

    @Query("SELECT i FROM Inventory i WHERE " +
           "(:officeId IS NULL OR i.office.id = :officeId) AND " +
           "(:productId IS NULL OR i.product.id = :productId) AND " +
           "(:query IS NULL OR LOWER(i.product.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.product.sku) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Inventory> searchInventory(
            @Param("officeId") String officeId,
            @Param("productId") String productId,
            @Param("query") String query,
            Pageable pageable);

    List<Inventory> findByOfficeId(String officeId);
}
