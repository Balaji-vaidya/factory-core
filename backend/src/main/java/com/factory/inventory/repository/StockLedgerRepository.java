package com.factory.inventory.repository;

import com.factory.inventory.entity.StockLedgerEntry;
import com.factory.inventory.entity.StockTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedgerEntry, String> {

    @Query("SELECT l FROM StockLedgerEntry l WHERE " +
           "(:officeId IS NULL OR l.office.id = :officeId) AND " +
           "(:productId IS NULL OR l.product.id = :productId) AND " +
           "(:transactionType IS NULL OR l.transactionType = :transactionType) " +
           "ORDER BY l.createdAt DESC")
    Page<StockLedgerEntry> searchLedger(
            @Param("officeId") String officeId,
            @Param("productId") String productId,
            @Param("transactionType") StockTransactionType transactionType,
            Pageable pageable);

    List<StockLedgerEntry> findByInventoryIdOrderByCreatedAtDesc(String inventoryId);
}
