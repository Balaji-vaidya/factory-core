package com.factory.product;

import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import com.factory.product.dto.*;
import com.factory.product.entity.*;
import com.factory.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceIntegrationTests {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("Create machinery product with specifications succeeds and normalizes SKU")
    void testCreateProduct_Success() {
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setSku("test-gs-10"); // lowercase input to test normalization
        createDto.setName("Agrisep® Precision Specific Gravity Separator Test Model");
        createDto.setCategory(ProductCategory.GRAVITY_SEPARATOR);
        createDto.setUnit(ProductUnit.SET);
        createDto.setDescription("Heavy-duty vibro-pneumatic seed deck for high-capacity seed conditioning.");
        createDto.setBasePriceINR(new BigDecimal("1250000.00"));
        createDto.setLeadTimeWeeks(6);
        createDto.setWebsiteVisibility(WebsiteVisibility.PUBLIC);

        ProductSpecDto specs = new ProductSpecDto();
        specs.setCapacity("10 - 12 TPH (Wheat / Paddy)");
        specs.setMotorPower("15 HP Main Blower + 1.5 HP Vibro Deck");
        specs.setMotorRating("TEFC IP55 Siemens/ABB");
        specs.setDimensions("2800 x 1850 x 2100 mm");
        specs.setWeight("2450 kg");
        specs.setAirVolume("12,500 m3/hr");
        specs.setScreenArea("4.2 m2");
        specs.setSeedCompatibility(List.of("Wheat", "Paddy", "Soybean", "Corn", "Millets"));
        createDto.setSpecs(specs);

        ProductResponseDto response = productService.createProduct(createDto);

        assertNotNull(response.getId());
        assertEquals("TEST-GS-10", response.getSku()); // Assert uppercase normalized
        assertEquals("Agrisep® Precision Specific Gravity Separator Test Model", response.getName());
        assertEquals(ProductCategory.GRAVITY_SEPARATOR, response.getCategory());
        assertEquals(ProductInternalStatus.ACTIVE, response.getInternalStatus());
        assertEquals(WebsiteVisibility.PUBLIC, response.getWebsiteVisibility());
        assertFalse(response.isLockedForSkuChange());
        assertNotNull(response.getSpecs());
        assertEquals("10 - 12 TPH (Wheat / Paddy)", response.getSpecs().getCapacity());
        assertTrue(response.getSpecs().getSeedCompatibility().contains("Soybean"));
    }

    @Test
    @DisplayName("Create product rejects duplicate SKU with ErrorCode.DUPLICATE_SKU")
    void testCreateProduct_DuplicateSku_ThrowsConflict() {
        ProductCreateDto p1 = new ProductCreateDto();
        p1.setSku("TEST-SC-04");
        p1.setName("Seed Cleaner Model 4");
        p1.setCategory(ProductCategory.SEED_CLEANER);
        p1.setBasePriceINR(new BigDecimal("680000.00"));
        productService.createProduct(p1);

        ProductCreateDto p2 = new ProductCreateDto();
        p2.setSku("  test-sc-04  "); // duplicate with whitespace and lowercase
        p2.setName("Duplicate Model");
        p2.setCategory(ProductCategory.SEED_CLEANER);
        p2.setBasePriceINR(new BigDecimal("700000.00"));

        BusinessException ex = assertThrows(BusinessException.class, () -> productService.createProduct(p2));
        assertEquals(ErrorCode.DUPLICATE_SKU, ex.getErrorCode());
    }

    @Test
    @DisplayName("Product SKU is strictly immutable when locked by purchase order reference")
    void testUpdateProduct_SkuImmutabilityWhenLocked() {
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setSku("TEST-DST-05");
        createDto.setName("Pre-Cleaner Destoner DST-5");
        createDto.setCategory(ProductCategory.DESTONER);
        createDto.setBasePriceINR(new BigDecimal("520000.00"));

        ProductResponseDto created = productService.createProduct(createDto);
        String productId = created.getId();

        // Lock SKU simulating reference in an approved PO
        productService.lockProductSku(productId);

        // Attempt to rename SKU to TEST-DST-06
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setSku("TEST-DST-06"); // Changed SKU
        updateDto.setName("Renamed Destoner");
        updateDto.setCategory(ProductCategory.DESTONER);
        updateDto.setBasePriceINR(new BigDecimal("550000.00"));

        BusinessException ex = assertThrows(BusinessException.class, () -> productService.updateProduct(productId, updateDto));
        assertEquals(ErrorCode.SKU_IMMUTABLE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Search and filter machinery products by query and category")
    void testSearchProducts_FiltersAndPagination() {
        ProductCreateDto cleaner = new ProductCreateDto();
        cleaner.setSku("TEST-SC-08");
        cleaner.setName("Multi-Screen Fine Seed Cleaner SC-8");
        cleaner.setCategory(ProductCategory.SEED_CLEANER);
        cleaner.setBasePriceINR(new BigDecimal("850000.00"));
        productService.createProduct(cleaner);

        ProductCreateDto treater = new ProductCreateDto();
        treater.setSku("TEST-CST-15");
        treater.setName("Continuous Seed Treater CST-15");
        treater.setCategory(ProductCategory.SEED_TREATER);
        treater.setBasePriceINR(new BigDecimal("420000.00"));
        productService.createProduct(treater);

        // Filter by category SEED_CLEANER
        PageResponse<ProductResponseDto> cleanerResults = productService.searchProducts(
                null, ProductCategory.SEED_CLEANER, null, null, PageRequest.of(0, 10));
        assertTrue(cleanerResults.getItems().stream().anyMatch(p -> p.getSku().equals("TEST-SC-08")));
        assertFalse(cleanerResults.getItems().stream().anyMatch(p -> p.getSku().equals("TEST-CST-15")));

        // Search by query
        PageResponse<ProductResponseDto> queryResults = productService.searchProducts(
                "Treater CST-15", null, null, null, PageRequest.of(0, 10));
        assertEquals(1, queryResults.getItems().size());
        assertEquals("TEST-CST-15", queryResults.getItems().get(0).getSku());
    }

    @Test
    @DisplayName("Toggle product internal status and website visibility")
    void testProductVisibilityAndInternalStatusToggles() {
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setSku("TEST-BE-01");
        createDto.setName("Heavy Duty Bucket Elevator");
        createDto.setCategory(ProductCategory.ELEVATOR);
        createDto.setBasePriceINR(new BigDecimal("210000.00"));

        ProductResponseDto created = productService.createProduct(createDto);
        assertEquals(ProductInternalStatus.ACTIVE, created.getInternalStatus());
        assertEquals(WebsiteVisibility.PUBLIC, created.getWebsiteVisibility());

        // Deactivate internal status
        ProductResponseDto deactivated = productService.updateInternalStatus(created.getId(), ProductInternalStatus.INACTIVE);
        assertEquals(ProductInternalStatus.INACTIVE, deactivated.getInternalStatus());

        // Hide from public website
        ProductResponseDto hidden = productService.updateWebsiteVisibility(created.getId(), WebsiteVisibility.HIDDEN);
        assertEquals(WebsiteVisibility.HIDDEN, hidden.getWebsiteVisibility());
    }

    @Test
    @DisplayName("Generate immutable product snapshot for purchase order items")
    void testGenerateProductSnapshot() {
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setSku("TEST-CS-200");
        createDto.setName("Agrisep Trichromatic Optical Color Sorter");
        createDto.setCategory(ProductCategory.COLOR_SORTER);
        createDto.setBasePriceINR(new BigDecimal("2800000.00"));

        ProductSpecDto specs = new ProductSpecDto();
        specs.setCapacity("4 - 6 TPH");
        specs.setMotorPower("3.5 kW Total Power");
        specs.setDimensions("1650 x 1400 x 1950 mm");
        specs.setWeight("1100 kg");
        createDto.setSpecs(specs);

        ProductResponseDto created = productService.createProduct(createDto);

        ProductSnapshotDto snapshot = productService.generateProductSnapshot(created.getId());
        assertNotNull(snapshot);
        assertEquals(created.getId(), snapshot.getProductId());
        assertEquals("TEST-CS-200", snapshot.getSku());
        assertEquals("Agrisep Trichromatic Optical Color Sorter", snapshot.getName());
        assertEquals(new BigDecimal("2800000.00"), snapshot.getBasePriceINR());
        assertEquals("4 - 6 TPH", snapshot.getCapacity());
        assertEquals("3.5 kW Total Power", snapshot.getMotorPower());
    }
}
