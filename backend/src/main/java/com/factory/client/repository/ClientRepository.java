package com.factory.client.repository;

import com.factory.client.entity.Client;
import com.factory.client.entity.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    Optional<Client> findByGstin(String gstin);

    boolean existsByGstin(String gstin);

    boolean existsByGstinAndIdNot(String gstin, String id);

    @Query("SELECT c FROM Client c WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(c.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.gstin) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.pan) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.primaryContactName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:state IS NULL OR :state = '' OR :state = 'ALL' OR c.state = :state) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<Client> searchClients(
            @Param("query") String query,
            @Param("state") String state,
            @Param("status") ClientStatus status,
            Pageable pageable
    );
}
