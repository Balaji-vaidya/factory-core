package com.factory.inventory.service;

import com.factory.common.response.PageResponse;
import com.factory.inventory.dto.StockShortageResponseDto;
import com.factory.inventory.entity.Inventory;
import com.factory.inventory.entity.ShortageStatus;
import com.factory.inventory.entity.StockShortage;
import com.factory.inventory.repository.InventoryRepository;
import com.factory.inventory.repository.StockShortageRepository;
import com.factory.po.entity.POItem;
import com.factory.po.entity.PurchaseOrder;
import com.factory.po.repository.POItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShortageAllocationService {

    private static final Logger log = LoggerFactory.getLogger(ShortageAllocationService.class);

    private final StockShortageRepository stockShortageRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final POItemRepository poItemRepository;

    public ShortageAllocationService(StockShortageRepository stockShortageRepository,
                                    InventoryRepository inventoryRepository,
                                    InventoryService inventoryService,
                                    POItemRepository poItemRepository) {
        this.stockShortageRepository = stockShortageRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
        this.poItemRepository = poItemRepository;
    }

    public void allocateStockOnPOApproval(PurchaseOrder po, String currentUserId) {
        String officeId = po.getOffice().getId();

        for (POItem item : po.getItems()) {
            String productId = item.getProduct().getId();
            int needed = item.getQuantity() - item.getReservedQuantity();

            if (needed <= 0) {
                continue;
            }

            Optional<Inventory> invOpt = inventoryRepository.findByOfficeIdAndProductId(officeId, productId);
            int availableStock = invOpt.map(Inventory::getAvailableQuantity).orElse(0);

            if (availableStock > 0) {
                int toReserve = Math.min(needed, availableStock);
                inventoryService.reserveStock(officeId, productId, toReserve, po.getId(), po.getPoNumber(), currentUserId);
                item.setReservedQuantity(item.getReservedQuantity() + toReserve);
                poItemRepository.save(item);
                needed -= toReserve;
            }

            if (needed > 0) {
                // Register / update shortage
                Optional<StockShortage> existingShortage = stockShortageRepository.findByPoItemId(item.getId());
                StockShortage shortage;
                if (existingShortage.isPresent()) {
                    shortage = existingShortage.get();
                    shortage.setRequiredQuantity(item.getQuantity());
                    shortage.setAllocatedQuantity(item.getReservedQuantity());
                } else {
                    shortage = new StockShortage(
                            po,
                            item,
                            po.getOffice(),
                            item.getProduct(),
                            item.getQuantity(),
                            item.getReservedQuantity()
                    );
                }
                stockShortageRepository.save(shortage);
                log.info("Registered stock shortage for PO {} item {}: needed {}, reserved {}, shortage {}",
                        po.getPoNumber(), item.getProduct().getSku(), item.getQuantity(), item.getReservedQuantity(), shortage.getShortageQuantity());
            } else {
                // Fully reserved, resolve any existing shortage record
                stockShortageRepository.findByPoItemId(item.getId()).ifPresent(s -> {
                    s.setAllocatedQuantity(item.getQuantity());
                    s.setStatus(ShortageStatus.RESOLVED);
                    stockShortageRepository.save(s);
                });
            }
        }
    }

    public int allocateIncomingStock(String officeId, String productId, int incomingQuantity, String currentUserId) {
        if (incomingQuantity <= 0) {
            return 0;
        }

        // Strict deterministic FIFO query: poDate ASC, expectedDeliveryDate ASC, poNumber ASC
        List<StockShortage> openShortages = stockShortageRepository.findOpenShortagesForAllocation(officeId, productId);
        int remaining = incomingQuantity;

        for (StockShortage shortage : openShortages) {
            if (remaining <= 0) {
                break;
            }

            int unmet = shortage.getShortageQuantity();
            int toAllocate = Math.min(remaining, unmet);

            // Reserve in inventory
            inventoryService.reserveStock(
                    officeId,
                    productId,
                    toAllocate,
                    shortage.getPurchaseOrder().getId(),
                    shortage.getPurchaseOrder().getPoNumber(),
                    currentUserId
            );

            // Update PO line item
            POItem poItem = shortage.getPoItem();
            poItem.setReservedQuantity(poItem.getReservedQuantity() + toAllocate);
            poItemRepository.save(poItem);

            // Update shortage record
            shortage.setAllocatedQuantity(shortage.getAllocatedQuantity() + toAllocate);
            stockShortageRepository.save(shortage);

            remaining -= toAllocate;

            log.info("Deterministic FIFO allocated {} units of {} to PO {} (Remaining shortage: {})",
                    toAllocate, productId, shortage.getPurchaseOrder().getPoNumber(), shortage.getShortageQuantity());
        }

        return incomingQuantity - remaining;
    }

    public void releaseReservationsAndCancelShortages(PurchaseOrder po, String reason, String currentUserId) {
        String officeId = po.getOffice().getId();

        for (POItem item : po.getItems()) {
            int reserved = item.getReservedQuantity();
            if (reserved > 0) {
                inventoryService.releaseReservation(
                        officeId,
                        item.getProduct().getId(),
                        reserved,
                        po.getId(),
                        reason,
                        currentUserId
                );
                item.setReservedQuantity(0);
                poItemRepository.save(item);
            }
        }

        List<StockShortage> shortages = stockShortageRepository.findByPurchaseOrderId(po.getId());
        for (StockShortage s : shortages) {
            s.setStatus(ShortageStatus.CANCELLED);
            stockShortageRepository.save(s);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<StockShortageResponseDto> searchShortages(String officeId, String productId, ShortageStatus status, Pageable pageable) {
        Page<StockShortage> page = stockShortageRepository.searchShortages(officeId, productId, status, pageable);
        List<StockShortageResponseDto> content = page.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    public StockShortageResponseDto mapToResponseDto(StockShortage s) {
        StockShortageResponseDto dto = new StockShortageResponseDto();
        dto.setId(s.getId());
        dto.setPoId(s.getPurchaseOrder().getId());
        dto.setPoNumber(s.getPurchaseOrder().getPoNumber());
        dto.setPoDate(s.getPurchaseOrder().getPoDate());
        dto.setExpectedDeliveryDate(s.getPurchaseOrder().getExpectedDeliveryDate());
        dto.setClientCompanyName(s.getPurchaseOrder().getClient() != null ? s.getPurchaseOrder().getClient().getCompanyName() : "Unknown");
        dto.setPoItemId(s.getPoItem().getId());
        dto.setOfficeId(s.getOffice().getId());
        dto.setOfficeName(s.getOffice().getName());
        dto.setProductId(s.getProduct().getId());
        dto.setSku(s.getProduct().getSku());
        dto.setProductName(s.getProduct().getName());
        dto.setRequiredQuantity(s.getRequiredQuantity());
        dto.setAllocatedQuantity(s.getAllocatedQuantity());
        dto.setShortageQuantity(s.getShortageQuantity());
        dto.setStatus(s.getStatus());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());
        return dto;
    }
}
