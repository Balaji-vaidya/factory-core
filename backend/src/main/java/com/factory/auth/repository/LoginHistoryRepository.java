package com.factory.auth.repository;

import com.factory.auth.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, String> {
    List<LoginHistory> findByUserIdOrderByTimestampDesc(String userId);
    List<LoginHistory> findByEmailOrderByTimestampDesc(String email);
}
