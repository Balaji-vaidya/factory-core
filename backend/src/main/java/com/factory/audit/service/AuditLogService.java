package com.factory.audit.service;

import com.factory.audit.dto.AuditLogResponseDto;
import com.factory.audit.entity.AuditLog;
import com.factory.audit.repository.AuditLogRepository;
import com.factory.common.response.PageResponse;
import com.factory.common.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logAction(String userId, String userEmail, String action, String entityName, String entityId, String details, String ipAddress) {
        AuditLog log = new AuditLog(userId, userEmail, action, entityName, entityId, details, ipAddress);
        return auditLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logCurrentAction(String action, String entityName, String entityId, String details) {
        String userId = "SYSTEM";
        String userEmail = "system@factorycore.internal";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            userId = userDetails.getId();
            userEmail = userDetails.getEmail();
        } else if (auth != null && auth.getPrincipal() instanceof String principalStr) {
            userEmail = principalStr;
        }

        AuditLog log = new AuditLog(userId, userEmail, action, entityName, entityId, details, "127.0.0.1");
        return auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponseDto> searchLogs(String action, String entityName, String userId, Instant startDate, Instant endDate, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.searchLogs(action, entityName, userId, startDate, endDate, pageable);
        List<AuditLogResponseDto> dtos = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponse.of(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponseDto> getLogsForEntity(String entityName, String entityId, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByEntityNameAndEntityIdOrderByCreatedAtDesc(entityName, entityId, pageable);
        List<AuditLogResponseDto> dtos = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponse.of(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    private AuditLogResponseDto mapToDto(AuditLog log) {
        AuditLogResponseDto dto = new AuditLogResponseDto();
        dto.setId(log.getId());
        dto.setUserId(log.getUserId());
        dto.setUserEmail(log.getUserEmail());
        dto.setAction(log.getAction());
        dto.setEntityName(log.getEntityName());
        dto.setEntityId(log.getEntityId());
        dto.setDetails(log.getDetails());
        dto.setIpAddress(log.getIpAddress());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
