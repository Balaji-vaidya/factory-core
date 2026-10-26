package com.factory.po.repository;

import com.factory.po.entity.POPriority;
import com.factory.po.entity.POStatus;
import com.factory.po.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    boolean existsByPoNumber(String poNumber);

    List<PurchaseOrder> findByClientId(String clientId);

    @Query("SELECT po FROM PurchaseOrder po WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(po.client.companyName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:officeId IS NULL OR po.office.id = :officeId) AND " +
           "(:state IS NULL OR po.office.state = :state) AND " +
           "(:status IS NULL OR po.status = :status) AND " +
           "(:priority IS NULL OR po.priority = :priority)")
    Page<PurchaseOrder> searchPurchaseOrders(
            @Param("query") String query,
            @Param("officeId") String officeId,
            @Param("state") String state,
            @Param("status") POStatus status,
            @Param("priority") POPriority priority,
            Pageable pageable
    );
}
