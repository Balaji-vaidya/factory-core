package com.factory.delivery;

import com.factory.client.dto.ClientAddressDto;
import com.factory.client.dto.ClientCreateDto;
import com.factory.client.dto.ClientResponseDto;
import com.factory.client.entity.ClientType;
import com.factory.client.service.ClientService;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.delivery.dto.DCCreateDto;
import com.factory.delivery.dto.DCDeliveryConfirmDto;
import com.factory.delivery.dto.DCItemCreateDto;
import com.factory.delivery.dto.DCResponseDto;
import com.factory.delivery.entity.DCStatus;
import com.factory.delivery.service.DeliveryChallanService;
import com.factory.inventory.dto.StockInwardDto;
import com.factory.inventory.entity.Inventory;
import com.factory.inventory.repository.InventoryRepository;
import com.factory.inventory.service.InventoryService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class DeliveryChallanServiceIntegrationTests {

    @Autowired
    private DeliveryChallanService dcService;

    @Autowired
    private PurchaseOrderService poService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ClientService clientService;

    private String testOfficeId;
    private String testClientId;
    private String testProductId;

    @BeforeEach
    void setUp() {
        Office testGodown = new Office(
                "off-dc-test",
                "Jalna Dispatch Godown",
                OfficeType.WAREHOUSE_GODOWN,
                "Maharashtra",
                "Jalna",
                "Plot 44, Phase 3, Jalna MIDC",
                "27AAACI4921C1Z7",
                "+91 2482 288777",
                "dispatch.godown@agrisep.com"
        );
        officeRepository.save(testGodown);
        testOfficeId = testGodown.getId();

        ClientCreateDto clientDto = new ClientCreateDto();
        clientDto.setCompanyName("Syngenta India Seed Logistics Account");
        clientDto.setClientType(ClientType.SEED_PRODUCER);
        clientDto.setGstin("27AABCS7777J1ZY");
        clientDto.setPan("AABCS7777J");
        clientDto.setState("Maharashtra");
        clientDto.setPrimaryContactName("Vikas Deshmukh");
        clientDto.setPrimaryContactPhone("+91 94221 66778");
        clientDto.setPrimaryContactEmail("vikas.deshmukh@syngenta.test");

        ClientAddressDto addressDto = new ClientAddressDto();
        addressDto.setSiteName("Chhatrapati Sambhajinagar Distribution Center");
        addressDto.setAddressLine1("Plot 105, Waluj Industrial Area");
        addressDto.setCity("Chhatrapati Sambhajinagar");
        addressDto.setState("Maharashtra");
        addressDto.setPincode("431136");
        addressDto.setContactPerson("Vikas Deshmukh");
        addressDto.setPhone("+91 94221 66778");
        clientDto.setInitialAddress(addressDto);

        ClientResponseDto client = clientService.createClient(clientDto, "usr-owner-001");
        testClientId = client.getId();

        ProductCreateDto p = new ProductCreateDto();
        p.setSku("DC-TEST-GS10");
        p.setName("Agrisep® Dispatch Ready Gravity Separator");
        p.setCategory(ProductCategory.GRAVITY_SEPARATOR);
        p.setUnit(ProductUnit.SET);
        p.setBasePriceINR(new BigDecimal("1200000.00"));
        p.setLeadTimeWeeks(4);
        ProductResponseDto prod = productService.createProduct(p);
        testProductId = prod.getId();
    }

    @Test
    @DisplayName("Create delivery challan assigns sequential DC number and sets DRAFT status")
    void testCreateDeliveryChallan_SequentialNumber_WithinReservedStock() {
        // Stock in godown: 8 units
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 8, 2, "Batch GS"), "usr-owner-001");

        // PO for 4 units, approved (4 units reserved)
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClientId);
        poDto.setOfficeId(testOfficeId);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(2));
        poDto.setSubmitForApproval(true);
        poDto.setItems(List.of(new POItemCreateDto(testProductId, 4, new BigDecimal("1200000.00"), null, null)));
        POResponseDto po = poService.createPurchaseOrder(poDto, "usr-main-001");
        po = poService.approvePurchaseOrder(po.getId(), "usr-owner-001", "PO Approved for dispatch");

        String poItemId = po.getItems().get(0).getId();

        // Create partial DC for 2 units
        DCCreateDto dcDto = new DCCreateDto();
        dcDto.setPurchaseOrderId(po.getId());
        dcDto.setVehicleNumber("MH-21-BH-4512");
        dcDto.setDriverName("Santosh Shinde");
        dcDto.setDriverPhone("+91 98221 55667");
        dcDto.setTransporterName("VRL Logistics Ltd");
        dcDto.setLrNumber("LR-2026-9918");
        dcDto.setEwayBillNumber("241890123456");
        dcDto.setEwayBillDate(LocalDate.now());
        dcDto.setChallanDate(LocalDate.now());
        dcDto.setNotes("First consignment of 2 units via open body truck.");
        dcDto.setItems(List.of(new DCItemCreateDto(poItemId, 2, "GS-10 Set 1 & 2")));

        DCResponseDto dc = dcService.createDeliveryChallan(dcDto, "usr-godown-001");

        assertNotNull(dc.getId());
        assertTrue(dc.getDcNumber().startsWith("DC-"));
        assertEquals(DCStatus.DRAFT, dc.getStatus());
        assertEquals("MH-21-BH-4512", dc.getVehicleNumber());
        assertEquals(1, dc.getItems().size());
        assertEquals(2, dc.getItems().get(0).getQuantity());
        assertNotNull(dc.getDeliveryAddressSnapshot());
    }

    @Test
    @DisplayName("Create DC fails with DISPATCH_LIMIT_EXCEEDED when requested quantity exceeds reserved stock")
    void testCreateDeliveryChallan_ExceedingReservedStock_ThrowsDispatchLimitExceeded() {
        // Stock in godown: only 2 units available
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 2, 2, "Initial batch"), "usr-owner-001");

        // PO for 4 units, approved (2 reserved, 2 in shortage)
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClientId);
        poDto.setOfficeId(testOfficeId);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(3));
        poDto.setSubmitForApproval(true);
        poDto.setItems(List.of(new POItemCreateDto(testProductId, 4, new BigDecimal("1200000.00"), null, null)));
        POResponseDto po = poService.createPurchaseOrder(poDto, "usr-main-001");
        po = poService.approvePurchaseOrder(po.getId(), "usr-owner-001", "PO Approved with partial shortage");

        String poItemId = po.getItems().get(0).getId();

        // Attempting to create DC for 3 units (only 2 reserved in godown)
        DCCreateDto dcDto = new DCCreateDto();
        dcDto.setPurchaseOrderId(po.getId());
        dcDto.setVehicleNumber("MH-21-BH-9999");
        dcDto.setDriverName("Ganesh Pawar");
        dcDto.setDriverPhone("+91 98221 11223");
        dcDto.setTransporterName("Navata Road Transport");
        dcDto.setItems(List.of(new DCItemCreateDto(poItemId, 3, "Invalid excess dispatch")));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                dcService.createDeliveryChallan(dcDto, "usr-godown-001"));

        assertEquals(ErrorCode.DISPATCH_LIMIT_EXCEEDED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Physical dispatch deducts physical and reserved inventory, updating PO to PARTIALLY_DELIVERED")
    void testDispatchDeliveryChallan_AtomicInventoryDeduction_UpdatesPOToPartiallyDelivered() {
        // Stock: 8 units
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 8, 2, "Batch"), "usr-owner-001");

        // PO for 4 units approved
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClientId);
        poDto.setOfficeId(testOfficeId);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(2));
        poDto.setSubmitForApproval(true);
        poDto.setItems(List.of(new POItemCreateDto(testProductId, 4, new BigDecimal("1200000.00"), null, null)));
        POResponseDto po = poService.createPurchaseOrder(poDto, "usr-main-001");
        po = poService.approvePurchaseOrder(po.getId(), "usr-owner-001", "Approved");
        String poItemId = po.getItems().get(0).getId();

        // Create DC for 2 units
        DCCreateDto dcDto = new DCCreateDto();
        dcDto.setPurchaseOrderId(po.getId());
        dcDto.setVehicleNumber("MH-21-BH-4512");
        dcDto.setDriverName("Santosh Shinde");
        dcDto.setDriverPhone("+91 98221 55667");
        dcDto.setTransporterName("VRL Logistics Ltd");
        dcDto.setItems(List.of(new DCItemCreateDto(poItemId, 2, "2 units")));
        DCResponseDto dc = dcService.createDeliveryChallan(dcDto, "usr-godown-001");

        // Inventory before dispatch: onHand = 8, reserved = 4, available = 4
        Inventory invBefore = inventoryRepository.findByOfficeIdAndProductId(testOfficeId, testProductId).orElseThrow();
        assertEquals(8, invBefore.getOnHandQuantity());
        assertEquals(4, invBefore.getReservedQuantity());
        assertEquals(4, invBefore.getAvailableQuantity());

        // Dispatch DC
        DCResponseDto dispatched = dcService.dispatchDeliveryChallan(dc.getId(), "usr-godown-001");

        assertEquals(DCStatus.DISPATCHED, dispatched.getStatus());
        assertNotNull(dispatched.getDispatchedAt());

        // Inventory after dispatch: onHand = 6, reserved = 2, available = 4
        Inventory invAfter = inventoryRepository.findByOfficeIdAndProductId(testOfficeId, testProductId).orElseThrow();
        assertEquals(6, invAfter.getOnHandQuantity());
        assertEquals(2, invAfter.getReservedQuantity());
        assertEquals(4, invAfter.getAvailableQuantity());

        // PO status is now PARTIALLY_DELIVERED
        POResponseDto updatedPo = poService.getPurchaseOrderById(po.getId());
        assertEquals(POStatus.PARTIALLY_DELIVERED, updatedPo.getStatus());
        assertEquals(2, updatedPo.getItems().get(0).getDispatchedQuantity());
        assertEquals(2, updatedPo.getItems().get(0).getReservedQuantity());
    }

    @Test
    @DisplayName("Delivery confirmation records recipient signatory and transitions PO to DELIVERED once all items arrive")
    void testConfirmDelivery_CompletesPOWhenAllItemsDelivered() {
        // Stock: 4 units
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 4, 2, "Batch D"), "usr-owner-001");

        // PO for 4 units approved
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClientId);
        poDto.setOfficeId(testOfficeId);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(2));
        poDto.setSubmitForApproval(true);
        poDto.setItems(List.of(new POItemCreateDto(testProductId, 4, new BigDecimal("1200000.00"), null, null)));
        POResponseDto po = poService.createPurchaseOrder(poDto, "usr-main-001");
        po = poService.approvePurchaseOrder(po.getId(), "usr-owner-001", "Approved");
        String poItemId = po.getItems().get(0).getId();

        // Single DC for all 4 units
        DCCreateDto dcDto = new DCCreateDto();
        dcDto.setPurchaseOrderId(po.getId());
        dcDto.setVehicleNumber("MH-21-BH-1122");
        dcDto.setDriverName("Anil Rathod");
        dcDto.setDriverPhone("+91 98221 44332");
        dcDto.setTransporterName("TCI Express");
        dcDto.setItems(List.of(new DCItemCreateDto(poItemId, 4, "Complete order")));
        DCResponseDto dc = dcService.createDeliveryChallan(dcDto, "usr-godown-001");

        // Physical dispatch
        dcService.dispatchDeliveryChallan(dc.getId(), "usr-godown-001");

        // Delivery confirmation at client site
        DCDeliveryConfirmDto confirmDto = new DCDeliveryConfirmDto(
                "Suresh Gaikwad",
                "+91 94221 99881",
                LocalDate.now(),
                "https://documents.agrisep.com/pod/pod-2026-001.pdf",
                "Unloaded and inspected in presence of Agrisep commissioning engineer."
        );
        DCResponseDto deliveredDc = dcService.confirmDelivery(dc.getId(), confirmDto, "usr-reg-001");

        assertEquals(DCStatus.DELIVERED, deliveredDc.getStatus());
        assertNotNull(deliveredDc.getDeliveredAt());
        assertEquals("Suresh Gaikwad", deliveredDc.getRecipientName());

        // PO is now fully DELIVERED
        POResponseDto finalPo = poService.getPurchaseOrderById(po.getId());
        assertEquals(POStatus.DELIVERED, finalPo.getStatus());
        assertEquals(4, finalPo.getItems().get(0).getDeliveredQuantity());
    }
}
