package com.factory.audit.controller;

import com.factory.audit.dto.AuditLogResponseDto;
import com.factory.audit.service.AuditLogService;
import com.factory.common.response.ApiResponse;
import com.factory.common.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/audit/logs")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponseDto>>> searchLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<AuditLogResponseDto> response = auditLogService.searchLogs(action, entityName, userId, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", response));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponseDto>>> getLogsForEntity(
            @PathVariable String entityName,
            @PathVariable String entityId,
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<AuditLogResponseDto> response = auditLogService.getLogsForEntity(entityName, entityId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Entity audit logs retrieved successfully", response));
    }
}
