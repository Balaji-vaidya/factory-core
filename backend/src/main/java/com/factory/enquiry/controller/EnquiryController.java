package com.factory.enquiry.controller;

import com.factory.client.dto.ClientResponseDto;
import com.factory.common.response.ApiResponse;
import com.factory.common.response.PageResponse;
import com.factory.common.security.CustomUserDetails;
import com.factory.enquiry.dto.EnquiryResponseDto;
import com.factory.enquiry.dto.EnquiryStatusUpdateDto;
import com.factory.enquiry.entity.EnquiryStatus;
import com.factory.enquiry.service.EnquiryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enquiries")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
public class EnquiryController {

    private final EnquiryService enquiryService;

    public EnquiryController(EnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EnquiryResponseDto>>> searchEnquiries(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) EnquiryStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<EnquiryResponseDto> response = enquiryService.searchEnquiries(query, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Enquiries retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnquiryResponseDto>> getEnquiryById(@PathVariable String id) {
        EnquiryResponseDto response = enquiryService.getEnquiryById(id);
        return ResponseEntity.ok(ApiResponse.success("Enquiry retrieved successfully", response));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EnquiryResponseDto>> updateEnquiryStatus(
            @PathVariable String id,
            @Valid @RequestBody EnquiryStatusUpdateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        EnquiryResponseDto response = enquiryService.updateEnquiryStatus(id, dto, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Enquiry status updated successfully", response));
    }

    @PostMapping("/{id}/convert-to-client")
    public ResponseEntity<ApiResponse<ClientResponseDto>> convertEnquiryToClient(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ClientResponseDto response = enquiryService.convertEnquiryToClient(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Enquiry successfully converted to Client record", response));
    }
}
