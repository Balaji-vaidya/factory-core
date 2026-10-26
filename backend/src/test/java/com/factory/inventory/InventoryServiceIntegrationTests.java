package com.factory.inventory;

import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import com.factory.inventory.dto.InventoryResponseDto;
import com.factory.inventory.dto.StockAdjustmentDto;
import com.factory.inventory.dto.StockInwardDto;
import com.factory.inventory.dto.StockLedgerResponseDto;
import com.factory.inventory.entity.Inventory;
import com.factory.inventory.entity.StockTransactionType;
import com.factory.inventory.service.InventoryService;
import com.factory.office.entity.Office;
import com.factory.office.entity.OfficeType;
import com.factory.office.repository.OfficeRepository;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class InventoryServiceIntegrationTests {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private ProductService productService;

    private String testOfficeId;
    private String testProductId;

    @BeforeEach
    void setUp() {
        Office testOffice = new Office(
                "off-inv-test",
                "Jalna Test Godown",
                OfficeType.WAREHOUSE_GODOWN,
                "Maharashtra",
                "Jalna",
                "Plot 88, Phase 2, Jalna MIDC",
                "27AAACI4921C1Z4",
                "+91 2482 233444",
                "godown.test@agrisep.com"
        );
        officeRepository.save(testOffice);
        testOfficeId = testOffice.getId();

        ProductCreateDto p = new ProductCreateDto();
        p.setSku("INV-TEST-01");
        p.setName("Agrisep® Test Seed Grader");
        p.setCategory(ProductCategory.GRAVITY_SEPARATOR);
        p.setUnit(ProductUnit.SET);
        p.setBasePriceINR(new BigDecimal("950000.00"));
        p.setLeadTimeWeeks(3);
        ProductResponseDto createdProd = productService.createProduct(p);
        testProductId = createdProd.getId();
    }

    @Test
    @DisplayName("Record stock inward creates inventory, updates on-hand & available, and creates ledger entry")
    void testRecordInward_UpdatesOnHandAndAvailable_CreatesStockLedgerEntry() {
        StockInwardDto inward = new StockInwardDto(testOfficeId, testProductId, 10, 2, "Production batch PB-2026-001");
        InventoryResponseDto res = inventoryService.recordInward(inward, "usr-owner-001");

        assertNotNull(res.getId());
        assertEquals(10, res.getOnHandQuantity());
        assertEquals(0, res.getReservedQuantity());
        assertEquals(10, res.getAvailableQuantity());
        assertFalse(res.isLowStock());

        // Verify ledger entry
        PageResponse<StockLedgerResponseDto> ledger = inventoryService.searchLedger(
                testOfficeId, testProductId, StockTransactionType.INWARD, PageRequest.of(0, 10));

        assertEquals(1, ledger.getTotalItems());
        StockLedgerResponseDto entry = ledger.getItems().get(0);
        assertEquals(10, entry.getQuantityDelta());
        assertEquals(0, entry.getOnHandBefore());
        assertEquals(10, entry.getOnHandAfter());
        assertEquals(0, entry.getReservedBefore());
        assertEquals(0, entry.getReservedAfter());
        assertEquals(0, entry.getAvailableBefore());
        assertEquals(10, entry.getAvailableAfter());
        assertEquals("Production batch PB-2026-001", entry.getNotes());
    }

    @Test
    @DisplayName("Manual stock adjustment with mandatory reason successfully updates balance and records audit trail")
    void testManualAdjustment_WithMandatoryReason_UpdatesBalance() {
        // Initial inward: 10
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 10, 2, "Initial stock"), "usr-owner-001");

        // Physical inventory audit found 12 units
        StockAdjustmentDto adj = new StockAdjustmentDto(
                testOfficeId, testProductId, 12, "Annual audit found 2 additional unassembled sets in Bay B.");
        InventoryResponseDto updated = inventoryService.adjustStock(adj, "usr-owner-001");

        assertEquals(12, updated.getOnHandQuantity());
        assertEquals(0, updated.getReservedQuantity());
        assertEquals(12, updated.getAvailableQuantity());
        assertNotNull(updated.getLastAdjustedAt());

        // Verify ledger entry
        PageResponse<StockLedgerResponseDto> ledger = inventoryService.searchLedger(
                testOfficeId, testProductId, StockTransactionType.MANUAL_ADJUSTMENT, PageRequest.of(0, 10));
        assertEquals(1, ledger.getTotalItems());
        assertEquals(2, ledger.getItems().get(0).getQuantityDelta());
        assertEquals("Annual audit found 2 additional unassembled sets in Bay B.", ledger.getItems().get(0).getNotes());
    }

    @Test
    @DisplayName("Manual stock adjustment without mandatory justification throws ValidationException")
    void testManualAdjustment_MissingReason_ThrowsValidationException() {
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 5, 2, "Initial"), "usr-owner-001");

        StockAdjustmentDto blankReason = new StockAdjustmentDto(testOfficeId, testProductId, 8, "   ");
        BusinessException ex = assertThrows(BusinessException.class, () ->
                inventoryService.adjustStock(blankReason, "usr-owner-001"));

        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Manual adjustment attempting to set on-hand below reserved quantity throws InvariantViolation")
    void testManualAdjustment_BelowReservedQuantity_ThrowsInvariantViolation() {
        // Initial inward: 5
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 5, 2, "Initial"), "usr-owner-001");

        // Reserve 3 units
        inventoryService.reserveStock(testOfficeId, testProductId, 3, "po-123", "PO-000001", "usr-owner-001");

        // Attempt to adjust on-hand to 2 (which is less than 3 reserved)
        StockAdjustmentDto invalidAdj = new StockAdjustmentDto(
                testOfficeId, testProductId, 2, "Discovered water damage on physical units.");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                inventoryService.adjustStock(invalidAdj, "usr-owner-001"));

        assertEquals(ErrorCode.INVENTORY_INVARIANT_VIOLATION, ex.getErrorCode());
    }

    @Test
    @DisplayName("Stock reservation decrements available and increments reserved while maintaining on-hand")
    void testReserveStock_Success_DecrementsAvailable_IncrementsReserved() {
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 8, 2, "Initial"), "usr-owner-001");

        Inventory reserved = inventoryService.reserveStock(
                testOfficeId, testProductId, 5, "po-test-99", "PO-000099", "usr-owner-001");

        assertEquals(8, reserved.getOnHandQuantity());
        assertEquals(5, reserved.getReservedQuantity());
        assertEquals(3, reserved.getAvailableQuantity()); // 8 - 5 = 3

        // Ledger verification
        PageResponse<StockLedgerResponseDto> ledger = inventoryService.searchLedger(
                testOfficeId, testProductId, StockTransactionType.PO_RESERVATION, PageRequest.of(0, 10));
        assertEquals(1, ledger.getTotalItems());
        assertEquals(5, ledger.getItems().get(0).getQuantityDelta());
        assertEquals(8, ledger.getItems().get(0).getAvailableBefore());
        assertEquals(3, ledger.getItems().get(0).getAvailableAfter());
    }

    @Test
    @DisplayName("Stock reservation when requested quantity exceeds available stock throws InsufficientStock")
    void testReserveStock_InsufficientStock_ThrowsInsufficientStockException() {
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 4, 2, "Initial"), "usr-owner-001");

        // Attempt to reserve 5 units (only 4 available)
        BusinessException ex = assertThrows(BusinessException.class, () ->
                inventoryService.reserveStock(testOfficeId, testProductId, 5, "po-test-01", "PO-000001", "usr-owner-001"));

        assertEquals(ErrorCode.INSUFFICIENT_STOCK, ex.getErrorCode());
    }

    @Test
    @DisplayName("Physical dispatch decrements on-hand and reserved, preserving available invariant")
    void testDispatchStock_DecrementsOnHandAndReserved_PreservesAvailable() {
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 10, 2, "Initial"), "usr-owner-001");
        inventoryService.reserveStock(testOfficeId, testProductId, 6, "po-404", "PO-000404", "usr-owner-001");
        // On hand: 10, Reserved: 6, Available: 4

        // Dispatch 4 units under DC
        Inventory dispatched = inventoryService.dispatchStock(
                testOfficeId, testProductId, 4, "dc-501", "DC-000001", "usr-owner-001");

        // On hand: 10 - 4 = 6
        // Reserved: 6 - 4 = 2
        // Available: 6 - 2 = 4 (available remains 4 because dispatched units were already reserved!)
        assertEquals(6, dispatched.getOnHandQuantity());
        assertEquals(2, dispatched.getReservedQuantity());
        assertEquals(4, dispatched.getAvailableQuantity());

        // Check ledger
        PageResponse<StockLedgerResponseDto> ledger = inventoryService.searchLedger(
                testOfficeId, testProductId, StockTransactionType.DC_DISPATCH, PageRequest.of(0, 10));
        assertEquals(1, ledger.getTotalItems());
        assertEquals(-4, ledger.getItems().get(0).getQuantityDelta());
        assertEquals(10, ledger.getItems().get(0).getOnHandBefore());
        assertEquals(6, ledger.getItems().get(0).getOnHandAfter());
    }

    @Test
    @DisplayName("Release reservation decrements reserved and restores available stock")
    void testReleaseReservation_DecrementsReserved_RestoresAvailable() {
        inventoryService.recordInward(new StockInwardDto(testOfficeId, testProductId, 6, 2, "Initial"), "usr-owner-001");
        inventoryService.reserveStock(testOfficeId, testProductId, 4, "po-777", "PO-000777", "usr-owner-001");
        // Reserved: 4, Available: 2

        // PO cancelled or amended: release 3 units
        Inventory released = inventoryService.releaseReservation(
                testOfficeId, testProductId, 3, "po-777", "Client reduced order quantity", "usr-owner-001");

        assertEquals(6, released.getOnHandQuantity());
        assertEquals(1, released.getReservedQuantity());
        assertEquals(5, released.getAvailableQuantity()); // 6 - 1 = 5
    }
}
