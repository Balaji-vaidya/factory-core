package com.factory.enquiry.repository;

import com.factory.enquiry.entity.EnquiryStatus;
import com.factory.enquiry.entity.WebsiteEnquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnquiryRepository extends JpaRepository<WebsiteEnquiry, String> {

    @Query("SELECT e FROM WebsiteEnquiry e WHERE " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:query IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(e.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(e.phone) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<WebsiteEnquiry> searchEnquiries(
            @Param("query") String query,
            @Param("status") EnquiryStatus status,
            Pageable pageable
    );
}
