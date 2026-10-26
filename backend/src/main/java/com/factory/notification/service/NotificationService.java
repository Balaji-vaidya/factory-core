package com.factory.notification.service;

import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import com.factory.notification.dto.NotificationResponseDto;
import com.factory.notification.entity.Notification;
import com.factory.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(String userId, String officeId, String role, String title, String message, String type, String referenceId, String referenceType) {
        Notification notification = new Notification(userId, officeId, role, title, message, type, referenceId, referenceType);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponseDto> getNotificationsForUser(String userId, String officeId, String role, Pageable pageable) {
        Page<Notification> page = notificationRepository.findForUser(userId, officeId, role, pageable);
        List<NotificationResponseDto> content = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId, String officeId, String role) {
        return notificationRepository.countUnreadForUser(userId, officeId, role);
    }

    public void markAsRead(String notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Notification not found: " + notificationId));
        n.setRead(true);
        n.setReadAt(Instant.now());
        notificationRepository.save(n);
    }

    public void markAllAsRead(String userId, String officeId, String role) {
        notificationRepository.markAllReadForUser(userId, officeId, role, Instant.now());
    }

    public NotificationResponseDto mapToDto(Notification n) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setReferenceId(n.getReferenceId());
        dto.setReferenceType(n.getReferenceType());
        dto.setRead(n.isRead());
        dto.setReadAt(n.getReadAt());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
