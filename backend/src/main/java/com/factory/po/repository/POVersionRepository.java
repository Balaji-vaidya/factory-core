package com.factory.po.repository;

import com.factory.po.entity.POVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface POVersionRepository extends JpaRepository<POVersion, String> {

    List<POVersion> findByPurchaseOrderIdOrderByVersionNumberDesc(String poId);

    Optional<POVersion> findByPurchaseOrderIdAndVersionNumber(String poId, int versionNumber);
}
