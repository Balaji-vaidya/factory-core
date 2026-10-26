package com.factory.document;

import com.factory.client.dto.ClientResponseDto;
import com.factory.client.repository.ClientRepository;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import com.factory.document.dto.DocumentResponseDto;
import com.factory.document.service.DocumentStorageService;
import com.factory.enquiry.dto.EnquiryResponseDto;
import com.factory.enquiry.dto.EnquiryStatusUpdateDto;
import com.factory.enquiry.dto.EnquirySubmitDto;
import com.factory.enquiry.entity.EnquiryStatus;
import com.factory.enquiry.service.EnquiryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class EnquiryAndDocumentIntegrationTests {

    @Autowired
    private EnquiryService enquiryService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DocumentStorageService documentService;

    @Test
    @DisplayName("Should submit public enquiry and query lead list")
    void testPublicEnquirySubmission() {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        EnquirySubmitDto dto = new EnquirySubmitDto();
        dto.setName("Vikram Patel");
        dto.setEmail("vikram." + suffix + "@patelseeds.com");
        dto.setPhone("9876500000");
        dto.setCompanyName("Patel Hybrid Seeds " + suffix);
        dto.setState("Gujarat");
        dto.setMessage("Interested in turnkey seed processing plant with 10 TPH capacity.");
        dto.setInterestedProducts("AG-GS-10, AG-SC-04");

        EnquiryResponseDto response = enquiryService.submitPublicEnquiry(dto);
        assertNotNull(response.getId());
        assertEquals(EnquiryStatus.NEW, response.getStatus());
        assertEquals("Patel Hybrid Seeds " + suffix, response.getCompanyName());

        PageResponse<EnquiryResponseDto> search = enquiryService.searchEnquiries(suffix, null, PageRequest.of(0, 10));
        assertFalse(search.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should update enquiry status with internal sales notes")
    void testEnquiryStatusUpdate() {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        EnquirySubmitDto dto = new EnquirySubmitDto();
        dto.setName("Suresh Kumar");
        dto.setEmail("suresh." + suffix + "@kisanagro.com");
        dto.setPhone("9876511111");
        dto.setMessage("Quotation requested for Destoner.");

        EnquiryResponseDto submitted = enquiryService.submitPublicEnquiry(dto);

        EnquiryStatusUpdateDto updateDto = new EnquiryStatusUpdateDto();
        updateDto.setStatus(EnquiryStatus.CONTACTED);
        updateDto.setInternalNotes("Called customer. Scheduled technical demo for next Tuesday.");

        EnquiryResponseDto updated = enquiryService.updateEnquiryStatus(submitted.getId(), updateDto, "sales_rep_1");
        assertEquals(EnquiryStatus.CONTACTED, updated.getStatus());
        assertEquals("Called customer. Scheduled technical demo for next Tuesday.", updated.getInternalNotes());
    }

    @Test
    @DisplayName("Should convert enquiry into a full Client record and reject duplicate conversions")
    void testEnquiryConversionToClient() {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        EnquirySubmitDto dto = new EnquirySubmitDto();
        dto.setName("Anita Sharma");
        dto.setEmail("anita." + suffix + "@sharmafarms.com");
        dto.setPhone("9876522222");
        dto.setCompanyName("Sharma Agri Ventures " + suffix);
        dto.setState("Madhya Pradesh");
        dto.setMessage("Ready to purchase Gravity Separator.");

        EnquiryResponseDto submitted = enquiryService.submitPublicEnquiry(dto);

        ClientResponseDto client = enquiryService.convertEnquiryToClient(submitted.getId(), "tester");
        assertNotNull(client.getId());
        assertEquals("Sharma Agri Ventures " + suffix, client.getCompanyName());

        EnquiryResponseDto reloadedEnquiry = enquiryService.getEnquiryById(submitted.getId());
        assertEquals(EnquiryStatus.CONVERTED_TO_CLIENT, reloadedEnquiry.getStatus());
        assertEquals(client.getId(), reloadedEnquiry.getConvertedClientId());

        // Second conversion attempt should fail with DUPLICATE_RESOURCE
        BusinessException ex = assertThrows(BusinessException.class, () ->
                enquiryService.convertEnquiryToClient(submitted.getId(), "tester")
        );
        assertEquals(ErrorCode.DUPLICATE_RESOURCE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should upload document, retrieve file resource, and delete document")
    void testDocumentUploadAndDownload() throws Exception {
        String testEntityId = "PO-" + UUID.randomUUID().toString().substring(0, 6);
        byte[] fileBytes = "INDO AMERICAN EQUIPMENT PO SIGNED SCAN".getBytes(StandardCharsets.UTF_8);

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "signed_po.pdf",
                "application/pdf",
                fileBytes
        );

        DocumentResponseDto uploaded = documentService.storeDocument(mockFile, "PURCHASE_ORDER", testEntityId, "owner@agrisep.com");
        assertNotNull(uploaded.getId());
        assertEquals("signed_po.pdf", uploaded.getFileName());
        assertEquals("PURCHASE_ORDER", uploaded.getEntityType());
        assertEquals(testEntityId, uploaded.getEntityId());

        // Verify retrieval
        List<DocumentResponseDto> docs = documentService.getDocumentsForEntity("PURCHASE_ORDER", testEntityId);
        assertEquals(1, docs.size());

        Resource resource = documentService.getDocumentResource(uploaded.getId());
        assertTrue(resource.exists());
        assertEquals(fileBytes.length, resource.contentLength());

        // Clean up
        documentService.deleteDocument(uploaded.getId(), "tester");
        List<DocumentResponseDto> afterDelete = documentService.getDocumentsForEntity("PURCHASE_ORDER", testEntityId);
        assertTrue(afterDelete.isEmpty());
    }
}
