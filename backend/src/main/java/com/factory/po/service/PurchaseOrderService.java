package com.factory.po.service;

import com.factory.audit.service.AuditLogService;
import com.factory.client.dto.ClientSnapshotDto;
import com.factory.client.entity.Client;
import com.factory.client.repository.ClientRepository;
import com.factory.client.service.ClientService;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import com.factory.common.sequence.SequenceService;
import com.factory.inventory.service.ShortageAllocationService;
import com.factory.notification.service.NotificationService;
import com.factory.office.entity.Office;
import com.factory.office.repository.OfficeRepository;
import com.factory.po.dto.*;
import com.factory.po.entity.*;
import com.factory.po.repository.POItemRepository;
import com.factory.po.repository.POVersionRepository;
import com.factory.po.repository.PurchaseOrderRepository;
import com.factory.product.dto.ProductSnapshotDto;
import com.factory.product.entity.Product;
import com.factory.product.repository.ProductRepository;
import com.factory.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final POItemRepository poItemRepository;
    private final POVersionRepository poVersionRepository;
    private final ClientRepository clientRepository;
    private final OfficeRepository officeRepository;
    private final ProductRepository productRepository;
    private final ClientService clientService;
    private final ProductService productService;
    private final SequenceService sequenceService;
    private final ShortageAllocationService shortageAllocationService;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<NotificationService> notificationServiceProvider;
    private final ObjectProvider<AuditLogService> auditLogServiceProvider;

    public PurchaseOrderService(PurchaseOrderRepository poRepository,
                                POItemRepository poItemRepository,
                                POVersionRepository poVersionRepository,
                                ClientRepository clientRepository,
                                OfficeRepository officeRepository,
                                ProductRepository productRepository,
                                ClientService clientService,
                                ProductService productService,
                                SequenceService sequenceService,
                                ShortageAllocationService shortageAllocationService,
                                ObjectMapper objectMapper,
                                ObjectProvider<NotificationService> notificationServiceProvider,
                                ObjectProvider<AuditLogService> auditLogServiceProvider) {
        this.poRepository = poRepository;
        this.poItemRepository = poItemRepository;
        this.poVersionRepository = poVersionRepository;
        this.clientRepository = clientRepository;
        this.officeRepository = officeRepository;
        this.productRepository = productRepository;
        this.clientService = clientService;
        this.productService = productService;
        this.sequenceService = sequenceService;
        this.shortageAllocationService = shortageAllocationService;
        this.objectMapper = objectMapper;
        this.notificationServiceProvider = notificationServiceProvider;
        this.auditLogServiceProvider = auditLogServiceProvider;
    }

    public POResponseDto createPurchaseOrder(POCreateDto dto, String currentUserId) {
        validateLineItemsNoDuplicates(dto.getItems());

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND, "Client not found with id: " + dto.getClientId()));

        Office office = officeRepository.findById(dto.getOfficeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Office not found with id: " + dto.getOfficeId()));

        ClientSnapshotDto clientSnapshot = clientService.generateClientSnapshot(dto.getClientId(), dto.getDeliveryAddressId());
        String clientSnapshotJson = serializeToJson(clientSnapshot);

        String poNumber = sequenceService.getNextPoNumber();

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(poNumber);
        po.setClient(client);
        po.setClientSnapshot(clientSnapshotJson);
        po.setOffice(office);
        if (dto.getPriority() != null) {
            po.setPriority(dto.getPriority());
        }
        po.setStatus(dto.isSubmitForApproval() ? POStatus.PENDING_OWNER_APPROVAL : POStatus.DRAFT);
        po.setPoDate(dto.getPoDate() != null ? dto.getPoDate() : LocalDate.now());
        po.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        po.setNotes(dto.getNotes());
        po.setCreatedBy(currentUserId);
        po.setCurrentVersion(1);

        for (POItemCreateDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + itemDto.getProductId()));

            ProductSnapshotDto productSnapshot = productService.generateProductSnapshot(product.getId());

            POItem poItem = new POItem();
            poItem.setProduct(product);
            poItem.setProductSnapshot(serializeToJson(productSnapshot));
            poItem.setQuantity(itemDto.getQuantity());
            poItem.setUnitPriceINR(itemDto.getUnitPriceINR());
            poItem.setExpectedDeliveryDate(itemDto.getExpectedDeliveryDate() != null ? itemDto.getExpectedDeliveryDate() : dto.getExpectedDeliveryDate());
            poItem.setNotes(itemDto.getNotes());

            po.addItem(poItem);

            // Lock product SKU so SKU cannot be altered after being referenced in a PO
            productService.lockProductSku(product.getId());
        }

        po.recalculateTotalAmount();
        PurchaseOrder saved = poRepository.save(po);

        if (saved.getStatus() == POStatus.PENDING_OWNER_APPROVAL) {
            notificationServiceProvider.ifAvailable(ns -> ns.createNotification(
                    null, null, "OWNER",
                    "New PO Submitted",
                    "Purchase Order " + saved.getPoNumber() + " submitted for approval.",
                    "PO_SUBMITTED", saved.getId(), "PURCHASE_ORDER"));
        }
        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "CREATE", "PURCHASE_ORDER", saved.getId(),
                "Created Purchase Order " + saved.getPoNumber() + " with " + saved.getItems().size() + " items", null));

        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public POResponseDto getPurchaseOrderById(String id) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PO_NOT_FOUND, "Purchase order not found with id: " + id));
        return mapToResponseDto(po);
    }

    @Transactional(readOnly = true)
    public POResponseDto getPurchaseOrderByNumber(String poNumber) {
        PurchaseOrder po = poRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.PO_NOT_FOUND, "Purchase order not found with number: " + poNumber));
        return mapToResponseDto(po);
    }

    @Transactional(readOnly = true)
    public PageResponse<POResponseDto> searchPurchaseOrders(
            String query,
            String officeId,
            String state,
            POStatus status,
            POPriority priority,
            Pageable pageable) {
        Page<PurchaseOrder> page = poRepository.searchPurchaseOrders(query, officeId, state, status, priority, pageable);
        List<POResponseDto> content = page.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    public POResponseDto updatePurchaseOrder(String id, POUpdateDto dto, String currentUserId) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PO_NOT_FOUND, "Purchase order not found with id: " + id));

        if (po.getStatus() == POStatus.CANCELLED || po.getStatus() == POStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Cannot modify purchase order in " + po.getStatus() + " status.");
        }

        validateLineItemsNoDuplicates(dto.getItems());

        // Snapshot current version if already APPROVED or PENDING_OWNER_APPROVAL
        if (po.getStatus() == POStatus.APPROVED || po.getStatus() == POStatus.PENDING_OWNER_APPROVAL) {
            recordVersionSnapshot(po, currentUserId, dto.getChangeReason(), dto);
            po.setCurrentVersion(po.getCurrentVersion() + 1);
            if (po.getStatus() == POStatus.APPROVED) {
                shortageAllocationService.releaseReservationsAndCancelShortages(po, "PO revision edited and submitted for re-approval", currentUserId);
            }
            po.setStatus(POStatus.PENDING_OWNER_APPROVAL);
        } else if (dto.isSubmitForApproval()) {
            po.setStatus(POStatus.PENDING_OWNER_APPROVAL);
        }

        if (dto.getPriority() != null) {
            po.setPriority(dto.getPriority());
        }
        if (dto.getExpectedDeliveryDate() != null) {
            po.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        }
        if (dto.getNotes() != null) {
            po.setNotes(dto.getNotes());
        }

        // Update line items in-place to avoid UK violation and preserve item IDs
        Map<String, POItemCreateDto> incomingMap = dto.getItems().stream()
                .collect(Collectors.toMap(POItemCreateDto::getProductId, java.util.function.Function.identity()));

        // 1. Remove items not present in the new list
        po.getItems().removeIf(item -> !incomingMap.containsKey(item.getProduct().getId()));

        // 2. Update existing items and add new ones
        for (POItemCreateDto itemDto : dto.getItems()) {
            Optional<POItem> existingOpt = po.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(itemDto.getProductId()))
                    .findFirst();

            if (existingOpt.isPresent()) {
                POItem existing = existingOpt.get();
                existing.setQuantity(itemDto.getQuantity());
                existing.setUnitPriceINR(itemDto.getUnitPriceINR());
                if (itemDto.getExpectedDeliveryDate() != null) {
                    existing.setExpectedDeliveryDate(itemDto.getExpectedDeliveryDate());
                } else if (po.getExpectedDeliveryDate() != null) {
                    existing.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
                }
                existing.setNotes(itemDto.getNotes());
                existing.calculateLineTotal();
            } else {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + itemDto.getProductId()));

                ProductSnapshotDto productSnapshot = productService.generateProductSnapshot(product.getId());

                POItem poItem = new POItem();
                poItem.setProduct(product);
                poItem.setProductSnapshot(serializeToJson(productSnapshot));
                poItem.setQuantity(itemDto.getQuantity());
                poItem.setUnitPriceINR(itemDto.getUnitPriceINR());
                poItem.setExpectedDeliveryDate(itemDto.getExpectedDeliveryDate() != null ? itemDto.getExpectedDeliveryDate() : po.getExpectedDeliveryDate());
                poItem.setNotes(itemDto.getNotes());

                po.addItem(poItem);
                productService.lockProductSku(product.getId());
            }
        }
        po.recalculateTotalAmount();

        PurchaseOrder updated = poRepository.save(po);
        return mapToResponseDto(updated);
    }

    public POResponseDto submitForApproval(String id, String currentUserId) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PO_NOT_FOUND, "Purchase order not found with id: " + id));

        if (po.getStatus() != POStatus.DRAFT && po.getStatus() != POStatus.RETURNED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only DRAFT or RETURNED purchase orders can be submitted for approval. Current status: " + po.getStatus());
        }

        po.setStatus(POStatus.PENDING_OWNER_APPROVAL);
        po.setRejectionReason(null);
        PurchaseOrder updated = poRepository.save(po);

        notificationServiceProvider.ifAvailable(ns -> ns.createNotification(
                null, null, "OWNER",
                "PO Submitted for Approval",
                "Purchase Order " + updated.getPoNumber() + " requires your approval.",
                "PO_SUBMITTED", updated.getId(), "PURCHASE_ORDER"));
        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "SUBMIT_APPROVAL", "PURCHASE_ORDER", updated.getId(),
                "Submitted PO " + updated.getPoNumber() + " for Owner approval", null));

        return mapToResponseDto(updated);
    }

    public POResponseDto approvePurchaseOrder(String id, String ownerUserId, String notes) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PO_NOT_FOUND, "Purchase order not found with id: " + id));

        if (po.getStatus() != POStatus.PENDING_OWNER_APPROVAL) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only purchase orders in PENDING_OWNER_APPROVAL status can be approved. Current status: " + po.getStatus());
        }

        po.setStatus(POStatus.APPROVED);
        po.setApprovedBy(ownerUserId);
        po.setApprovedAt(Instant.now());
        if (notes != null && !notes.trim().isEmpty()) {
            po.setNotes((po.getNotes() != null ? po.getNotes() + "\n" : "") + "[Owner Approval Note]: " + notes.trim());
        }

        PurchaseOrder updated = poRepository.save(po);

        // Phase 7 & 8: Stock reservation & shortage registration
        shortageAllocationService.allocateStockOnPOApproval(updated, ownerUserId);

        notificationServiceProvider.ifAvailable(ns -> ns.createNotification(
                updated.getCreatedBy(), updated.getOffice().getId(), null,
                "PO Approved",
                "Purchase Order " + updated.getPoNumber() + " has been approved by Owner.",
                "PO_APPROVED", updated.getId(), "PURCHASE_ORDER"));
        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                ownerUserId, null, "APPROVE", "PURCHASE_ORDER", updated.getId(),
                "Approved Purchase Order " + updated.getPoNumber(), null));

        return mapToResponseDto(updated);
    }

    public POResponseDto returnPurchaseOrder(String id, String ownerUserId, String reason) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PO_NOT_FOUND, "Purchase order not found with id: " + id));

        if (po.getStatus() != POStatus.PENDING_OWNER_APPROVAL && po.getStatus() != POStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only purchase orders in PENDING_OWNER_APPROVAL or APPROVED status can be returned for revision. Current status: " + po.getStatus());
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "A mandatory return reason must be provided by the Owner.");
        }

        po.setStatus(POStatus.RETURNED);
        po.setRejectionReason(reason.trim());
        PurchaseOrder updated = poRepository.save(po);

        // Release reservations & cancel shortages
        shortageAllocationService.releaseReservationsAndCancelShortages(updated, "PO returned by owner: " + reason.trim(), ownerUserId);

        notificationServiceProvider.ifAvailable(ns -> ns.createNotification(
                updated.getCreatedBy(), updated.getOffice().getId(), null,
                "PO Returned for Revision",
                "Purchase Order " + updated.getPoNumber() + " was returned by Owner. Reason: " + reason.trim(),
                "PO_RETURNED", updated.getId(), "PURCHASE_ORDER"));
        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                ownerUserId, null, "RETURN", "PURCHASE_ORDER", updated.getId(),
                "Returned PO " + updated.getPoNumber() + ". Reason: " + reason.trim(), null));

        return mapToResponseDto(updated);
    }

    public POResponseDto requestCancellation(String id, String currentUserId, String reason) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PO_NOT_FOUND, "Purchase order not found with id: " + id));

        if (po.getStatus() == POStatus.DELIVERED || po.getStatus() == POStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Cannot request cancellation for a " + po.getStatus() + " purchase order.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "A cancellation justification reason is mandatory.");
        }

        po.setStatus(POStatus.CANCEL_REQUESTED);
        po.setCancellationReason(reason.trim());
        PurchaseOrder updated = poRepository.save(po);

        notificationServiceProvider.ifAvailable(ns -> ns.createNotification(
                null, null, "OWNER",
                "PO Cancellation Requested",
                "Cancellation requested for PO " + updated.getPoNumber() + ". Reason: " + reason.trim(),
                "PO_CANCEL_REQUESTED", updated.getId(), "PURCHASE_ORDER"));
        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "REQUEST_CANCEL", "PURCHASE_ORDER", updated.getId(),
                "Cancellation requested for PO " + updated.getPoNumber() + ". Reason: " + reason.trim(), null));

        return mapToResponseDto(updated);
    }

    public POResponseDto approveCancellation(String id, String ownerUserId) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PO_NOT_FOUND, "Purchase order not found with id: " + id));

        if (po.getStatus() != POStatus.CANCEL_REQUESTED && po.getStatus() != POStatus.PENDING_OWNER_APPROVAL) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Cannot approve cancellation for purchase order in status: " + po.getStatus());
        }

        po.setStatus(POStatus.CANCELLED);
        PurchaseOrder updated = poRepository.save(po);

        // Release reservations & cancel shortages
        shortageAllocationService.releaseReservationsAndCancelShortages(updated, "PO cancellation approved", ownerUserId);

        notificationServiceProvider.ifAvailable(ns -> ns.createNotification(
                updated.getCreatedBy(), updated.getOffice().getId(), null,
                "PO Cancelled",
                "Purchase Order " + updated.getPoNumber() + " cancellation was approved.",
                "PO_CANCELLED", updated.getId(), "PURCHASE_ORDER"));
        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                ownerUserId, null, "CANCEL", "PURCHASE_ORDER", updated.getId(),
                "Approved cancellation for PO " + updated.getPoNumber(), null));

        return mapToResponseDto(updated);
    }

    @Transactional(readOnly = true)
    public List<POVersionResponseDto> getPOVersionHistory(String poId) {
        return poVersionRepository.findByPurchaseOrderIdOrderByVersionNumberDesc(poId).stream()
                .map(this::mapVersionEntityToDto)
                .collect(Collectors.toList());
    }

    private void recordVersionSnapshot(PurchaseOrder po, String changedBy, String changeSummary, POUpdateDto dto) {
        POVersion version = new POVersion();
        version.setVersionNumber(po.getCurrentVersion());
        version.setChangedBy(changedBy);
        version.setChangedAt(Instant.now());
        version.setChangeSummary(changeSummary != null && !changeSummary.trim().isEmpty()
                ? changeSummary.trim()
                : "PO version " + po.getCurrentVersion() + " modification");

        // Serialize complete PO state as snapshot
        POResponseDto currentSnapshot = mapToResponseDto(po);
        version.setSnapshotData(serializeToJson(currentSnapshot));

        // Diff detection
        if (dto.getExpectedDeliveryDate() != null && !dto.getExpectedDeliveryDate().equals(po.getExpectedDeliveryDate())) {
            version.addChange(new POVersionChange("expectedDeliveryDate",
                    po.getExpectedDeliveryDate().toString(), dto.getExpectedDeliveryDate().toString()));
        }

        if (dto.getPriority() != null && dto.getPriority() != po.getPriority()) {
            version.addChange(new POVersionChange("priority",
                    po.getPriority().name(), dto.getPriority().name()));
        }

        version.addChange(new POVersionChange("itemsCount",
                String.valueOf(po.getItems().size()), String.valueOf(dto.getItems().size())));

        po.addVersion(version);
    }

    private void validateLineItemsNoDuplicates(List<POItemCreateDto> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Purchase order must contain at least one line item.");
        }

        Set<String> productIds = new HashSet<>();
        for (POItemCreateDto item : items) {
            if (!productIds.add(item.getProductId())) {
                throw new BusinessException(ErrorCode.DUPLICATE_PO_PRODUCT,
                        "Duplicate product detected in order items: " + item.getProductId() + ". Each product can only appear once per PO.");
            }
        }
    }

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public POResponseDto mapToResponseDto(PurchaseOrder po) {
        POResponseDto dto = new POResponseDto();
        dto.setId(po.getId());
        dto.setPoNumber(po.getPoNumber());
        dto.setClientId(po.getClient() != null ? po.getClient().getId() : null);
        dto.setClientCompanyName(po.getClient() != null ? po.getClient().getCompanyName() : "Unknown");
        dto.setClientState(po.getClient() != null ? po.getClient().getState() : "Unknown");
        dto.setClientSnapshot(po.getClientSnapshot());
        dto.setOfficeId(po.getOffice() != null ? po.getOffice().getId() : null);
        dto.setOfficeName(po.getOffice() != null ? po.getOffice().getName() : "Unknown");
        dto.setOfficeState(po.getOffice() != null ? po.getOffice().getState() : "Unknown");
        dto.setPriority(po.getPriority());
        dto.setStatus(po.getStatus());
        dto.setTotalAmountINR(po.getTotalAmountINR());
        dto.setPoDate(po.getPoDate());
        dto.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        dto.setNotes(po.getNotes());
        dto.setRejectionReason(po.getRejectionReason());
        dto.setCancellationReason(po.getCancellationReason());
        dto.setCurrentVersion(po.getCurrentVersion());
        dto.setCreatedBy(po.getCreatedBy());
        dto.setApprovedBy(po.getApprovedBy());
        dto.setApprovedAt(po.getApprovedAt());
        dto.setCreatedAt(po.getCreatedAt());
        dto.setUpdatedAt(po.getUpdatedAt());

        if (po.getItems() != null) {
            dto.setItems(po.getItems().stream().map(this::mapItemEntityToDto).collect(Collectors.toList()));
        }

        if (po.getVersions() != null) {
            dto.setVersions(po.getVersions().stream().map(this::mapVersionEntityToDto).collect(Collectors.toList()));
        }

        return dto;
    }

    private POItemResponseDto mapItemEntityToDto(POItem item) {
        POItemResponseDto dto = new POItemResponseDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        dto.setSku(item.getProduct() != null ? item.getProduct().getSku() : "N/A");
        dto.setProductName(item.getProduct() != null ? item.getProduct().getName() : "Unknown");
        dto.setUnit(item.getProduct() != null ? item.getProduct().getUnit().name() : "UNIT");
        dto.setQuantity(item.getQuantity());
        dto.setUnitPriceINR(item.getUnitPriceINR());
        dto.setLineTotalINR(item.getLineTotalINR());
        dto.setReservedQuantity(item.getReservedQuantity());
        dto.setDispatchedQuantity(item.getDispatchedQuantity());
        dto.setDeliveredQuantity(item.getDeliveredQuantity());
        dto.setExpectedDeliveryDate(item.getExpectedDeliveryDate());
        dto.setNotes(item.getNotes());
        return dto;
    }

    private POVersionResponseDto mapVersionEntityToDto(POVersion version) {
        POVersionResponseDto dto = new POVersionResponseDto();
        dto.setVersionNumber(version.getVersionNumber());
        dto.setChangedBy(version.getChangedBy());
        dto.setChangedAt(version.getChangedAt());
        dto.setChangeSummary(version.getChangeSummary());
        dto.setSnapshotData(version.getSnapshotData());

        if (version.getChanges() != null) {
            dto.setChanges(version.getChanges().stream()
                    .map(c -> new POVersionResponseDto.POVersionChangeDto(c.getFieldName(), c.getPreviousValue(), c.getNewValue()))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
