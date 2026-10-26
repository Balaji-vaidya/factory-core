package com.factory.inventory.service;

import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import com.factory.inventory.dto.InventoryResponseDto;
import com.factory.inventory.dto.StockAdjustmentDto;
import com.factory.inventory.dto.StockInwardDto;
import com.factory.inventory.dto.StockLedgerResponseDto;
import com.factory.inventory.entity.Inventory;
import com.factory.inventory.entity.StockLedgerEntry;
import com.factory.inventory.entity.StockTransactionType;
import com.factory.inventory.repository.InventoryRepository;
import com.factory.inventory.repository.StockLedgerRepository;
import com.factory.office.entity.Office;
import com.factory.office.repository.OfficeRepository;
import com.factory.product.entity.Product;
import com.factory.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final OfficeRepository officeRepository;
    private final ProductRepository productRepository;
    private final org.springframework.beans.factory.ObjectProvider<ShortageAllocationService> shortageAllocationServiceProvider;
    private final org.springframework.beans.factory.ObjectProvider<com.factory.audit.service.AuditLogService> auditLogServiceProvider;

    public InventoryService(InventoryRepository inventoryRepository,
                            StockLedgerRepository stockLedgerRepository,
                            OfficeRepository officeRepository,
                            ProductRepository productRepository,
                            org.springframework.beans.factory.ObjectProvider<ShortageAllocationService> shortageAllocationServiceProvider,
                            org.springframework.beans.factory.ObjectProvider<com.factory.audit.service.AuditLogService> auditLogServiceProvider) {
        this.inventoryRepository = inventoryRepository;
        this.stockLedgerRepository = stockLedgerRepository;
        this.officeRepository = officeRepository;
        this.productRepository = productRepository;
        this.shortageAllocationServiceProvider = shortageAllocationServiceProvider;
        this.auditLogServiceProvider = auditLogServiceProvider;
    }

    public InventoryResponseDto recordInward(StockInwardDto dto, String currentUserId) {
        if (dto.getQuantity() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Inward quantity must be greater than zero.");
        }

        Office office = officeRepository.findById(dto.getOfficeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Office not found: " + dto.getOfficeId()));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + dto.getProductId()));

        Inventory inventory = inventoryRepository.findByOfficeIdAndProductIdForUpdate(dto.getOfficeId(), dto.getProductId())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory(office, product, 0, dto.getReorderThreshold() != null ? dto.getReorderThreshold() : 2);
                    return inventoryRepository.save(newInv);
                });

        int onHandBefore = inventory.getOnHandQuantity();
        int reservedBefore = inventory.getReservedQuantity();
        int availableBefore = inventory.getAvailableQuantity();

        int onHandAfter = onHandBefore + dto.getQuantity();
        int reservedAfter = reservedBefore;
        int availableAfter = onHandAfter - reservedAfter;

        validateInvariants(onHandAfter, reservedAfter, availableAfter);

        inventory.setOnHandQuantity(onHandAfter);
        if (dto.getReorderThreshold() != null) {
            inventory.setReorderThreshold(dto.getReorderThreshold());
        }
        Inventory saved = inventoryRepository.save(inventory);

        StockLedgerEntry ledgerEntry = new StockLedgerEntry();
        ledgerEntry.setInventory(saved);
        ledgerEntry.setOffice(office);
        ledgerEntry.setProduct(product);
        ledgerEntry.setTransactionType(StockTransactionType.INWARD);
        ledgerEntry.setQuantityDelta(dto.getQuantity());
        ledgerEntry.setOnHandBefore(onHandBefore);
        ledgerEntry.setOnHandAfter(onHandAfter);
        ledgerEntry.setReservedBefore(reservedBefore);
        ledgerEntry.setReservedAfter(reservedAfter);
        ledgerEntry.setAvailableBefore(availableBefore);
        ledgerEntry.setAvailableAfter(availableAfter);
        ledgerEntry.setReferenceType("STOCK_INWARD");
        ledgerEntry.setNotes(dto.getNotes() != null ? dto.getNotes() : "Stock inward / production receipt");
        ledgerEntry.setCreatedBy(currentUserId);
        stockLedgerRepository.save(ledgerEntry);

        // Deterministic FIFO: automatically allocate newly inwarded stock to open shortages
        shortageAllocationServiceProvider.ifAvailable(allocator ->
                allocator.allocateIncomingStock(dto.getOfficeId(), dto.getProductId(), dto.getQuantity(), currentUserId)
        );

        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "INWARD_STOCK", "INVENTORY", saved.getId(),
                "Inwarded " + dto.getQuantity() + " units for " + product.getSku() + " into " + office.getName(), null));

        Inventory finalInventory = inventoryRepository.findById(saved.getId()).orElse(saved);
        return mapToResponseDto(finalInventory);
    }

    public InventoryResponseDto adjustStock(StockAdjustmentDto dto, String currentUserId) {
        if (dto.getReason() == null || dto.getReason().trim().length() < 5) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "A mandatory reason (minimum 5 characters) is required for manual stock adjustment.");
        }

        Inventory inventory = inventoryRepository.findByOfficeIdAndProductIdForUpdate(dto.getOfficeId(), dto.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND,
                        "Inventory record not found for product " + dto.getProductId() + " in office " + dto.getOfficeId()));

        int onHandBefore = inventory.getOnHandQuantity();
        int reservedBefore = inventory.getReservedQuantity();
        int availableBefore = inventory.getAvailableQuantity();

        int onHandAfter = dto.getNewOnHandQuantity();
        int reservedAfter = reservedBefore;
        int availableAfter = onHandAfter - reservedAfter;

        if (onHandAfter < reservedAfter) {
            throw new BusinessException(ErrorCode.INVENTORY_INVARIANT_VIOLATION,
                    String.format("Cannot adjust on-hand quantity (%d) below already reserved/committed quantity (%d).", onHandAfter, reservedAfter));
        }

        validateInvariants(onHandAfter, reservedAfter, availableAfter);

        inventory.setOnHandQuantity(onHandAfter);
        inventory.setLastAdjustedAt(Instant.now());
        Inventory saved = inventoryRepository.save(inventory);

        StockLedgerEntry ledgerEntry = new StockLedgerEntry();
        ledgerEntry.setInventory(saved);
        ledgerEntry.setOffice(inventory.getOffice());
        ledgerEntry.setProduct(inventory.getProduct());
        ledgerEntry.setTransactionType(StockTransactionType.MANUAL_ADJUSTMENT);
        ledgerEntry.setQuantityDelta(onHandAfter - onHandBefore);
        ledgerEntry.setOnHandBefore(onHandBefore);
        ledgerEntry.setOnHandAfter(onHandAfter);
        ledgerEntry.setReservedBefore(reservedBefore);
        ledgerEntry.setReservedAfter(reservedAfter);
        ledgerEntry.setAvailableBefore(availableBefore);
        ledgerEntry.setAvailableAfter(availableAfter);
        ledgerEntry.setReferenceType("MANUAL_ADJUSTMENT");
        ledgerEntry.setNotes(dto.getReason().trim());
        ledgerEntry.setCreatedBy(currentUserId);
        stockLedgerRepository.save(ledgerEntry);

        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "STOCK_ADJUSTMENT", "INVENTORY", saved.getId(),
                "Adjusted " + inventory.getProduct().getSku() + " in " + inventory.getOffice().getName() + " from " + onHandBefore + " to " + onHandAfter + ". Reason: " + dto.getReason().trim(), null));

        return mapToResponseDto(saved);
    }

    public Inventory reserveStock(String officeId, String productId, int quantityToReserve, String poId, String poNumber, String currentUserId) {
        Inventory inventory = inventoryRepository.findByOfficeIdAndProductIdForUpdate(officeId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                        "No inventory record available for product " + productId + " at office " + officeId));

        if (inventory.getAvailableQuantity() < quantityToReserve) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                    String.format("Insufficient stock for product %s at office %s. Available: %d, Requested: %d",
                            inventory.getProduct().getSku(), inventory.getOffice().getName(), inventory.getAvailableQuantity(), quantityToReserve));
        }

        int onHandBefore = inventory.getOnHandQuantity();
        int reservedBefore = inventory.getReservedQuantity();
        int availableBefore = inventory.getAvailableQuantity();

        int onHandAfter = onHandBefore;
        int reservedAfter = reservedBefore + quantityToReserve;
        int availableAfter = onHandAfter - reservedAfter;

        validateInvariants(onHandAfter, reservedAfter, availableAfter);

        inventory.setReservedQuantity(reservedAfter);
        Inventory saved = inventoryRepository.save(inventory);

        StockLedgerEntry ledgerEntry = new StockLedgerEntry();
        ledgerEntry.setInventory(saved);
        ledgerEntry.setOffice(saved.getOffice());
        ledgerEntry.setProduct(saved.getProduct());
        ledgerEntry.setTransactionType(StockTransactionType.PO_RESERVATION);
        ledgerEntry.setQuantityDelta(quantityToReserve);
        ledgerEntry.setOnHandBefore(onHandBefore);
        ledgerEntry.setOnHandAfter(onHandAfter);
        ledgerEntry.setReservedBefore(reservedBefore);
        ledgerEntry.setReservedAfter(reservedAfter);
        ledgerEntry.setAvailableBefore(availableBefore);
        ledgerEntry.setAvailableAfter(availableAfter);
        ledgerEntry.setReferenceId(poId);
        ledgerEntry.setReferenceType("PURCHASE_ORDER");
        ledgerEntry.setNotes("Stock reserved for Purchase Order: " + poNumber);
        ledgerEntry.setCreatedBy(currentUserId);
        stockLedgerRepository.save(ledgerEntry);

        return saved;
    }

    public Inventory releaseReservation(String officeId, String productId, int quantityToRelease, String poId, String reason, String currentUserId) {
        Inventory inventory = inventoryRepository.findByOfficeIdAndProductIdForUpdate(officeId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND,
                        "Inventory record not found for product " + productId + " at office " + officeId));

        int onHandBefore = inventory.getOnHandQuantity();
        int reservedBefore = inventory.getReservedQuantity();
        int availableBefore = inventory.getAvailableQuantity();

        int actualRelease = Math.min(quantityToRelease, reservedBefore);
        int onHandAfter = onHandBefore;
        int reservedAfter = reservedBefore - actualRelease;
        int availableAfter = onHandAfter - reservedAfter;

        validateInvariants(onHandAfter, reservedAfter, availableAfter);

        inventory.setReservedQuantity(reservedAfter);
        Inventory saved = inventoryRepository.save(inventory);

        StockLedgerEntry ledgerEntry = new StockLedgerEntry();
        ledgerEntry.setInventory(saved);
        ledgerEntry.setOffice(saved.getOffice());
        ledgerEntry.setProduct(saved.getProduct());
        ledgerEntry.setTransactionType(StockTransactionType.PO_UNRESERVATION);
        ledgerEntry.setQuantityDelta(-actualRelease);
        ledgerEntry.setOnHandBefore(onHandBefore);
        ledgerEntry.setOnHandAfter(onHandAfter);
        ledgerEntry.setReservedBefore(reservedBefore);
        ledgerEntry.setReservedAfter(reservedAfter);
        ledgerEntry.setAvailableBefore(availableBefore);
        ledgerEntry.setAvailableAfter(availableAfter);
        ledgerEntry.setReferenceId(poId);
        ledgerEntry.setReferenceType("PURCHASE_ORDER");
        ledgerEntry.setNotes(reason != null ? reason : "Stock reservation released");
        ledgerEntry.setCreatedBy(currentUserId);
        stockLedgerRepository.save(ledgerEntry);

        return saved;
    }

    public Inventory dispatchStock(String officeId, String productId, int quantityToDispatch, String dcId, String dcNumber, String currentUserId) {
        Inventory inventory = inventoryRepository.findByOfficeIdAndProductIdForUpdate(officeId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND,
                        "Inventory record not found for product " + productId + " at office " + officeId));

        if (inventory.getOnHandQuantity() < quantityToDispatch) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                    String.format("Cannot dispatch %d units; on-hand quantity is only %d.", quantityToDispatch, inventory.getOnHandQuantity()));
        }

        if (inventory.getReservedQuantity() < quantityToDispatch) {
            throw new BusinessException(ErrorCode.DISPATCH_LIMIT_EXCEEDED,
                    String.format("Cannot dispatch %d units; reserved quantity is only %d.", quantityToDispatch, inventory.getReservedQuantity()));
        }

        int onHandBefore = inventory.getOnHandQuantity();
        int reservedBefore = inventory.getReservedQuantity();
        int availableBefore = inventory.getAvailableQuantity();

        int onHandAfter = onHandBefore - quantityToDispatch;
        int reservedAfter = reservedBefore - quantityToDispatch;
        int availableAfter = onHandAfter - reservedAfter;

        validateInvariants(onHandAfter, reservedAfter, availableAfter);

        inventory.setOnHandQuantity(onHandAfter);
        inventory.setReservedQuantity(reservedAfter);
        Inventory saved = inventoryRepository.save(inventory);

        StockLedgerEntry ledgerEntry = new StockLedgerEntry();
        ledgerEntry.setInventory(saved);
        ledgerEntry.setOffice(saved.getOffice());
        ledgerEntry.setProduct(saved.getProduct());
        ledgerEntry.setTransactionType(StockTransactionType.DC_DISPATCH);
        ledgerEntry.setQuantityDelta(-quantityToDispatch);
        ledgerEntry.setOnHandBefore(onHandBefore);
        ledgerEntry.setOnHandAfter(onHandAfter);
        ledgerEntry.setReservedBefore(reservedBefore);
        ledgerEntry.setReservedAfter(reservedAfter);
        ledgerEntry.setAvailableBefore(availableBefore);
        ledgerEntry.setAvailableAfter(availableAfter);
        ledgerEntry.setReferenceId(dcId);
        ledgerEntry.setReferenceType("DELIVERY_CHALLAN");
        ledgerEntry.setNotes("Dispatched under Delivery Challan: " + dcNumber);
        ledgerEntry.setCreatedBy(currentUserId);
        stockLedgerRepository.save(ledgerEntry);

        return saved;
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryResponseDto> searchInventory(String officeId, String productId, String query, Pageable pageable) {
        Page<Inventory> page = inventoryRepository.searchInventory(officeId, productId, query, pageable);
        List<InventoryResponseDto> content = page.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public PageResponse<StockLedgerResponseDto> searchLedger(String officeId, String productId, StockTransactionType type, Pageable pageable) {
        Page<StockLedgerEntry> page = stockLedgerRepository.searchLedger(officeId, productId, type, pageable);
        List<StockLedgerResponseDto> content = page.getContent().stream()
                .map(this::mapLedgerEntryToDto)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public InventoryResponseDto getInventoryById(String id) {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND, "Inventory record not found: " + id));
        return mapToResponseDto(inv);
    }

    private void validateInvariants(int onHand, int reserved, int available) {
        if (onHand < 0) {
            throw new BusinessException(ErrorCode.INVENTORY_INVARIANT_VIOLATION, "Invariant violation: On-hand quantity cannot be negative: " + onHand);
        }
        if (reserved < 0) {
            throw new BusinessException(ErrorCode.INVENTORY_INVARIANT_VIOLATION, "Invariant violation: Reserved quantity cannot be negative: " + reserved);
        }
        if (reserved > onHand) {
            throw new BusinessException(ErrorCode.INVENTORY_INVARIANT_VIOLATION,
                    String.format("Invariant violation: Reserved quantity (%d) cannot exceed on-hand quantity (%d).", reserved, onHand));
        }
        if (available != onHand - reserved) {
            throw new BusinessException(ErrorCode.INVENTORY_INVARIANT_VIOLATION,
                    String.format("Invariant violation: Available (%d) != OnHand (%d) - Reserved (%d).", available, onHand, reserved));
        }
    }

    public InventoryResponseDto mapToResponseDto(Inventory inv) {
        InventoryResponseDto dto = new InventoryResponseDto();
        dto.setId(inv.getId());
        dto.setOfficeId(inv.getOffice() != null ? inv.getOffice().getId() : null);
        dto.setOfficeName(inv.getOffice() != null ? inv.getOffice().getName() : "Unknown");
        dto.setOfficeState(inv.getOffice() != null ? inv.getOffice().getState() : "Unknown");
        dto.setProductId(inv.getProduct() != null ? inv.getProduct().getId() : null);
        dto.setSku(inv.getProduct() != null ? inv.getProduct().getSku() : "N/A");
        dto.setProductName(inv.getProduct() != null ? inv.getProduct().getName() : "Unknown");
        dto.setUnit(inv.getProduct() != null ? inv.getProduct().getUnit().name() : "UNIT");
        dto.setOnHandQuantity(inv.getOnHandQuantity());
        dto.setReservedQuantity(inv.getReservedQuantity());
        dto.setAvailableQuantity(inv.getAvailableQuantity());
        dto.setReorderThreshold(inv.getReorderThreshold());
        dto.setLowStock(inv.getAvailableQuantity() <= inv.getReorderThreshold());
        dto.setLastAdjustedAt(inv.getLastAdjustedAt());
        dto.setUpdatedAt(inv.getUpdatedAt());
        return dto;
    }

    public StockLedgerResponseDto mapLedgerEntryToDto(StockLedgerEntry entry) {
        StockLedgerResponseDto dto = new StockLedgerResponseDto();
        dto.setId(entry.getId());
        dto.setInventoryId(entry.getInventory() != null ? entry.getInventory().getId() : null);
        dto.setOfficeId(entry.getOffice() != null ? entry.getOffice().getId() : null);
        dto.setOfficeName(entry.getOffice() != null ? entry.getOffice().getName() : "Unknown");
        dto.setProductId(entry.getProduct() != null ? entry.getProduct().getId() : null);
        dto.setSku(entry.getProduct() != null ? entry.getProduct().getSku() : "N/A");
        dto.setProductName(entry.getProduct() != null ? entry.getProduct().getName() : "Unknown");
        dto.setTransactionType(entry.getTransactionType());
        dto.setQuantityDelta(entry.getQuantityDelta());
        dto.setOnHandBefore(entry.getOnHandBefore());
        dto.setOnHandAfter(entry.getOnHandAfter());
        dto.setReservedBefore(entry.getReservedBefore());
        dto.setReservedAfter(entry.getReservedAfter());
        dto.setAvailableBefore(entry.getAvailableBefore());
        dto.setAvailableAfter(entry.getAvailableAfter());
        dto.setReferenceId(entry.getReferenceId());
        dto.setReferenceType(entry.getReferenceType());
        dto.setNotes(entry.getNotes());
        dto.setCreatedBy(entry.getCreatedBy());
        dto.setCreatedAt(entry.getCreatedAt());
        return dto;
    }
}
