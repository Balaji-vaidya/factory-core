package com.factory.product.service;

import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import com.factory.product.dto.*;
import com.factory.product.entity.*;
import com.factory.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponseDto createProduct(ProductCreateDto dto) {
        String normalizedSku = normalizeSku(dto.getSku());
        if (productRepository.existsBySku(normalizedSku)) {
            throw new BusinessException(ErrorCode.DUPLICATE_SKU,
                    "A product with SKU '" + normalizedSku + "' is already registered in the machinery catalog.");
        }

        Product product = new Product();
        product.setSku(normalizedSku);
        product.setName(dto.getName().trim());
        product.setCategory(dto.getCategory());
        if (dto.getUnit() != null) {
            product.setUnit(dto.getUnit());
        }
        product.setDescription(dto.getDescription());
        product.setBasePriceINR(dto.getBasePriceINR());
        product.setLeadTimeWeeks(dto.getLeadTimeWeeks());
        if (dto.getWebsiteVisibility() != null) {
            product.setWebsiteVisibility(dto.getWebsiteVisibility());
        }
        product.setBrochureUrl(dto.getBrochureUrl());
        product.setInternalStatus(ProductInternalStatus.ACTIVE);
        product.setLockedForSkuChange(false);

        if (dto.getSpecs() != null) {
            ProductSpec spec = new ProductSpec();
            mapSpecDtoToEntity(dto.getSpecs(), spec);
            product.setSpecs(spec);
        }

        Product saved = productRepository.save(product);
        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with id: " + id));
        return mapToResponseDto(product);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductBySku(String sku) {
        String normalizedSku = normalizeSku(sku);
        Product product = productRepository.findBySku(normalizedSku)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with SKU: " + normalizedSku));
        return mapToResponseDto(product);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponseDto> searchProducts(
            String query,
            ProductCategory category,
            ProductInternalStatus status,
            WebsiteVisibility visibility,
            Pageable pageable) {
        Page<Product> page = productRepository.searchProducts(query, category, status, visibility, pageable);
        List<ProductResponseDto> content = page.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    public ProductResponseDto updateProduct(String id, ProductUpdateDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with id: " + id));

        String normalizedSku = normalizeSku(dto.getSku());

        // Enforce SKU immutability once locked by purchase orders
        if (!product.getSku().equals(normalizedSku)) {
            if (product.isLockedForSkuChange()) {
                throw new BusinessException(ErrorCode.SKU_IMMUTABLE,
                        "Product SKU '" + product.getSku() + "' is locked and cannot be changed because historical purchase orders or inventory reference this model.");
            }
            if (productRepository.existsBySkuAndIdNot(normalizedSku, id)) {
                throw new BusinessException(ErrorCode.DUPLICATE_SKU,
                        "Another product with SKU '" + normalizedSku + "' already exists.");
            }
            product.setSku(normalizedSku);
        }

        product.setName(dto.getName().trim());
        product.setCategory(dto.getCategory());
        if (dto.getUnit() != null) {
            product.setUnit(dto.getUnit());
        }
        product.setDescription(dto.getDescription());
        product.setBasePriceINR(dto.getBasePriceINR());
        product.setLeadTimeWeeks(dto.getLeadTimeWeeks());
        if (dto.getWebsiteVisibility() != null) {
            product.setWebsiteVisibility(dto.getWebsiteVisibility());
        }
        product.setBrochureUrl(dto.getBrochureUrl());

        if (dto.getSpecs() != null) {
            ProductSpec spec = product.getSpecs();
            if (spec == null) {
                spec = new ProductSpec();
                product.setSpecs(spec);
            }
            mapSpecDtoToEntity(dto.getSpecs(), spec);
        }

        Product updated = productRepository.save(product);
        return mapToResponseDto(updated);
    }

    public ProductResponseDto updateInternalStatus(String id, ProductInternalStatus newStatus) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with id: " + id));

        product.setInternalStatus(newStatus);
        Product updated = productRepository.save(product);
        return mapToResponseDto(updated);
    }

    public ProductResponseDto updateWebsiteVisibility(String id, WebsiteVisibility newVisibility) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with id: " + id));

        product.setWebsiteVisibility(newVisibility);
        Product updated = productRepository.save(product);
        return mapToResponseDto(updated);
    }

    public void lockProductSku(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with id: " + id));
        product.setLockedForSkuChange(true);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public ProductSnapshotDto generateProductSnapshot(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with id: " + id));

        String capacity = product.getSpecs() != null ? product.getSpecs().getCapacity() : "N/A";
        String motorPower = product.getSpecs() != null ? product.getSpecs().getMotorPower() : "N/A";

        return new ProductSnapshotDto(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getUnit(),
                product.getBasePriceINR(),
                capacity,
                motorPower
        );
    }

    private String normalizeSku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Product SKU cannot be blank.");
        }
        return sku.trim().toUpperCase();
    }

    private void mapSpecDtoToEntity(ProductSpecDto dto, ProductSpec entity) {
        entity.setCapacity(dto.getCapacity().trim());
        entity.setMotorPower(dto.getMotorPower().trim());
        entity.setMotorRating(dto.getMotorRating());
        entity.setDimensions(dto.getDimensions().trim());
        entity.setWeight(dto.getWeight().trim());
        entity.setAirVolume(dto.getAirVolume());
        entity.setScreenArea(dto.getScreenArea());
        if (dto.getSeedCompatibility() != null && !dto.getSeedCompatibility().isEmpty()) {
            entity.setSeedCompatibility(String.join(", ", dto.getSeedCompatibility()));
        }
    }

    private ProductSpecDto mapSpecEntityToDto(ProductSpec entity) {
        List<String> seeds = null;
        if (entity.getSeedCompatibility() != null && !entity.getSeedCompatibility().trim().isEmpty()) {
            seeds = Arrays.stream(entity.getSeedCompatibility().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        return new ProductSpecDto(
                entity.getCapacity(),
                entity.getMotorPower(),
                entity.getMotorRating(),
                entity.getDimensions(),
                entity.getWeight(),
                entity.getAirVolume(),
                entity.getScreenArea(),
                seeds
        );
    }

    public ProductResponseDto mapToResponseDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setUnit(product.getUnit());
        dto.setDescription(product.getDescription());
        dto.setBasePriceINR(product.getBasePriceINR());
        dto.setLeadTimeWeeks(product.getLeadTimeWeeks());
        dto.setInternalStatus(product.getInternalStatus());
        dto.setWebsiteVisibility(product.getWebsiteVisibility());
        dto.setBrochureUrl(product.getBrochureUrl());
        dto.setLockedForSkuChange(product.isLockedForSkuChange());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getSpecs() != null) {
            dto.setSpecs(mapSpecEntityToDto(product.getSpecs()));
        }

        return dto;
    }
}
