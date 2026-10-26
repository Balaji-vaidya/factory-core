package com.factory.audit;

import com.factory.audit.dto.AuditLogResponseDto;
import com.factory.audit.entity.AuditLog;
import com.factory.audit.repository.AuditLogRepository;
import com.factory.audit.service.AuditLogService;
import com.factory.common.response.PageResponse;
import com.factory.notification.dto.NotificationResponseDto;
import com.factory.notification.entity.Notification;
import com.factory.notification.repository.NotificationRepository;
import com.factory.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class NotificationAndAuditIntegrationTests {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    private String testUserId;
    private String testOfficeId;

    @BeforeEach
    void setUp() {
        testUserId = "user-" + UUID.randomUUID().toString().substring(0, 8);
        testOfficeId = "office-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("Should successfully record audit log and query by entity")
    void testCreateAndQueryAuditLog() {
        String entityId = "PO-" + UUID.randomUUID().toString().substring(0, 6);
        AuditLog saved = auditLogService.logAction(
                testUserId,
                "operator@agrisep.com",
                "CREATE",
                "PURCHASE_ORDER",
                entityId,
                "Created test PO for testing audit logger",
                "192.168.1.100"
        );

        assertNotNull(saved.getId());
        assertEquals("CREATE", saved.getAction());
        assertEquals("PURCHASE_ORDER", saved.getEntityName());
        assertEquals(entityId, saved.getEntityId());

        PageResponse<AuditLogResponseDto> logs = auditLogService.getLogsForEntity("PURCHASE_ORDER", entityId, PageRequest.of(0, 10));
        assertFalse(logs.getItems().isEmpty());
        assertEquals(1, logs.getTotalItems());
        assertEquals("operator@agrisep.com", logs.getItems().get(0).getUserEmail());
        assertEquals("192.168.1.100", logs.getItems().get(0).getIpAddress());
    }

    @Test
    @DisplayName("Should search audit logs by action and date filters")
    void testSearchAuditLogsByCriteria() {
        String entityId = "INV-" + UUID.randomUUID().toString().substring(0, 6);
        auditLogService.logAction(
                testUserId,
                "admin@agrisep.com",
                "STOCK_ADJUSTMENT",
                "INVENTORY",
                entityId,
                "Manual cycle count discrepancy reconciliation",
                "10.0.0.1"
        );

        PageResponse<AuditLogResponseDto> searchResult = auditLogService.searchLogs(
                "STOCK_ADJUSTMENT",
                "INVENTORY",
                null,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertFalse(searchResult.getItems().isEmpty());
        assertTrue(searchResult.getItems().stream().anyMatch(l -> l.getEntityId().equals(entityId)));
    }

    @Test
    @DisplayName("Should create notifications and count unread accurately for targeted user and role")
    void testNotificationCreationAndRetrieval() {
        // Direct notification to user
        notificationService.createNotification(
                testUserId,
                testOfficeId,
                null,
                "Welcome to Factory Core",
                "Your account is now ready for operations.",
                "USER_WELCOME",
                testUserId,
                "USER"
        );

        // Role-based broadcast notification (e.g. OWNER)
        notificationService.createNotification(
                null,
                null,
                "OWNER",
                "New High Priority PO Received",
                "PO-9999 requires owner sign-off.",
                "PO_SUBMITTED",
                "po-9999",
                "PURCHASE_ORDER"
        );

        // Query for user without OWNER role
        long userUnreadCount = notificationService.getUnreadCount(testUserId, testOfficeId, "MAIN_OFFICE");
        assertEquals(1, userUnreadCount);

        // Query for user with OWNER role
        long ownerUnreadCount = notificationService.getUnreadCount(testUserId, testOfficeId, "OWNER");
        assertEquals(2, ownerUnreadCount);

        PageResponse<NotificationResponseDto> pageResult = notificationService.getNotificationsForUser(
                testUserId, testOfficeId, "OWNER", PageRequest.of(0, 10)
        );
        assertEquals(2, pageResult.getTotalItems());
    }

    @Test
    @DisplayName("Should mark single notification as read")
    void testMarkNotificationAsRead() {
        notificationService.createNotification(
                testUserId,
                testOfficeId,
                null,
                "Status Update",
                "PO has been approved.",
                "PO_APPROVED",
                "po-123",
                "PURCHASE_ORDER"
        );

        PageResponse<NotificationResponseDto> initialPage = notificationService.getNotificationsForUser(
                testUserId, testOfficeId, "MAIN_OFFICE", PageRequest.of(0, 10)
        );
        String notifId = initialPage.getItems().get(0).getId();

        notificationService.markAsRead(notifId);

        Notification updated = notificationRepository.findById(notifId).orElseThrow();
        assertTrue(updated.isRead());
        assertNotNull(updated.getReadAt());

        long unreadCount = notificationService.getUnreadCount(testUserId, testOfficeId, "MAIN_OFFICE");
        assertEquals(0, unreadCount);
    }

    @Test
    @DisplayName("Should mark all notifications as read in bulk")
    void testMarkAllNotificationsAsRead() {
        notificationService.createNotification(testUserId, testOfficeId, null, "Alert 1", "Msg 1", "TYPE_1", null, null);
        notificationService.createNotification(testUserId, testOfficeId, null, "Alert 2", "Msg 2", "TYPE_2", null, null);

        long countBefore = notificationService.getUnreadCount(testUserId, testOfficeId, "MAIN_OFFICE");
        assertEquals(2, countBefore);

        notificationService.markAllAsRead(testUserId, testOfficeId, "MAIN_OFFICE");

        long countAfter = notificationService.getUnreadCount(testUserId, testOfficeId, "MAIN_OFFICE");
        assertEquals(0, countAfter);
    }
}
