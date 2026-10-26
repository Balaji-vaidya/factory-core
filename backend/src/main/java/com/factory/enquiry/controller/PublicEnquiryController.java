package com.factory.enquiry.controller;

import com.factory.common.response.ApiResponse;
import com.factory.enquiry.dto.EnquiryResponseDto;
import com.factory.enquiry.dto.EnquirySubmitDto;
import com.factory.enquiry.service.EnquiryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/enquiries")
public class PublicEnquiryController {

    private final EnquiryService enquiryService;

    public PublicEnquiryController(EnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EnquiryResponseDto>> submitPublicEnquiry(@Valid @RequestBody EnquirySubmitDto dto) {
        EnquiryResponseDto response = enquiryService.submitPublicEnquiry(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thank you for contacting Agrisep. Our sales team will get back to you shortly.", response));
    }
}
