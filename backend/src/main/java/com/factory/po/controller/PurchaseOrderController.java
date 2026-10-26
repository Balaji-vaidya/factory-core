package com.factory.po.controller;

import com.factory.common.response.ApiResponse;
import com.factory.common.response.PageResponse;
import com.factory.common.security.CustomUserDetails;
import com.factory.po.dto.*;
import com.factory.po.entity.POPriority;
import com.factory.po.entity.POStatus;
import com.factory.po.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    public PurchaseOrderController(PurchaseOrderService poService) {
        this.poService = poService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE')")
    public ResponseEntity<ApiResponse<POResponseDto>> createPurchaseOrder(
            @Valid @RequestBody POCreateDto createDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails != null ? userDetails.getId() : "SYSTEM";
        POResponseDto response = poService.createPurchaseOrder(createDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<POResponseDto>>> getPurchaseOrders(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String officeId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) POStatus status,
            @RequestParam(required = false) POPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<POResponseDto> response = poService.searchPurchaseOrders(query, officeId, state, status, priority, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<POResponseDto>> getPurchaseOrderById(@PathVariable String id) {
        POResponseDto response = poService.getPurchaseOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{poNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<POResponseDto>> getPurchaseOrderByNumber(@PathVariable String poNumber) {
        POResponseDto response = poService.getPurchaseOrderByNumber(poNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE')")
    public ResponseEntity<ApiResponse<POResponseDto>> updatePurchaseOrder(
            @PathVariable String id,
            @Valid @RequestBody POUpdateDto updateDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails != null ? userDetails.getId() : "SYSTEM";
        POResponseDto response = poService.updatePurchaseOrder(id, updateDto, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Purchase order updated successfully", response));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE')")
    public ResponseEntity<ApiResponse<POResponseDto>> submitForApproval(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails != null ? userDetails.getId() : "SYSTEM";
        POResponseDto response = poService.submitForApproval(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Purchase order submitted for Owner approval", response));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<POResponseDto>> approvePurchaseOrder(
            @PathVariable String id,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String ownerUserId = userDetails != null ? userDetails.getId() : "OWNER";
        POResponseDto response = poService.approvePurchaseOrder(id, ownerUserId, notes);
        return ResponseEntity.ok(ApiResponse.success("Purchase order approved successfully", response));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<POResponseDto>> returnPurchaseOrder(
            @PathVariable String id,
            @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String ownerUserId = userDetails != null ? userDetails.getId() : "OWNER";
        POResponseDto response = poService.returnPurchaseOrder(id, ownerUserId, reason);
        return ResponseEntity.ok(ApiResponse.success("Purchase order returned to originator with notes", response));
    }

    @PostMapping("/{id}/cancel-request")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE')")
    public ResponseEntity<ApiResponse<POResponseDto>> requestCancellation(
            @PathVariable String id,
            @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails != null ? userDetails.getId() : "SYSTEM";
        POResponseDto response = poService.requestCancellation(id, currentUserId, reason);
        return ResponseEntity.ok(ApiResponse.success("Cancellation request submitted for Owner approval", response));
    }

    @PostMapping("/{id}/cancel-approve")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<POResponseDto>> approveCancellation(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String ownerUserId = userDetails != null ? userDetails.getId() : "OWNER";
        POResponseDto response = poService.approveCancellation(id, ownerUserId);
        return ResponseEntity.ok(ApiResponse.success("Purchase order cancelled successfully", response));
    }

    @GetMapping("/{id}/versions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<POVersionResponseDto>>> getVersionHistory(@PathVariable String id) {
        List<POVersionResponseDto> versions = poService.getPOVersionHistory(id);
        return ResponseEntity.ok(ApiResponse.success(versions));
    }
}
