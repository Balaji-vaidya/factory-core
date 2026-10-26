package com.factory.office.repository;

import com.factory.office.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficeRepository extends JpaRepository<Office, String> {
    Optional<Office> findByName(String name);
    List<Office> findByStatus(String status);
    List<Office> findByState(String state);
}
