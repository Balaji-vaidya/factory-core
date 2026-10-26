package com.factory.inventory;

import com.factory.client.dto.ClientAddressDto;
import com.factory.client.dto.ClientCreateDto;
import com.factory.client.dto.ClientResponseDto;
import com.factory.client.entity.ClientType;
import com.factory.client.service.ClientService;
import com.factory.inventory.dto.StockInwardDto;
import com.factory.inventory.dto.StockShortageResponseDto;
import com.factory.inventory.entity.Inventory;
import com.factory.inventory.entity.ShortageStatus;
import com.factory.inventory.repository.InventoryRepository;
import com.factory.inventory.service.InventoryService;
import com.factory.inventory.service.ShortageAllocationService;
import com.factory.office.entity.Office;
import com.factory.office.entity.OfficeType;
import com.factory.office.repository.OfficeRepository;
import com.factory.po.dto.POCreateDto;
import com.factory.po.dto.POItemCreateDto;
import com.factory.po.dto.POResponseDto;
import com.factory.po.entity.POPriority;
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
public class ShortageAndReservationIntegrationTests {

    @Autowired
    private PurchaseOrderService poService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ShortageAllocationService shortageAllocationService;

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
                "off-shortage-test",
                "Jalna Allocation Godown",
                OfficeType.WAREHOUSE_GODOWN,
                "Maharashtra",
                "Jalna",
                "Phase 4 Industrial Zone, Jalna",
                "27AAACI4921C1Z6",
                "+91 2482 299888",
                "allocation.godown@agrisep.com"
        );
        officeRepository.save(testGodown);
        testOfficeId = testGodown.getId();

        ClientCreateDto clientDto = new ClientCreateDto();
        clientDto.setCompanyName("Mahyco Hybrid Seeds Allocation Account");
        clientDto.setClientType(ClientType.SEED_PRODUCER);
        clientDto.setGstin("27AABCM8888H1ZV");
        clientDto.setPan("AABCM8888H");
        clientDto.setState("Maharashtra");
        clientDto.setPrimaryContactName("Ramesh Shinde");
        clientDto.setPrimaryContactPhone("+91 94221 44556");
        clientDto.setPrimaryContactEmail("ramesh.shinde@mahyco.test");

        ClientAddressDto addressDto = new ClientAddressDto();
        addressDto.setSiteName("Main Processing Facility");
        addressDto.setAddressLine1("Plot 12, Dawadwadi");
        addressDto.setCity("Jalna");
        addressDto.setState("Maharashtra");
        addressDto.setPincode("431203");
        addressDto.setContactPerson("Ramesh Shinde");
        addressDto.setPhone("+91 94221 44556");
        clientDto.setInitialAddress(addressDto);

        ClientResponseDto client = clientService.createClient(clientDto, "usr-owner-001");
        testClientId = client.getId();

        ProductCreateDto p = new ProductCreateDto();
        p.setSku("ALLOC-GS-10");
        p.setName("Agrisep® Allocation Gravity Separator");
        p.setCategory(ProductCategory.GRAVITY_SEPARATOR);
        p.setUnit(ProductUnit.SET);
        p.setBasePriceINR(new BigDecimal("1200000.00"));
        p.setLeadTimeWeeks(4);
        ProductResponseDto prod = productService.createProduct(p);
        testProductId = prod.getId();
    }

    @Test
    @DisplayName("When stock is sufficient, Owner approval fully reserves PO items without creating shortages")
    void testFullReservation_OnPOApproval_WhenStockSufficient() {
        // Stock in godown: 10 units
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 10, 2, "Batch 1"), "usr-owner-001");

        // Create PO for 4 units
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClientId);
        poDto.setOfficeId(testOfficeId);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(3));
        poDto.setSubmitForApproval(true);
        poDto.setItems(List.of(new POItemCreateDto(testProductId, 4, new BigDecimal("1200000.00"), null, null)));

        POResponseDto po = poService.createPurchaseOrder(poDto, "usr-main-001");
        assertEquals(POStatus.PENDING_OWNER_APPROVAL, po.getStatus());

        // Owner approves PO
        POResponseDto approved = poService.approvePurchaseOrder(po.getId(), "usr-owner-001", "Full stock ready.");
        assertEquals(POStatus.APPROVED, approved.getStatus());

        // Verify inventory: onHand = 10, reserved = 4, available = 6
        Inventory inv = inventoryRepository.findByOfficeIdAndProductId(testOfficeId, testProductId).orElseThrow();
        assertEquals(10, inv.getOnHandQuantity());
        assertEquals(4, inv.getReservedQuantity());
        assertEquals(6, inv.getAvailableQuantity());

        // PO item has reservedQuantity = 4
        assertEquals(4, approved.getItems().get(0).getReservedQuantity());

        // Zero open shortages
        var shortages = shortageAllocationService.searchShortages(testOfficeId, testProductId, ShortageStatus.OPEN, PageRequest.of(0, 10));
        assertEquals(0, shortages.getTotalItems());
    }

    @Test
    @DisplayName("When stock is insufficient, Owner approval reserves available stock and generates tracked shortage")
    void testPartialReservationAndShortageGeneration_WhenStockInsufficient() {
        // Stock in godown: only 2 units available
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 2, 2, "Batch Partial"), "usr-owner-001");

        // Create PO for 5 units
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClientId);
        poDto.setOfficeId(testOfficeId);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(4));
        poDto.setSubmitForApproval(true);
        poDto.setItems(List.of(new POItemCreateDto(testProductId, 5, new BigDecimal("1200000.00"), null, null)));

        POResponseDto po = poService.createPurchaseOrder(poDto, "usr-main-001");

        // Owner approves PO
        POResponseDto approved = poService.approvePurchaseOrder(po.getId(), "usr-owner-001", "Approved. 2 in stock, 3 on backorder.");
        assertEquals(POStatus.APPROVED, approved.getStatus());

        // Inventory: onHand = 2, reserved = 2, available = 0
        Inventory inv = inventoryRepository.findByOfficeIdAndProductId(testOfficeId, testProductId).orElseThrow();
        assertEquals(2, inv.getOnHandQuantity());
        assertEquals(2, inv.getReservedQuantity());
        assertEquals(0, inv.getAvailableQuantity());

        // Shortage tracked
        var shortages = shortageAllocationService.searchShortages(testOfficeId, testProductId, ShortageStatus.PARTIALLY_RESOLVED, PageRequest.of(0, 10));
        assertEquals(1, shortages.getTotalItems());
        StockShortageResponseDto shortage = shortages.getItems().get(0);
        assertEquals(5, shortage.getRequiredQuantity());
        assertEquals(2, shortage.getAllocatedQuantity());
        assertEquals(3, shortage.getShortageQuantity());
    }

    @Test
    @DisplayName("Deterministic FIFO priority allocation resolves shortages chronologically on inward stock arrival")
    void testDeterministicFIFOShortageAllocation_OnStockInward() {
        // Initial stock = 0
        // PO 1: created 3 days ago, needs 3 units
        POCreateDto po1Dto = new POCreateDto();
        po1Dto.setClientId(testClientId);
        po1Dto.setOfficeId(testOfficeId);
        po1Dto.setPoDate(LocalDate.now().minusDays(3));
        po1Dto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(2));
        po1Dto.setSubmitForApproval(true);
        po1Dto.setItems(List.of(new POItemCreateDto(testProductId, 3, new BigDecimal("1200000.00"), null, null)));
        POResponseDto po1 = poService.createPurchaseOrder(po1Dto, "usr-main-001");
        poService.approvePurchaseOrder(po1.getId(), "usr-owner-001", "PO1 approved");

        // PO 2: created 1 day ago, needs 4 units
        POCreateDto po2Dto = new POCreateDto();
        po2Dto.setClientId(testClientId);
        po2Dto.setOfficeId(testOfficeId);
        po2Dto.setPoDate(LocalDate.now().minusDays(1));
        po2Dto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(3));
        po2Dto.setSubmitForApproval(true);
        po2Dto.setItems(List.of(new POItemCreateDto(testProductId, 4, new BigDecimal("1200000.00"), null, null)));
        POResponseDto po2 = poService.createPurchaseOrder(po2Dto, "usr-main-001");
        poService.approvePurchaseOrder(po2.getId(), "usr-owner-001", "PO2 approved");

        // Factory inward receipt arrives: 5 units manufactured
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 5, 2, "Factory inward Lot 99"), "usr-owner-001");

        // Inward of 5 units must strictly allocate in FIFO order:
        // PO 1 (older poDate) receives 3 units -> completely RESOLVED (0 shortage left)
        // PO 2 (newer poDate) receives remaining 2 units -> PARTIALLY_RESOLVED (2 shortage left)
        var po1Shortage = shortageAllocationService.searchShortages(testOfficeId, testProductId, ShortageStatus.RESOLVED, PageRequest.of(0, 10));
        assertEquals(1, po1Shortage.getTotalItems());
        assertEquals(po1.getPoNumber(), po1Shortage.getItems().get(0).getPoNumber());
        assertEquals(3, po1Shortage.getItems().get(0).getAllocatedQuantity());
        assertEquals(0, po1Shortage.getItems().get(0).getShortageQuantity());

        var po2Shortage = shortageAllocationService.searchShortages(testOfficeId, testProductId, ShortageStatus.PARTIALLY_RESOLVED, PageRequest.of(0, 10));
        assertEquals(1, po2Shortage.getTotalItems());
        assertEquals(po2.getPoNumber(), po2Shortage.getItems().get(0).getPoNumber());
        assertEquals(2, po2Shortage.getItems().get(0).getAllocatedQuantity());
        assertEquals(2, po2Shortage.getItems().get(0).getShortageQuantity());

        // Inventory state: onHand = 5, reserved = 5 (3 for PO1, 2 for PO2), available = 0
        Inventory inv = inventoryRepository.findByOfficeIdAndProductId(testOfficeId, testProductId).orElseThrow();
        assertEquals(5, inv.getOnHandQuantity());
        assertEquals(5, inv.getReservedQuantity());
        assertEquals(0, inv.getAvailableQuantity());
    }

    @Test
    @DisplayName("Returning or cancelling an approved PO releases its reserved stock back to Available")
    void testReservationRollback_OnPOReturn() {
        // Stock: 6 units
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 6, 2, "Batch R"), "usr-owner-001");

        // Create and approve PO for 4 units
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClientId);
        poDto.setOfficeId(testOfficeId);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusWeeks(3));
        poDto.setSubmitForApproval(true);
        poDto.setItems(List.of(new POItemCreateDto(testProductId, 4, new BigDecimal("1200000.00"), null, null)));
        POResponseDto po = poService.createPurchaseOrder(poDto, "usr-main-001");
        poService.approvePurchaseOrder(po.getId(), "usr-owner-001", "Approved");

        // 4 units are reserved (onHand = 6, reserved = 4, available = 2)
        Inventory invAfterApproval = inventoryRepository.findByOfficeIdAndProductId(testOfficeId, testProductId).orElseThrow();
        assertEquals(4, invAfterApproval.getReservedQuantity());
        assertEquals(2, invAfterApproval.getAvailableQuantity());

        // Return PO for revision
        poService.returnPurchaseOrder(po.getId(), "usr-owner-001", "Client requested motor rating change from TEFC to Flameproof.");

        // Reserved stock is rolled back to Available (reserved = 0, available = 6)
        Inventory invAfterReturn = inventoryRepository.findByOfficeIdAndProductId(testOfficeId, testProductId).orElseThrow();
        assertEquals(0, invAfterReturn.getReservedQuantity());
        assertEquals(6, invAfterReturn.getAvailableQuantity());
    }
}
