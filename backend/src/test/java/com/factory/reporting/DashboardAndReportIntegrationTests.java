package com.factory.reporting;

import com.factory.client.dto.ClientCreateDto;
import com.factory.client.dto.ClientResponseDto;
import com.factory.client.entity.ClientType;
import com.factory.client.service.ClientService;
import com.factory.inventory.dto.StockInwardDto;
import com.factory.inventory.service.InventoryService;
import com.factory.office.entity.Office;
import com.factory.office.entity.OfficeType;
import com.factory.office.repository.OfficeRepository;
import com.factory.po.dto.POCreateDto;
import com.factory.po.dto.POItemCreateDto;
import com.factory.po.dto.POResponseDto;
import com.factory.po.entity.POPriority;
import com.factory.po.service.PurchaseOrderService;
import com.factory.product.dto.ProductCreateDto;
import com.factory.product.dto.ProductResponseDto;
import com.factory.product.entity.ProductCategory;
import com.factory.product.entity.ProductUnit;
import com.factory.product.service.ProductService;
import com.factory.reporting.dto.*;
import com.factory.reporting.service.DashboardService;
import com.factory.reporting.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class DashboardAndReportIntegrationTests {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private PurchaseOrderService poService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OfficeRepository officeRepository;

    private Office testGodownOffice;
    private ClientResponseDto testClient;
    private ProductResponseDto testProduct;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 6);

        testGodownOffice = new Office(
                UUID.randomUUID().toString(),
                "Test Godown " + suffix,
                OfficeType.WAREHOUSE_GODOWN,
                "Maharashtra",
                "Jalna",
                "MIDC Area",
                "27AAAAA0000A1Z5",
                "9876543210",
                "godown." + suffix + "@agrisep.com"
        );
        testGodownOffice = officeRepository.save(testGodownOffice);

        ClientCreateDto clientDto = new ClientCreateDto();
        clientDto.setCompanyName("Agro Seeds Corp " + suffix);
        clientDto.setClientType(ClientType.SEED_PRODUCER);
        clientDto.setGstin("27AAAAA0000A1Z5");
        clientDto.setState("Maharashtra");
        clientDto.setPrimaryContactName("Ramesh Rao");
        clientDto.setPrimaryContactPhone("9822000000");
        clientDto.setPrimaryContactEmail("ramesh." + suffix + "@agroseeds.com");
        testClient = clientService.createClient(clientDto, "tester");

        ProductCreateDto productDto = new ProductCreateDto();
        productDto.setName("Fine Grader " + suffix);
        productDto.setSku("FG-" + suffix);
        productDto.setCategory(ProductCategory.GRAVITY_SEPARATOR);
        productDto.setUnit(ProductUnit.SET);
        productDto.setBasePriceINR(BigDecimal.valueOf(150000.00));
        testProduct = productService.createProduct(productDto);

        // Inward 10 units into test godown
        StockInwardDto inwardDto = new StockInwardDto();
        inwardDto.setOfficeId(testGodownOffice.getId());
        inwardDto.setProductId(testProduct.getId());
        inwardDto.setQuantity(10);
        inwardDto.setReorderThreshold(2);
        inventoryService.recordInward(inwardDto, "tester");
    }

    @Test
    @DisplayName("Should generate Owner dashboard with calculated metrics")
    void testOwnerDashboardMetrics() {
        // Create a PO
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClient.getId());
        poDto.setOfficeId(testGodownOffice.getId());
        poDto.setPriority(POPriority.URGENT);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusDays(15));
        poDto.setSubmitForApproval(true);

        POItemCreateDto itemDto = new POItemCreateDto();
        itemDto.setProductId(testProduct.getId());
        itemDto.setQuantity(4);
        itemDto.setUnitPriceINR(BigDecimal.valueOf(150000.00));
        poDto.setItems(Collections.singletonList(itemDto));

        poService.createPurchaseOrder(poDto, "tester");

        OwnerDashboardDto dashboard = dashboardService.getOwnerDashboard();
        assertNotNull(dashboard);
        assertTrue(dashboard.getTotalRevenueINR().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(dashboard.getPendingApprovalCount() >= 1);
        assertTrue(dashboard.getTotalInventoryValuationINR().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(dashboard.getRecentPOs());
    }

    @Test
    @DisplayName("Should generate Admin dashboard with system metrics")
    void testAdminDashboardMetrics() {
        AdminDashboardDto adminDashboard = dashboardService.getAdminDashboard();
        assertNotNull(adminDashboard);
        assertTrue(adminDashboard.getTotalUsersCount() >= 5);
        assertTrue(adminDashboard.getActiveOfficesCount() >= 4);
        assertTrue(adminDashboard.getTotalProductsCount() >= 3);
    }

    @Test
    @DisplayName("Should generate Godown dashboard with accurate stock totals")
    void testGodownDashboardMetrics() {
        GodownDashboardDto godownDashboard = dashboardService.getGodownDashboard(testGodownOffice.getId());
        assertNotNull(godownDashboard);
        assertTrue(godownDashboard.getTotalOnHandQuantity() >= 10);
        assertEquals(godownDashboard.getTotalOnHandQuantity(),
                godownDashboard.getTotalReservedQuantity() + godownDashboard.getTotalAvailableQuantity());
    }

    @Test
    @DisplayName("Should generate Inventory Valuation Report and CSV Export")
    void testInventoryValuationReportAndCsvExport() {
        List<InventoryValuationReportItemDto> report = reportService.getInventoryValuationReport(testGodownOffice.getId());
        assertFalse(report.isEmpty());
        assertTrue(report.stream().anyMatch(r -> r.getSku().equals(testProduct.getSku())));

        byte[] csv = reportService.exportInventoryValuationCsv(testGodownOffice.getId());
        assertNotNull(csv);
        String csvContent = new String(csv, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("Office,SKU,Product Name"));
        assertTrue(csvContent.contains(testProduct.getSku()));
    }

    @Test
    @DisplayName("Should generate PO Fulfillment Report and CSV Export")
    void testPOFulfillmentReportAndCsvExport() {
        POCreateDto poDto = new POCreateDto();
        poDto.setClientId(testClient.getId());
        poDto.setOfficeId(testGodownOffice.getId());
        poDto.setPriority(POPriority.NORMAL);
        poDto.setExpectedDeliveryDate(LocalDate.now().plusDays(20));

        POItemCreateDto itemDto = new POItemCreateDto();
        itemDto.setProductId(testProduct.getId());
        itemDto.setQuantity(2);
        itemDto.setUnitPriceINR(BigDecimal.valueOf(150000.00));
        poDto.setItems(Collections.singletonList(itemDto));

        POResponseDto createdPO = poService.createPurchaseOrder(poDto, "tester");

        List<POFulfillmentReportItemDto> report = reportService.getPOFulfillmentReport(testGodownOffice.getId(), null, null);
        assertFalse(report.isEmpty());
        assertTrue(report.stream().anyMatch(r -> r.getPoNumber().equals(createdPO.getPoNumber())));

        byte[] csv = reportService.exportPOFulfillmentCsv(testGodownOffice.getId(), null, null);
        assertNotNull(csv);
        String csvContent = new String(csv, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("PO Number,Client,Office"));
        assertTrue(csvContent.contains(createdPO.getPoNumber()));
    }
}
