package com.factory.document.service;

import com.factory.audit.service.AuditLogService;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.document.dto.DocumentResponseDto;
import com.factory.document.entity.DocumentMetadata;
import com.factory.document.repository.DocumentRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentStorageService {

    private final DocumentRepository documentRepository;
    private final ObjectProvider<AuditLogService> auditLogServiceProvider;
    private final Path rootStorageLocation;

    public DocumentStorageService(DocumentRepository documentRepository,
                                  ObjectProvider<AuditLogService> auditLogServiceProvider) {
        this.documentRepository = documentRepository;
        this.auditLogServiceProvider = auditLogServiceProvider;
        this.rootStorageLocation = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload storage location", e);
        }
    }

    public DocumentResponseDto storeDocument(MultipartFile file, String entityType, String entityId, String uploadedBy) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Cannot upload empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        if (originalFilename.contains("..")) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Filename contains invalid path sequence: " + originalFilename);
        }

        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        String storedFileName = UUID.randomUUID() + fileExtension;
        Path targetDir = this.rootStorageLocation.resolve(entityType.toLowerCase()).resolve(entityId);
        try {
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            DocumentMetadata doc = new DocumentMetadata(
                    originalFilename,
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                    file.getSize(),
                    targetFile.toString(),
                    entityType.toUpperCase(),
                    entityId,
                    uploadedBy
            );

            DocumentMetadata saved = documentRepository.save(doc);

            auditLogServiceProvider.ifAvailable(as -> as.logAction(
                    uploadedBy, null, "UPLOAD_DOCUMENT", doc.getEntityType(), doc.getEntityId(),
                    "Uploaded file: " + doc.getFileName() + " (" + doc.getFileSize() + " bytes)", null));

            return mapToDto(saved);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to store file on disk: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public DocumentResponseDto getDocumentMetadata(String id) {
        DocumentMetadata doc = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Document not found with id: " + id));
        return mapToDto(doc);
    }

    @Transactional(readOnly = true)
    public Resource getDocumentResource(String id) {
        DocumentMetadata doc = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Document not found with id: " + id));

        try {
            Path file = Paths.get(doc.getStoragePath());
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "File not readable or not found on storage: " + doc.getFileName());
            }
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Invalid file path URL for document: " + doc.getFileName());
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getDocumentsForEntity(String entityType, String entityId) {
        return documentRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType.toUpperCase(), entityId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void deleteDocument(String id, String currentUserId) {
        DocumentMetadata doc = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Document not found with id: " + id));

        try {
            Path file = Paths.get(doc.getStoragePath());
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }

        documentRepository.delete(doc);

        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "DELETE_DOCUMENT", doc.getEntityType(), doc.getEntityId(),
                "Deleted file: " + doc.getFileName(), null));
    }

    private DocumentResponseDto mapToDto(DocumentMetadata doc) {
        DocumentResponseDto dto = new DocumentResponseDto();
        dto.setId(doc.getId());
        dto.setFileName(doc.getFileName());
        dto.setFileType(doc.getFileType());
        dto.setFileSize(doc.getFileSize());
        dto.setEntityType(doc.getEntityType());
        dto.setEntityId(doc.getEntityId());
        dto.setUploadedBy(doc.getUploadedBy());
        dto.setCreatedAt(doc.getCreatedAt());
        return dto;
    }
}
