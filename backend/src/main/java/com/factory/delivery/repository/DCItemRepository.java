package com.factory.delivery.repository;

import com.factory.delivery.entity.DCItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DCItemRepository extends JpaRepository<DCItem, String> {
    List<DCItem> findByDeliveryChallanId(String deliveryChallanId);
}
