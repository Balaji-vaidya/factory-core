package com.factory.inventory.controller;

import com.factory.common.response.ApiResponse;
import com.factory.common.response.PageResponse;
import com.factory.common.security.CustomUserDetails;
import com.factory.inventory.dto.InventoryResponseDto;
import com.factory.inventory.dto.StockAdjustmentDto;
import com.factory.inventory.dto.StockInwardDto;
import com.factory.inventory.dto.StockLedgerResponseDto;
import com.factory.inventory.entity.StockTransactionType;
import com.factory.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponseDto>>> searchInventory(
            @RequestParam(required = false) String officeId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<InventoryResponseDto> result = inventoryService.searchInventory(officeId, productId, query, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> getInventoryById(@PathVariable String id) {
        InventoryResponseDto result = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/inward")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE', 'GODOWN_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> recordInward(
            @Valid @RequestBody StockInwardDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails != null ? userDetails.getId() : "system";
        InventoryResponseDto result = inventoryService.recordInward(dto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock inward successfully recorded in ledger.", result));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'GODOWN_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> adjustStock(
            @Valid @RequestBody StockAdjustmentDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails != null ? userDetails.getId() : "system";
        InventoryResponseDto result = inventoryService.adjustStock(dto, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Stock adjustment recorded in audit ledger.", result));
    }

    @GetMapping("/ledger")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<StockLedgerResponseDto>>> searchLedger(
            @RequestParam(required = false) String officeId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) StockTransactionType transactionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<StockLedgerResponseDto> result = inventoryService.searchLedger(officeId, productId, transactionType, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
