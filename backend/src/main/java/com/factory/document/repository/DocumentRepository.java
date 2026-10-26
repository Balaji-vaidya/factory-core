package com.factory.document.repository;

import com.factory.document.entity.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentMetadata, String> {

    List<DocumentMetadata> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);
}
