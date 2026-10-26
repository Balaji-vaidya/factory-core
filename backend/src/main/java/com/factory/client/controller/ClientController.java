package com.factory.client.controller;

import com.factory.client.dto.*;
import com.factory.client.entity.ClientStatus;
import com.factory.client.service.ClientService;
import com.factory.common.response.ApiResponse;
import com.factory.common.response.PageResponse;
import com.factory.common.security.CustomUserDetails;
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
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
    public ResponseEntity<ApiResponse<ClientResponseDto>> createClient(
            @Valid @RequestBody ClientCreateDto createDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails != null ? userDetails.getId() : "SYSTEM";
        ClientResponseDto response = clientService.createClient(createDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponseDto>>> getClients(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) ClientStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ClientResponseDto> response = clientService.searchClients(query, state, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClientResponseDto>> getClientById(@PathVariable String id) {
        ClientResponseDto response = clientService.getClientById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
    public ResponseEntity<ApiResponse<ClientResponseDto>> updateClient(
            @PathVariable String id,
            @Valid @RequestBody ClientUpdateDto updateDto) {
        ClientResponseDto response = clientService.updateClient(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success("Client updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
    public ResponseEntity<ApiResponse<ClientResponseDto>> updateClientStatus(
            @PathVariable String id,
            @RequestParam ClientStatus status) {
        ClientResponseDto response = clientService.updateClientStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Client status updated successfully", response));
    }

    @PostMapping("/{id}/addresses")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
    public ResponseEntity<ApiResponse<ClientResponseDto>> addDeliveryAddress(
            @PathVariable String id,
            @Valid @RequestBody ClientAddressDto addressDto) {
        ClientResponseDto response = clientService.addDeliveryAddress(id, addressDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Delivery address added successfully", response));
    }

    @PutMapping("/{id}/addresses/{addressId}/default")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
    public ResponseEntity<ApiResponse<ClientResponseDto>> setDefaultAddress(
            @PathVariable String id,
            @PathVariable String addressId) {
        ClientResponseDto response = clientService.setDefaultDeliveryAddress(id, addressId);
        return ResponseEntity.ok(ApiResponse.success("Default address updated successfully", response));
    }

    @GetMapping("/{id}/snapshot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClientSnapshotDto>> getClientSnapshot(
            @PathVariable String id,
            @RequestParam(required = false) String deliveryAddressId) {
        ClientSnapshotDto snapshot = clientService.generateClientSnapshot(id, deliveryAddressId);
        return ResponseEntity.ok(ApiResponse.success(snapshot));
    }
}
