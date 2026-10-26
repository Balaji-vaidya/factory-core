package com.factory.product.repository;

import com.factory.product.entity.Product;
import com.factory.product.entity.ProductCategory;
import com.factory.product.entity.ProductInternalStatus;
import com.factory.product.entity.WebsiteVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, String id);

    @Query("SELECT p FROM Product p WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:status IS NULL OR p.internalStatus = :status) AND " +
           "(:visibility IS NULL OR p.websiteVisibility = :visibility)")
    Page<Product> searchProducts(
            @Param("query") String query,
            @Param("category") ProductCategory category,
            @Param("status") ProductInternalStatus status,
            @Param("visibility") WebsiteVisibility visibility,
            Pageable pageable
    );

    List<Product> findByWebsiteVisibilityAndInternalStatus(
            WebsiteVisibility visibility,
            ProductInternalStatus internalStatus
    );
}
