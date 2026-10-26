package com.factory.product.controller;

import com.factory.common.response.ApiResponse;
import com.factory.common.response.PageResponse;
import com.factory.product.dto.*;
import com.factory.product.entity.ProductCategory;
import com.factory.product.entity.ProductInternalStatus;
import com.factory.product.entity.WebsiteVisibility;
import com.factory.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(
            @Valid @RequestBody ProductCreateDto createDto) {
        ProductResponseDto response = productService.createProduct(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Machinery product registered successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponseDto>>> getProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ProductInternalStatus status,
            @RequestParam(required = false) WebsiteVisibility visibility,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductResponseDto> response = productService.searchProducts(query, category, status, visibility, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductById(@PathVariable String id) {
        ProductResponseDto response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductBySku(@PathVariable String sku) {
        ProductResponseDto response = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductUpdateDto updateDto) {
        ProductResponseDto response = productService.updateProduct(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateInternalStatus(
            @PathVariable String id,
            @RequestParam ProductInternalStatus status) {
        ProductResponseDto response = productService.updateInternalStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Product internal status updated", response));
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateWebsiteVisibility(
            @PathVariable String id,
            @RequestParam WebsiteVisibility visibility) {
        ProductResponseDto response = productService.updateWebsiteVisibility(id, visibility);
        return ResponseEntity.ok(ApiResponse.success("Product website visibility updated", response));
    }

    @GetMapping("/{id}/snapshot")
    public ResponseEntity<ApiResponse<ProductSnapshotDto>> getProductSnapshot(@PathVariable String id) {
        ProductSnapshotDto response = productService.generateProductSnapshot(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
