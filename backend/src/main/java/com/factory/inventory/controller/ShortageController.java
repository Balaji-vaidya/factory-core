package com.factory.inventory.controller;

import com.factory.common.response.ApiResponse;
import com.factory.common.response.PageResponse;
import com.factory.inventory.dto.StockShortageResponseDto;
import com.factory.inventory.entity.ShortageStatus;
import com.factory.inventory.service.ShortageAllocationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shortages")
public class ShortageController {

    private final ShortageAllocationService shortageAllocationService;

    public ShortageController(ShortageAllocationService shortageAllocationService) {
        this.shortageAllocationService = shortageAllocationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<StockShortageResponseDto>>> searchShortages(
            @RequestParam(required = false) String officeId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) ShortageStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<StockShortageResponseDto> result = shortageAllocationService.searchShortages(officeId, productId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
