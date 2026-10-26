package com.factory.delivery.controller;

import com.factory.common.response.ApiResponse;
import com.factory.common.response.PageResponse;
import com.factory.common.security.CustomUserDetails;
import com.factory.delivery.dto.DCCreateDto;
import com.factory.delivery.dto.DCDeliveryConfirmDto;
import com.factory.delivery.dto.DCResponseDto;
import com.factory.delivery.entity.DCStatus;
import com.factory.delivery.service.DeliveryChallanService;
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
@RequestMapping("/api/v1/delivery-challans")
public class DeliveryChallanController {

    private final DeliveryChallanService dcService;

    public DeliveryChallanController(DeliveryChallanService dcService) {
        this.dcService = dcService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<DCResponseDto>>> searchChallans(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String officeId,
            @RequestParam(required = false) DCStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<DCResponseDto> result = dcService.searchChallans(query, officeId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DCResponseDto>> getDeliveryChallanById(@PathVariable String id) {
        DCResponseDto result = dcService.getDeliveryChallanById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/number/{dcNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DCResponseDto>> getDeliveryChallanByNumber(@PathVariable String dcNumber) {
        DCResponseDto result = dcService.getDeliveryChallanByNumber(dcNumber);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/po/{poId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DCResponseDto>>> getChallansByPO(@PathVariable String poId) {
        List<DCResponseDto> result = dcService.getChallansByPO(poId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE', 'GODOWN_MANAGER')")
    public ResponseEntity<ApiResponse<DCResponseDto>> createDeliveryChallan(
            @Valid @RequestBody DCCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails != null ? userDetails.getId() : "system";
        DCResponseDto result = dcService.createDeliveryChallan(dto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Delivery challan generated successfully.", result));
    }

    @PostMapping("/{id}/dispatch")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'GODOWN_MANAGER')")
    public ResponseEntity<ApiResponse<DCResponseDto>> dispatchDeliveryChallan(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails != null ? userDetails.getId() : "system";
        DCResponseDto result = dcService.dispatchDeliveryChallan(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Delivery challan dispatched and inventory deducted.", result));
    }

    @PostMapping("/{id}/confirm-delivery")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE')")
    public ResponseEntity<ApiResponse<DCResponseDto>> confirmDelivery(
            @PathVariable String id,
            @Valid @RequestBody DCDeliveryConfirmDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails != null ? userDetails.getId() : "system";
        DCResponseDto result = dcService.confirmDelivery(id, dto, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Goods receipt and delivery confirmed successfully.", result));
    }
}
