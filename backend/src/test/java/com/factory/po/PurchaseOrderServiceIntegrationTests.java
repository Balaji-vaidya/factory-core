package com.factory.po;

import com.factory.client.dto.ClientAddressDto;
import com.factory.client.dto.ClientCreateDto;
import com.factory.client.dto.ClientResponseDto;
import com.factory.client.service.ClientService;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import com.factory.office.entity.Office;
import com.factory.office.repository.OfficeRepository;
import com.factory.po.dto.*;
import com.factory.po.entity.*;
import com.factory.po.service.PurchaseOrderService;
import com.factory.product.dto.ProductCreateDto;
import com.factory.product.dto.ProductResponseDto;
import com.factory.product.dto.ProductSpecDto;
import com.factory.product.entity.ProductCategory;
import com.factory.product.entity.ProductUnit;
import com.factory.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PurchaseOrderServiceIntegrationTests {

    @Autowired
    private PurchaseOrderService poService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OfficeRepository officeRepository;

    private String testClientId;
    private String testOfficeId;
    private String testProduct1Id;
    private String testProduct2Id;

    @BeforeEach
    void setUp() {
        Office office = officeRepository.findById("off-hq-jalna").orElseThrow();
        testOfficeId = office.getId();

        // Create test client
        ClientCreateDto clientDto = new ClientCreateDto();
        clientDto.setCompanyName("Bayer CropScience Test Account");
        clientDto.setGstin("27AABCB9999F1Z4");
        clientDto.setState("Maharashtra");
        clientDto.setPrimaryContactName("Amit Kulkarni");
        clientDto.setPrimaryContactPhone("+91 98220 99999");
        clientDto.setPrimaryContactEmail("amit.k@bayer.com");

        ClientAddressDto addressDto = new ClientAddressDto();
        addressDto.setSiteName("Bayer Jalna Processing Facility");
        addressDto.setAddressLine1("Plot 10, MIDC Phase 3");
        addressDto.setCity("Jalna");
        addressDto.setState("Maharashtra");
        addressDto.setPincode("431203");
        clientDto.setInitialAddress(addressDto);

        ClientResponseDto client = clientService.createClient(clientDto, "test-user");
        testClientId = client.getId();

        // Create test product 1
        ProductCreateDto p1 = new ProductCreateDto();
        p1.setSku("PO-TEST-GS10");
        p1.setName("Agrisep Gravity Separator PO Test");
        p1.setCategory(ProductCategory.GRAVITY_SEPARATOR);
        p1.setUnit(ProductUnit.SET);
        p1.setBasePriceINR(new BigDecimal("1200000.00"));
        ProductSpecDto s1 = new ProductSpecDto();
        s1.setCapacity("10 TPH");
        s1.setMotorPower("15 HP");
        s1.setDimensions("2800 x 1800 x 2000 mm");
        s1.setWeight("2400 kg");
        p1.setSpecs(s1);
        ProductResponseDto prod1 = productService.createProduct(p1);
        testProduct1Id = prod1.getId();

        // Create test product 2
        ProductCreateDto p2 = new ProductCreateDto();
        p2.setSku("PO-TEST-SC04");
        p2.setName("Agrisep Seed Cleaner PO Test");
        p2.setCategory(ProductCategory.SEED_CLEANER);
        p2.setUnit(ProductUnit.SET);
        p2.setBasePriceINR(new BigDecimal("650000.00"));
        ProductSpecDto s2 = new ProductSpecDto();
        s2.setCapacity("4 TPH");
        s2.setMotorPower("5 HP");
        s2.setDimensions("2400 x 1400 x 2100 mm");
        s2.setWeight("1800 kg");
        p2.setSpecs(s2);
        ProductResponseDto prod2 = productService.createProduct(p2);
        testProduct2Id = prod2.getId();
    }

    @Test
    @DisplayName("Create purchase order assigns sequential number, freezes snapshots, and locks SKU")
    void testCreatePurchaseOrder_SuccessWithSequentialNumber() {
        POCreateDto createDto = new POCreateDto();
        createDto.setClientId(testClientId);
        createDto.setOfficeId(testOfficeId);
        createDto.setPriority(POPriority.HIGH);
        createDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(6));
        createDto.setNotes("Urgent delivery for upcoming Kharif processing season.");

        POItemCreateDto item1 = new POItemCreateDto(testProduct1Id, 2, new BigDecimal("1200000.00"), LocalDate.now().plusWeeks(6), "GS-10 line");
        POItemCreateDto item2 = new POItemCreateDto(testProduct2Id, 1, new BigDecimal("650000.00"), LocalDate.now().plusWeeks(5), "SC-4 line");
        createDto.setItems(List.of(item1, item2));

        POResponseDto response = poService.createPurchaseOrder(createDto, "user-main-001");

        assertNotNull(response.getId());
        assertTrue(response.getPoNumber().startsWith("PO-"));
        assertEquals(POStatus.DRAFT, response.getStatus());
        assertEquals(POPriority.HIGH, response.getPriority());
        assertEquals(2, response.getItems().size());

        // Assert total amount calculation: (2 * 1200000) + (1 * 650000) = 3050000
        assertEquals(new BigDecimal("3050000.00"), response.getTotalAmountINR());

        // Assert client and product snapshots exist in JSON
        assertNotNull(response.getClientSnapshot());
        assertTrue(response.getClientSnapshot().contains("Bayer CropScience Test Account"));

        // Assert product SKU is now locked
        ProductResponseDto p1 = productService.getProductById(testProduct1Id);
        assertTrue(p1.isLockedForSkuChange());
    }

    @Test
    @DisplayName("Create purchase order rejects duplicate product line items")
    void testCreatePurchaseOrder_DuplicateProductsInSamePO_ThrowsConflict() {
        POCreateDto createDto = new POCreateDto();
        createDto.setClientId(testClientId);
        createDto.setOfficeId(testOfficeId);
        createDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(4));

        POItemCreateDto item1 = new POItemCreateDto(testProduct1Id, 1, new BigDecimal("1200000.00"), null, null);
        POItemCreateDto duplicateItem = new POItemCreateDto(testProduct1Id, 2, new BigDecimal("1200000.00"), null, null);
        createDto.setItems(List.of(item1, duplicateItem));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> poService.createPurchaseOrder(createDto, "user-main-001"));
        assertEquals(ErrorCode.DUPLICATE_PO_PRODUCT, ex.getErrorCode());
    }

    @Test
    @DisplayName("Complete PO approval lifecycle: Draft -> Submit -> Return -> Resubmit -> Approve")
    void testPOApprovalLifecycle_SubmitApproveReturn() {
        POCreateDto createDto = new POCreateDto();
        createDto.setClientId(testClientId);
        createDto.setOfficeId(testOfficeId);
        createDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(5));
        createDto.setItems(List.of(new POItemCreateDto(testProduct1Id, 1, new BigDecimal("1200000.00"), null, null)));

        // 1. Initial Draft
        POResponseDto po = poService.createPurchaseOrder(createDto, "user-main-001");
        assertEquals(POStatus.DRAFT, po.getStatus());

        // 2. Submit for Approval
        POResponseDto submitted = poService.submitForApproval(po.getId(), "user-main-001");
        assertEquals(POStatus.PENDING_OWNER_APPROVAL, submitted.getStatus());

        // 3. Owner Returns with Reason
        POResponseDto returned = poService.returnPurchaseOrder(po.getId(), "usr-owner-001", "Please negotiate 5% discount on commercial terms.");
        assertEquals(POStatus.RETURNED, returned.getStatus());
        assertEquals("Please negotiate 5% discount on commercial terms.", returned.getRejectionReason());

        // 4. Originator Resubmits
        POResponseDto resubmitted = poService.submitForApproval(po.getId(), "user-main-001");
        assertEquals(POStatus.PENDING_OWNER_APPROVAL, resubmitted.getStatus());
        assertNull(resubmitted.getRejectionReason());

        // 5. Owner Approves
        POResponseDto approved = poService.approvePurchaseOrder(po.getId(), "usr-owner-001", "Commercial terms approved. Proceed to stock reservation.");
        assertEquals(POStatus.APPROVED, approved.getStatus());
        assertEquals("usr-owner-001", approved.getApprovedBy());
        assertNotNull(approved.getApprovedAt());
    }

    @Test
    @DisplayName("Modifying an approved PO triggers deep version snapshotting and increments version")
    void testPOVersionSnapshotting_OnApprovedPOEdit() {
        POCreateDto createDto = new POCreateDto();
        createDto.setClientId(testClientId);
        createDto.setOfficeId(testOfficeId);
        createDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(4));
        createDto.setSubmitForApproval(true);
        createDto.setItems(List.of(new POItemCreateDto(testProduct1Id, 1, new BigDecimal("1200000.00"), null, null)));

        POResponseDto po = poService.createPurchaseOrder(createDto, "user-main-001");
        poService.approvePurchaseOrder(po.getId(), "usr-owner-001", "Initial version approved.");

        // Edit approved PO: add second product, extend delivery date, provide change reason
        POUpdateDto updateDto = new POUpdateDto();
        updateDto.setPriority(POPriority.URGENT);
        updateDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(8));
        updateDto.setChangeReason("Client requested additional Seed Cleaner and extended commissioning deadline.");
        updateDto.setItems(List.of(
                new POItemCreateDto(testProduct1Id, 1, new BigDecimal("1200000.00"), null, null),
                new POItemCreateDto(testProduct2Id, 1, new BigDecimal("650000.00"), null, null)
        ));

        POResponseDto updated = poService.updatePurchaseOrder(po.getId(), updateDto, "user-main-001");

        assertEquals(2, updated.getCurrentVersion());
        assertEquals(POStatus.PENDING_OWNER_APPROVAL, updated.getStatus()); // Requires re-approval
        assertEquals(new BigDecimal("1850000.00"), updated.getTotalAmountINR());

        // Verify version history
        List<POVersionResponseDto> versions = poService.getPOVersionHistory(po.getId());
        assertEquals(1, versions.size());
        POVersionResponseDto v1 = versions.get(0);
        assertEquals(1, v1.getVersionNumber());
        assertEquals("Client requested additional Seed Cleaner and extended commissioning deadline.", v1.getChangeSummary());
        assertNotNull(v1.getSnapshotData());
    }

    @Test
    @DisplayName("PO cancellation lifecycle: request cancellation and owner approval")
    void testPOCancellationLifecycle() {
        POCreateDto createDto = new POCreateDto();
        createDto.setClientId(testClientId);
        createDto.setOfficeId(testOfficeId);
        createDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(3));
        createDto.setItems(List.of(new POItemCreateDto(testProduct1Id, 1, new BigDecimal("1200000.00"), null, null)));

        POResponseDto po = poService.createPurchaseOrder(createDto, "user-main-001");

        // Request Cancellation
        POResponseDto cancelRequested = poService.requestCancellation(po.getId(), "user-main-001", "Client postponed machinery installation project to next fiscal year.");
        assertEquals(POStatus.CANCEL_REQUESTED, cancelRequested.getStatus());
        assertEquals("Client postponed machinery installation project to next fiscal year.", cancelRequested.getCancellationReason());

        // Owner Approves Cancellation
        POResponseDto cancelled = poService.approveCancellation(po.getId(), "usr-owner-001");
        assertEquals(POStatus.CANCELLED, cancelled.getStatus());

        // Attempting to modify a cancelled PO must fail
        POUpdateDto editDto = new POUpdateDto();
        editDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(10));
        editDto.setItems(List.of(new POItemCreateDto(testProduct1Id, 1, new BigDecimal("1200000.00"), null, null)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> poService.updatePurchaseOrder(po.getId(), editDto, "user-main-001"));
        assertEquals(ErrorCode.INVALID_STATUS_TRANSITION, ex.getErrorCode());
    }

    @Test
    @DisplayName("Search purchase orders with office and priority filters")
    void testSearchAndFilterPOs() {
        POCreateDto po1 = new POCreateDto();
        po1.setClientId(testClientId);
        po1.setOfficeId(testOfficeId);
        po1.setPriority(POPriority.URGENT);
        po1.setExpectedDeliveryDate(LocalDate.now().plusWeeks(2));
        po1.setItems(List.of(new POItemCreateDto(testProduct1Id, 1, new BigDecimal("1200000.00"), null, null)));
        POResponseDto created1 = poService.createPurchaseOrder(po1, "user-main-001");

        // Filter by URGENT priority
        PageResponse<POResponseDto> urgentResults = poService.searchPurchaseOrders(
                null, null, null, null, POPriority.URGENT, PageRequest.of(0, 10));
        assertTrue(urgentResults.getItems().stream().anyMatch(p -> p.getId().equals(created1.getId())));

        // Search by PO number
        PageResponse<POResponseDto> numberResults = poService.searchPurchaseOrders(
                created1.getPoNumber(), null, null, null, null, PageRequest.of(0, 10));
        assertEquals(1, numberResults.getItems().size());
        assertEquals(created1.getPoNumber(), numberResults.getItems().get(0).getPoNumber());
    }
}
