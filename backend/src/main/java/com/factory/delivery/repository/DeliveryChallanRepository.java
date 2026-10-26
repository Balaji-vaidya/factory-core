package com.factory.delivery.repository;

import com.factory.delivery.entity.DCStatus;
import com.factory.delivery.entity.DeliveryChallan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryChallanRepository extends JpaRepository<DeliveryChallan, String> {

    Optional<DeliveryChallan> findByDcNumber(String dcNumber);

    List<DeliveryChallan> findByPurchaseOrderId(String purchaseOrderId);

    @Query("SELECT d FROM DeliveryChallan d WHERE " +
           "(:query IS NULL OR LOWER(d.dcNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.vehicleNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.transporterName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.client.companyName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:officeId IS NULL OR d.office.id = :officeId) AND " +
           "(:status IS NULL OR d.status = :status) " +
           "ORDER BY d.createdAt DESC")
    Page<DeliveryChallan> searchChallans(
            @Param("query") String query,
            @Param("officeId") String officeId,
            @Param("status") DCStatus status,
            Pageable pageable);
}
