package com.factory.inventory.repository;

import com.factory.inventory.entity.ShortageStatus;
import com.factory.inventory.entity.StockShortage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockShortageRepository extends JpaRepository<StockShortage, String> {

    @Query("SELECT s FROM StockShortage s WHERE " +
           "s.office.id = :officeId AND s.product.id = :productId AND " +
           "(s.status = 'OPEN' OR s.status = 'PARTIALLY_RESOLVED') " +
           "ORDER BY s.purchaseOrder.poDate ASC, s.purchaseOrder.expectedDeliveryDate ASC, s.purchaseOrder.poNumber ASC")
    List<StockShortage> findOpenShortagesForAllocation(
            @Param("officeId") String officeId,
            @Param("productId") String productId);

    List<StockShortage> findByPurchaseOrderId(String poId);

    Optional<StockShortage> findByPoItemId(String poItemId);

    @Query("SELECT s FROM StockShortage s WHERE " +
           "(:officeId IS NULL OR s.office.id = :officeId) AND " +
           "(:productId IS NULL OR s.product.id = :productId) AND " +
           "(:status IS NULL OR s.status = :status) " +
           "ORDER BY s.createdAt DESC")
    Page<StockShortage> searchShortages(
            @Param("officeId") String officeId,
            @Param("productId") String productId,
            @Param("status") ShortageStatus status,
            Pageable pageable);
}
