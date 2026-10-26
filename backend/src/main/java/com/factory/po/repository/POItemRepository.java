package com.factory.po.repository;

import com.factory.po.entity.POItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface POItemRepository extends JpaRepository<POItem, String> {

    List<POItem> findByPurchaseOrderId(String poId);

    boolean existsByProductId(String productId);
}
