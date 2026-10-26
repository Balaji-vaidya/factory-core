package com.factory.enquiry.service;

import com.factory.audit.service.AuditLogService;
import com.factory.client.dto.ClientCreateDto;
import com.factory.client.dto.ClientResponseDto;
import com.factory.client.entity.ClientType;
import com.factory.client.service.ClientService;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import com.factory.enquiry.dto.EnquiryResponseDto;
import com.factory.enquiry.dto.EnquiryStatusUpdateDto;
import com.factory.enquiry.dto.EnquirySubmitDto;
import com.factory.enquiry.entity.EnquiryStatus;
import com.factory.enquiry.entity.WebsiteEnquiry;
import com.factory.enquiry.repository.EnquiryRepository;
import com.factory.notification.service.NotificationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final ClientService clientService;
    private final ObjectProvider<NotificationService> notificationServiceProvider;
    private final ObjectProvider<AuditLogService> auditLogServiceProvider;

    public EnquiryService(EnquiryRepository enquiryRepository,
                          ClientService clientService,
                          ObjectProvider<NotificationService> notificationServiceProvider,
                          ObjectProvider<AuditLogService> auditLogServiceProvider) {
        this.enquiryRepository = enquiryRepository;
        this.clientService = clientService;
        this.notificationServiceProvider = notificationServiceProvider;
        this.auditLogServiceProvider = auditLogServiceProvider;
    }

    public EnquiryResponseDto submitPublicEnquiry(EnquirySubmitDto dto) {
        WebsiteEnquiry enquiry = new WebsiteEnquiry();
        enquiry.setName(dto.getName().trim());
        enquiry.setEmail(dto.getEmail().trim().toLowerCase());
        enquiry.setPhone(dto.getPhone().trim());
        enquiry.setCompanyName(dto.getCompanyName() != null ? dto.getCompanyName().trim() : null);
        enquiry.setState(dto.getState() != null ? dto.getState().trim() : null);
        enquiry.setMessage(dto.getMessage().trim());
        enquiry.setInterestedProducts(dto.getInterestedProducts());
        enquiry.setSource(dto.getSource() != null ? dto.getSource() : "WEBSITE_CONTACT");
        enquiry.setStatus(EnquiryStatus.NEW);

        WebsiteEnquiry saved = enquiryRepository.save(enquiry);

        // Notify sales / main office & owner of new lead
        notificationServiceProvider.ifAvailable(ns -> {
            String company = saved.getCompanyName() != null ? " (" + saved.getCompanyName() + ")" : "";
            ns.createNotification(
                    null, null, "MAIN_OFFICE",
                    "New Website Lead Received",
                    "New enquiry from " + saved.getName() + company + ": " + saved.getMessage(),
                    "NEW_ENQUIRY", saved.getId(), "WEBSITE_ENQUIRY");
        });

        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                "PUBLIC_USER", saved.getEmail(), "SUBMIT", "WEBSITE_ENQUIRY", saved.getId(),
                "Website enquiry submitted by " + saved.getName(), "127.0.0.1"));

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnquiryResponseDto> searchEnquiries(String query, EnquiryStatus status, Pageable pageable) {
        Page<WebsiteEnquiry> page = enquiryRepository.searchEnquiries(query, status, pageable);
        List<EnquiryResponseDto> content = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public EnquiryResponseDto getEnquiryById(String id) {
        WebsiteEnquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Enquiry not found: " + id));
        return mapToDto(enquiry);
    }

    public EnquiryResponseDto updateEnquiryStatus(String id, EnquiryStatusUpdateDto dto, String currentUserId) {
        WebsiteEnquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Enquiry not found: " + id));

        enquiry.setStatus(dto.getStatus());
        if (dto.getInternalNotes() != null) {
            enquiry.setInternalNotes(dto.getInternalNotes().trim());
        }

        WebsiteEnquiry saved = enquiryRepository.save(enquiry);

        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "UPDATE_STATUS", "WEBSITE_ENQUIRY", saved.getId(),
                "Updated status to " + dto.getStatus(), null));

        return mapToDto(saved);
    }

    public ClientResponseDto convertEnquiryToClient(String id, String currentUserId) {
        WebsiteEnquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Enquiry not found: " + id));

        if (enquiry.getConvertedClientId() != null || enquiry.getStatus() == EnquiryStatus.CONVERTED_TO_CLIENT) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Enquiry is already converted to a client with ID: " + enquiry.getConvertedClientId());
        }

        ClientCreateDto clientDto = new ClientCreateDto();
        String companyName = enquiry.getCompanyName() != null && !enquiry.getCompanyName().trim().isEmpty()
                ? enquiry.getCompanyName().trim()
                : enquiry.getName().trim() + " Enterprise";
        clientDto.setCompanyName(companyName);
        clientDto.setClientType(ClientType.SEED_PRODUCER);
        clientDto.setState(enquiry.getState() != null && !enquiry.getState().trim().isEmpty() ? enquiry.getState().trim() : "Maharashtra");
        clientDto.setPrimaryContactName(enquiry.getName());
        clientDto.setPrimaryContactPhone(enquiry.getPhone());
        clientDto.setPrimaryContactEmail(enquiry.getEmail());

        ClientResponseDto client = clientService.createClient(clientDto, currentUserId);

        enquiry.setStatus(EnquiryStatus.CONVERTED_TO_CLIENT);
        enquiry.setConvertedClientId(client.getId());
        enquiryRepository.save(enquiry);

        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "CONVERT_TO_CLIENT", "WEBSITE_ENQUIRY", enquiry.getId(),
                "Converted enquiry to Client: " + client.getCompanyName() + " (" + client.getId() + ")", null));

        return client;
    }

    private EnquiryResponseDto mapToDto(WebsiteEnquiry e) {
        EnquiryResponseDto dto = new EnquiryResponseDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setCompanyName(e.getCompanyName());
        dto.setState(e.getState());
        dto.setMessage(e.getMessage());
        dto.setInterestedProducts(e.getInterestedProducts());
        dto.setSource(e.getSource());
        dto.setStatus(e.getStatus());
        dto.setInternalNotes(e.getInternalNotes());
        dto.setConvertedClientId(e.getConvertedClientId());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
