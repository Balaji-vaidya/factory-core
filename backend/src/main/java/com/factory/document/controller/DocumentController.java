package com.factory.document.controller;

import com.factory.common.response.ApiResponse;
import com.factory.common.security.CustomUserDetails;
import com.factory.document.dto.DocumentResponseDto;
import com.factory.document.service.DocumentStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentStorageService documentService;

    public DocumentController(DocumentStorageService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DocumentResponseDto>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") String entityId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String uploader = userDetails != null ? userDetails.getEmail() : "SYSTEM";
        DocumentResponseDto response = documentService.storeDocument(file, entityType, entityId, uploader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully", response));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String id) {
        DocumentResponseDto metadata = documentService.getDocumentMetadata(id);
        Resource resource = documentService.getDocumentResource(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getDocumentsForEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {

        List<DocumentResponseDto> documents = documentService.getDocumentsForEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved successfully", documents));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails != null ? userDetails.getId() : "SYSTEM";
        documentService.deleteDocument(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }
}
