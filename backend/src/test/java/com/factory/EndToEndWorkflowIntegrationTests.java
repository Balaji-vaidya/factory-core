package com.factory;

import com.factory.audit.dto.AuditLogResponseDto;
import com.factory.audit.service.AuditLogService;
import com.factory.client.dto.ClientAddressDto;
import com.factory.client.dto.ClientCreateDto;
import com.factory.client.dto.ClientResponseDto;
import com.factory.client.entity.ClientType;
import com.factory.client.service.ClientService;
import com.factory.delivery.dto.DCCreateDto;
import com.factory.delivery.dto.DCDeliveryConfirmDto;
import com.factory.delivery.dto.DCItemCreateDto;
import com.factory.delivery.dto.DCResponseDto;
import com.factory.delivery.entity.DCStatus;
import com.factory.delivery.service.DeliveryChallanService;
import com.factory.inventory.dto.StockInwardDto;
import com.factory.inventory.dto.StockShortageResponseDto;
import com.factory.inventory.entity.Inventory;
import com.factory.inventory.entity.ShortageStatus;
import com.factory.inventory.repository.InventoryRepository;
import com.factory.inventory.service.InventoryService;
import com.factory.inventory.service.ShortageAllocationService;
import com.factory.notification.service.NotificationService;
import com.factory.office.entity.Office;
import com.factory.office.entity.OfficeType;
import com.factory.office.repository.OfficeRepository;
import com.factory.po.dto.POCreateDto;
import com.factory.po.dto.POItemCreateDto;
import com.factory.po.dto.POResponseDto;
import com.factory.po.entity.POStatus;
import com.factory.po.service.PurchaseOrderService;
import com.factory.product.dto.ProductCreateDto;
import com.factory.product.dto.ProductResponseDto;
import com.factory.product.entity.ProductCategory;
import com.factory.product.entity.ProductUnit;
import com.factory.product.service.ProductService;
import com.factory.reporting.dto.OwnerDashboardDto;
import com.factory.reporting.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class EndToEndWorkflowIntegrationTests {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PurchaseOrderService poService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ShortageAllocationService shortageAllocationService;

    @Autowired
    private DeliveryChallanService dcService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DashboardService dashboardService;

    private String testOfficeId;
    private String testClientId;
    private String testProductId;

    @BeforeEach
    void setUp() {
        Office godown = new Office(
                "off-e2e-jalna",
                "Jalna Central Plant & Godown",
                OfficeType.WAREHOUSE_GODOWN,
                "Maharashtra",
                "Jalna",
                "Plot B-12, MIDC Phase 3, Jalna",
                "27AAACI4921C1Z0",
                "+91 2482 299900",
                "central.godown@agrisep.com"
        );
        officeRepository.save(godown);
        testOfficeId = godown.getId();

        // Register client
        ClientCreateDto clientDto = new ClientCreateDto();
        clientDto.setCompanyName("Barwale Agro Seed Processing Corp");
        clientDto.setClientType(ClientType.SEED_PRODUCER);
        clientDto.setGstin("27AABCB9999K1ZR");
        clientDto.setPan("AABCB9999K");
        clientDto.setState("Maharashtra");
        clientDto.setPrimaryContactName("Rajendra Barwale");
        clientDto.setPrimaryContactPhone("+91 94222 11223");
        clientDto.setPrimaryContactEmail("rajendra@barwale.test");

        ClientAddressDto addressDto = new ClientAddressDto();
        addressDto.setSiteName("Jalna Seed Conditioning Unit");
        addressDto.setAddressLine1("Survey No 88, Dawadwadi Road");
        addressDto.setCity("Jalna");
        addressDto.setState("Maharashtra");
        addressDto.setPincode("431203");
        addressDto.setContactPerson("Rajendra Barwale");
        addressDto.setPhone("+91 94222 11223");
        clientDto.setInitialAddress(addressDto);

        ClientResponseDto client = clientService.createClient(clientDto, "usr-main-001");
        testClientId = client.getId();

        // Register machinery product
        ProductCreateDto productDto = new ProductCreateDto();
        productDto.setSku("E2E-AG-GS12");
        productDto.setName("Agrisep® Heavy-Duty Seed Gravity Separator GS-12");
        productDto.setCategory(ProductCategory.GRAVITY_SEPARATOR);
        productDto.setUnit(ProductUnit.SET);
        productDto.setBasePriceINR(new BigDecimal("1450000.00"));
        productDto.setLeadTimeWeeks(3);
        ProductResponseDto product = productService.createProduct(productDto);
        testProductId = product.getId();
    }

    @Test
    @DisplayName("Complete end-to-end flow: Client -> PO -> Owner Approval with Shortage -> Inward with FIFO Allocation -> DC Dispatch -> Delivery Confirmation")
    void testCompleteManufacturingToDeliveryFlow() {
        // Step 1: Main office creates Purchase Order for 2 units of GS-12
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClientId);
        poDto.setOfficeId(testOfficeId);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(3));
        poDto.setSubmitForApproval(true);
        poDto.setItems(List.of(
                new POItemCreateDto(testProductId, 2, new BigDecimal("1450000.00"), null, "Fitted with multi-deck screen")
        ));

        POResponseDto createdPo = poService.createPurchaseOrder(poDto, "usr-main-001");
        assertNotNull(createdPo.getId());
        assertTrue(createdPo.getPoNumber().startsWith("PO-"));
        assertEquals(POStatus.PENDING_OWNER_APPROVAL, createdPo.getStatus());
        assertEquals(1, createdPo.getItems().size());
        assertEquals(2, createdPo.getItems().get(0).getQuantity());
        assertEquals(0, createdPo.getItems().get(0).getReservedQuantity());

        // Step 2: Owner approves Purchase Order
        // At this point, warehouse stock is 0 -> PO is approved, but 2 units logged as OPEN shortage
        POResponseDto approvedPo = poService.approvePurchaseOrder(createdPo.getId(), "usr-owner-001", "Production schedule confirmed by factory head");
        assertEquals(POStatus.APPROVED, approvedPo.getStatus());
        assertEquals(0, approvedPo.getItems().get(0).getReservedQuantity());

        // Verify open shortage created for 2 units
        var openShortages = shortageAllocationService.searchShortages(testOfficeId, testProductId, ShortageStatus.OPEN, PageRequest.of(0, 10));
        assertEquals(1, openShortages.getTotalItems());
        assertEquals(2, openShortages.getItems().get(0).getShortageQuantity());

        // Step 3: Godown Manager records inward of 3 finished units into warehouse
        inventoryService.recordInward(
                new StockInwardDto(testOfficeId, testProductId, 3, 2, "Production Batch #JALNA-2026-GS12"),
                "usr-godown-001"
        );

        // Verify deterministic FIFO inward fulfilled the shortage
        var resolvedShortages = shortageAllocationService.searchShortages(testOfficeId, testProductId, ShortageStatus.RESOLVED, PageRequest.of(0, 10));
        assertEquals(1, resolvedShortages.getTotalItems());
        assertEquals(0, shortageAllocationService.searchShortages(testOfficeId, testProductId, ShortageStatus.OPEN, PageRequest.of(0, 10)).getTotalItems());

        // Verify inventory invariants: 3 on-hand, 2 reserved for PO, 1 available
        Inventory inv = inventoryRepository.findByOfficeIdAndProductId(testOfficeId, testProductId).orElseThrow();
        assertEquals(3, inv.getOnHandQuantity());
        assertEquals(2, inv.getReservedQuantity());
        assertEquals(1, inv.getAvailableQuantity());
        assertEquals(inv.getOnHandQuantity() - inv.getReservedQuantity(), inv.getAvailableQuantity());

        // Verify PO item reserved quantity is now 2
        POResponseDto poAfterInward = poService.getPurchaseOrderById(createdPo.getId());
        String poItemId = poAfterInward.getItems().get(0).getId();
        assertEquals(2, poAfterInward.getItems().get(0).getReservedQuantity());

        // Step 4: Godown Manager creates Delivery Challan for the 2 reserved units
        DCCreateDto dcDto = new DCCreateDto();
        dcDto.setPurchaseOrderId(poAfterInward.getId());
        dcDto.setVehicleNumber("MH-20-DE-1001");
        dcDto.setDriverName("Gajanan Jadhav");
        dcDto.setDriverPhone("+91 98220 77112");
        dcDto.setTransporterName("Agrisep Factory Logistics Division");
        dcDto.setLrNumber("LR-AG-2026-880");
        dcDto.setEwayBillNumber("341122334455");
        dcDto.setEwayBillDate(LocalDate.now());
        dcDto.setChallanDate(LocalDate.now());
        dcDto.setNotes("Complete consignment of 2 units GS-12 Gravity Separator");
        dcDto.setItems(List.of(new DCItemCreateDto(poItemId, 2, "Serial numbers: AG-GS12-001, AG-GS12-002")));

        DCResponseDto createdDc = dcService.createDeliveryChallan(dcDto, "usr-godown-001");
        assertNotNull(createdDc.getId());
        assertTrue(createdDc.getDcNumber().startsWith("DC-"));
        assertEquals(DCStatus.DRAFT, createdDc.getStatus());

        // Step 5: Physical Dispatch execution by Godown Manager
        DCResponseDto dispatchedDc = dcService.dispatchDeliveryChallan(createdDc.getId(), "usr-godown-001");
        assertEquals(DCStatus.DISPATCHED, dispatchedDc.getStatus());
        assertNotNull(dispatchedDc.getDispatchedAt());

        // Verify warehouse inventory updated: 3 - 2 = 1 on-hand, 0 reserved, 1 available
        Inventory invAfterDispatch = inventoryRepository.findByOfficeIdAndProductId(testOfficeId, testProductId).orElseThrow();
        assertEquals(1, invAfterDispatch.getOnHandQuantity());
        assertEquals(0, invAfterDispatch.getReservedQuantity());
        assertEquals(1, invAfterDispatch.getAvailableQuantity());

        // Verify PO transitioned to PARTIALLY_DELIVERED (in transit)
        POResponseDto dispatchedPo = poService.getPurchaseOrderById(createdPo.getId());
        assertEquals(POStatus.PARTIALLY_DELIVERED, dispatchedPo.getStatus());
        assertEquals(2, dispatchedPo.getItems().get(0).getDispatchedQuantity());
        assertEquals(0, dispatchedPo.getItems().get(0).getDeliveredQuantity());

        // Step 6: Recipient signs and confirms delivery at client processing facility
        DCDeliveryConfirmDto confirmDto = new DCDeliveryConfirmDto(
                "Rajendra Barwale",
                "+91 94222 11223",
                LocalDate.now(),
                "https://cdn.agrisep.com/documents/signed-pod/POD-2026-1001.pdf",
                "Delivered and assembled at Jalna seed conditioning plant in perfect condition."
        );
        DCResponseDto confirmedDc = dcService.confirmDelivery(dispatchedDc.getId(), confirmDto, "usr-main-001");
        assertEquals(DCStatus.DELIVERED, confirmedDc.getStatus());
        assertNotNull(confirmedDc.getDeliveredAt());
        assertEquals("Rajendra Barwale", confirmedDc.getRecipientName());

        // Step 7: Verify PO transitioned to final DELIVERED status
        POResponseDto finalPo = poService.getPurchaseOrderById(createdPo.getId());
        assertEquals(POStatus.DELIVERED, finalPo.getStatus());
        assertEquals(2, finalPo.getItems().get(0).getDeliveredQuantity());

        // Step 8: Verify Owner Dashboard metrics reflect the completed operations
        OwnerDashboardDto ownerDashboard = dashboardService.getOwnerDashboard();
        assertNotNull(ownerDashboard);
        assertTrue(ownerDashboard.getTotalClientCount() >= 1);
        assertTrue(ownerDashboard.getTotalPOCount() >= 1);

        // Step 9: Verify audit trail contains records of each step
        var auditLogs = auditLogService.searchLogs(null, null, null, null, null, PageRequest.of(0, 50));
        assertFalse(auditLogs.getItems().isEmpty());
    }

    @Test
    @DisplayName("Partial fulfillment flow: PO for 3 units -> Partial Dispatch 1 unit -> Partial Dispatch remaining 2 units -> Complete Delivery")
    void testPartialDeliveryAndMultiChallanFlow() {
        // Stock 1 unit initially
        inventoryService.recordInward(
                new StockInwardDto(testOfficeId, testProductId, 1, 2, "Batch Partial-1"),
                "usr-godown-001"
        );

        // Create PO for 3 units
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClientId);
        poDto.setOfficeId(testOfficeId);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(2));
        poDto.setSubmitForApproval(true);
        poDto.setItems(List.of(
                new POItemCreateDto(testProductId, 3, new BigDecimal("1450000.00"), null, null)
        ));

        POResponseDto po = poService.createPurchaseOrder(poDto, "usr-main-001");
        po = poService.approvePurchaseOrder(po.getId(), "usr-owner-001", "Approved for split dispatch");

        // 1 reserved, 2 in shortage
        assertEquals(1, po.getItems().get(0).getReservedQuantity());
        String poItemId = po.getItems().get(0).getId();

        // Dispatch DC 1 for 1 unit
        DCCreateDto dc1Dto = new DCCreateDto();
        dc1Dto.setPurchaseOrderId(po.getId());
        dc1Dto.setVehicleNumber("MH-20-DE-2001");
        dc1Dto.setDriverName("Kailash Shinde");
        dc1Dto.setDriverPhone("+91 94220 88223");
        dc1Dto.setTransporterName("Agrisep Express");
        dc1Dto.setItems(List.of(new DCItemCreateDto(poItemId, 1, "First unit")));
        DCResponseDto dc1 = dcService.createDeliveryChallan(dc1Dto, "usr-godown-001");
        dcService.dispatchDeliveryChallan(dc1.getId(), "usr-godown-001");

        POResponseDto poAfterDc1 = poService.getPurchaseOrderById(po.getId());
        assertEquals(POStatus.PARTIALLY_DELIVERED, poAfterDc1.getStatus());
        assertEquals(1, poAfterDc1.getItems().get(0).getDispatchedQuantity());

        // Inward remaining 2 units
        inventoryService.recordInward(
                new StockInwardDto(testOfficeId, testProductId, 2, 2, "Batch Partial-2"),
                "usr-godown-001"
        );

        // Dispatch DC 2 for remaining 2 units
        DCCreateDto dc2Dto = new DCCreateDto();
        dc2Dto.setPurchaseOrderId(po.getId());
        dc2Dto.setVehicleNumber("MH-20-DE-2002");
        dc2Dto.setDriverName("Vijay Patil");
        dc2Dto.setDriverPhone("+91 94220 88224");
        dc2Dto.setTransporterName("Agrisep Express");
        dc2Dto.setItems(List.of(new DCItemCreateDto(poItemId, 2, "Remaining 2 units")));
        DCResponseDto dc2 = dcService.createDeliveryChallan(dc2Dto, "usr-godown-001");
        dcService.dispatchDeliveryChallan(dc2.getId(), "usr-godown-001");

        // Deliver DC 1
        DCDeliveryConfirmDto confirm1 = new DCDeliveryConfirmDto("Rajendra Barwale", "+91 94222 11223", LocalDate.now(), null, "DC 1 received");
        dcService.confirmDelivery(dc1.getId(), confirm1, "usr-main-001");

        // PO should still be PARTIALLY_DELIVERED because DC 2 is not yet confirmed
        POResponseDto poAfterConfirm1 = poService.getPurchaseOrderById(po.getId());
        assertEquals(POStatus.PARTIALLY_DELIVERED, poAfterConfirm1.getStatus());
        assertEquals(1, poAfterConfirm1.getItems().get(0).getDeliveredQuantity());
        assertEquals(3, poAfterConfirm1.getItems().get(0).getDispatchedQuantity());

        // Deliver DC 2
        DCDeliveryConfirmDto confirm2 = new DCDeliveryConfirmDto("Rajendra Barwale", "+91 94222 11223", LocalDate.now(), null, "DC 2 received");
        dcService.confirmDelivery(dc2.getId(), confirm2, "usr-main-001");

        // PO should now be fully DELIVERED
        POResponseDto poFinal = poService.getPurchaseOrderById(po.getId());
        assertEquals(POStatus.DELIVERED, poFinal.getStatus());
        assertEquals(3, poFinal.getItems().get(0).getDeliveredQuantity());
    }
}
