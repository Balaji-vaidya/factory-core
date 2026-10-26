package com.factory.notification.repository;

import com.factory.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT n FROM Notification n WHERE " +
           "(n.userId = :userId OR (n.userId IS NULL AND (n.officeId IS NULL OR n.officeId = :officeId) AND (n.role IS NULL OR n.role = :role))) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findForUser(
            @Param("userId") String userId,
            @Param("officeId") String officeId,
            @Param("role") String role,
            Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE " +
           "n.read = false AND " +
           "(n.userId = :userId OR (n.userId IS NULL AND (n.officeId IS NULL OR n.officeId = :officeId) AND (n.role IS NULL OR n.role = :role)))")
    long countUnreadForUser(
            @Param("userId") String userId,
            @Param("officeId") String officeId,
            @Param("role") String role);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt WHERE " +
           "n.read = false AND " +
           "(n.userId = :userId OR (n.userId IS NULL AND (n.officeId IS NULL OR n.officeId = :officeId) AND (n.role IS NULL OR n.role = :role)))")
    int markAllReadForUser(
            @Param("userId") String userId,
            @Param("officeId") String officeId,
            @Param("role") String role,
            @Param("readAt") Instant readAt);
}
