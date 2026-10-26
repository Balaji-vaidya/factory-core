package com.factory.delivery.service;

import com.factory.audit.service.AuditLogService;
import com.factory.client.dto.ClientSnapshotDto;
import com.factory.client.entity.Client;
import com.factory.client.service.ClientService;
import com.factory.common.exception.BusinessException;
import com.factory.common.exception.ErrorCode;
import com.factory.common.response.PageResponse;
import com.factory.common.sequence.SequenceService;
import com.factory.delivery.dto.*;
import com.factory.delivery.entity.DCItem;
import com.factory.delivery.entity.DCStatus;
import com.factory.delivery.entity.DeliveryChallan;
import com.factory.delivery.repository.DCItemRepository;
import com.factory.delivery.repository.DeliveryChallanRepository;
import com.factory.inventory.service.InventoryService;
import com.factory.notification.service.NotificationService;
import com.factory.office.entity.Office;
import com.factory.po.entity.POItem;
import com.factory.po.entity.POStatus;
import com.factory.po.entity.PurchaseOrder;
import com.factory.po.repository.POItemRepository;
import com.factory.po.repository.PurchaseOrderRepository;
import com.factory.product.entity.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeliveryChallanService {

    private final DeliveryChallanRepository dcRepository;
    private final DCItemRepository dcItemRepository;
    private final PurchaseOrderRepository poRepository;
    private final POItemRepository poItemRepository;
    private final InventoryService inventoryService;
    private final ClientService clientService;
    private final SequenceService sequenceService;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<NotificationService> notificationServiceProvider;
    private final ObjectProvider<AuditLogService> auditLogServiceProvider;

    public DeliveryChallanService(DeliveryChallanRepository dcRepository,
                                  DCItemRepository dcItemRepository,
                                  PurchaseOrderRepository poRepository,
                                  POItemRepository poItemRepository,
                                  InventoryService inventoryService,
                                  ClientService clientService,
                                  SequenceService sequenceService,
                                  ObjectMapper objectMapper,
                                  ObjectProvider<NotificationService> notificationServiceProvider,
                                  ObjectProvider<AuditLogService> auditLogServiceProvider) {
        this.dcRepository = dcRepository;
        this.dcItemRepository = dcItemRepository;
        this.poRepository = poRepository;
        this.poItemRepository = poItemRepository;
        this.inventoryService = inventoryService;
        this.clientService = clientService;
        this.sequenceService = sequenceService;
        this.objectMapper = objectMapper;
        this.notificationServiceProvider = notificationServiceProvider;
        this.auditLogServiceProvider = auditLogServiceProvider;
    }

    public DCResponseDto createDeliveryChallan(DCCreateDto dto, String currentUserId) {
        PurchaseOrder po = poRepository.findById(dto.getPurchaseOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PO_NOT_FOUND, "Purchase order not found: " + dto.getPurchaseOrderId()));

        if (po.getStatus() != POStatus.APPROVED && po.getStatus() != POStatus.PARTIALLY_DELIVERED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Delivery challans can only be created for APPROVED or PARTIALLY_DELIVERED purchase orders. Current status: " + po.getStatus());
        }

        Client client = po.getClient();
        Office office = po.getOffice();

        ClientSnapshotDto addressSnapshot = clientService.generateClientSnapshot(client.getId(), dto.getDeliveryAddressId());
        String addressSnapshotJson = serializeToJson(addressSnapshot);

        String dcNumber = sequenceService.getNextDcNumber();

        DeliveryChallan dc = new DeliveryChallan();
        dc.setDcNumber(dcNumber);
        dc.setPurchaseOrder(po);
        dc.setClient(client);
        dc.setOffice(office);
        dc.setDeliveryAddressId(dto.getDeliveryAddressId());
        dc.setDeliveryAddressSnapshot(addressSnapshotJson);
        dc.setStatus(DCStatus.DRAFT);
        dc.setVehicleNumber(dto.getVehicleNumber().trim());
        dc.setDriverName(dto.getDriverName().trim());
        dc.setDriverPhone(dto.getDriverPhone().trim());
        dc.setTransporterName(dto.getTransporterName().trim());
        dc.setLrNumber(dto.getLrNumber());
        dc.setEwayBillNumber(dto.getEwayBillNumber());
        dc.setEwayBillDate(dto.getEwayBillDate());
        dc.setChallanDate(dto.getChallanDate() != null ? dto.getChallanDate() : LocalDate.now());
        dc.setNotes(dto.getNotes());
        dc.setCreatedBy(currentUserId);

        for (DCItemCreateDto itemDto : dto.getItems()) {
            POItem poItem = poItemRepository.findById(itemDto.getPoItemId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "PO Item not found: " + itemDto.getPoItemId()));

            int remainingToDispatch = poItem.getQuantity() - poItem.getDispatchedQuantity();
            if (itemDto.getQuantity() > remainingToDispatch) {
                throw new BusinessException(ErrorCode.DISPATCH_LIMIT_EXCEEDED,
                        String.format("Requested quantity (%d) exceeds remaining line item quantity (%d) for product %s.",
                                itemDto.getQuantity(), remainingToDispatch, poItem.getProduct().getSku()));
            }

            if (itemDto.getQuantity() > poItem.getReservedQuantity()) {
                throw new BusinessException(ErrorCode.DISPATCH_LIMIT_EXCEEDED,
                        String.format("Cannot create challan for %d units; only %d units are reserved in godown stock for product %s.",
                                itemDto.getQuantity(), poItem.getReservedQuantity(), poItem.getProduct().getSku()));
            }

            DCItem dcItem = new DCItem();
            dcItem.setPoItem(poItem);
            dcItem.setProduct(poItem.getProduct());
            dcItem.setProductSnapshot(poItem.getProductSnapshot());
            dcItem.setQuantity(itemDto.getQuantity());
            dcItem.setNotes(itemDto.getNotes());

            dc.addItem(dcItem);
        }

        DeliveryChallan saved = dcRepository.save(dc);

        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "CREATE", "DELIVERY_CHALLAN", saved.getId(),
                "Created Delivery Challan " + saved.getDcNumber() + " for PO " + po.getPoNumber(), null));

        return mapToResponseDto(saved);
    }

    public DCResponseDto dispatchDeliveryChallan(String id, String currentUserId) {
        DeliveryChallan dc = dcRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DC_NOT_FOUND, "Delivery challan not found: " + id));

        if (dc.getStatus() != DCStatus.DRAFT) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only DRAFT delivery challans can be dispatched. Current status: " + dc.getStatus());
        }

        PurchaseOrder po = dc.getPurchaseOrder();

        for (DCItem item : dc.getItems()) {
            POItem poItem = item.getPoItem();
            Product product = item.getProduct();

            // Deduct physical on-hand and reserved quantity from godown inventory
            inventoryService.dispatchStock(
                    dc.getOffice().getId(),
                    product.getId(),
                    item.getQuantity(),
                    dc.getId(),
                    dc.getDcNumber(),
                    currentUserId
            );

            // Update line item counters
            poItem.setDispatchedQuantity(poItem.getDispatchedQuantity() + item.getQuantity());
            poItem.setReservedQuantity(poItem.getReservedQuantity() - item.getQuantity());
            poItemRepository.save(poItem);
        }

        dc.setStatus(DCStatus.DISPATCHED);
        dc.setDispatchedAt(Instant.now());
        dc.setDispatchedBy(currentUserId);
        DeliveryChallan savedDc = dcRepository.save(dc);

        // Update Purchase Order status
        po.setStatus(POStatus.PARTIALLY_DELIVERED);
        poRepository.save(po);

        notificationServiceProvider.ifAvailable(ns -> ns.createNotification(
                po.getCreatedBy(), po.getOffice().getId(), null,
                "Delivery Challan Dispatched",
                "Challan " + savedDc.getDcNumber() + " has been dispatched for PO " + po.getPoNumber(),
                "DC_DISPATCHED", savedDc.getId(), "DELIVERY_CHALLAN"));

        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "DISPATCH", "DELIVERY_CHALLAN", savedDc.getId(),
                "Dispatched DC " + savedDc.getDcNumber() + " with " + savedDc.getItems().size() + " items", null));

        return mapToResponseDto(savedDc);
    }

    public DCResponseDto confirmDelivery(String id, DCDeliveryConfirmDto dto, String currentUserId) {
        DeliveryChallan dc = dcRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DC_NOT_FOUND, "Delivery challan not found: " + id));

        if (dc.getStatus() != DCStatus.DISPATCHED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only DISPATCHED delivery challans can be confirmed delivered. Current status: " + dc.getStatus());
        }

        PurchaseOrder po = dc.getPurchaseOrder();

        for (DCItem item : dc.getItems()) {
            POItem poItem = item.getPoItem();
            poItem.setDeliveredQuantity(poItem.getDeliveredQuantity() + item.getQuantity());
            poItemRepository.save(poItem);
        }

        dc.setStatus(DCStatus.DELIVERED);
        dc.setDeliveredAt(Instant.now());
        dc.setDeliveryConfirmedBy(currentUserId);
        dc.setRecipientName(dto.getRecipientName().trim());
        dc.setRecipientPhone(dto.getRecipientPhone().trim());
        if (dto.getProofOfDeliveryUrl() != null) {
            dc.setProofOfDeliveryUrl(dto.getProofOfDeliveryUrl());
        }
        if (dto.getNotes() != null) {
            dc.setNotes((dc.getNotes() != null ? dc.getNotes() + "\n" : "") + "[Delivery Note]: " + dto.getNotes().trim());
        }
        DeliveryChallan savedDc = dcRepository.save(dc);

        // Check if all PO items are completely delivered
        boolean allPoDelivered = po.getItems().stream()
                .allMatch(item -> item.getDeliveredQuantity() >= item.getQuantity());

        if (allPoDelivered) {
            po.setStatus(POStatus.DELIVERED);
            poRepository.save(po);
        }

        notificationServiceProvider.ifAvailable(ns -> ns.createNotification(
                po.getCreatedBy(), po.getOffice().getId(), null,
                "Delivery Completed",
                "Challan " + savedDc.getDcNumber() + " marked DELIVERED for PO " + po.getPoNumber(),
                "DC_DELIVERED", savedDc.getId(), "DELIVERY_CHALLAN"));

        auditLogServiceProvider.ifAvailable(as -> as.logAction(
                currentUserId, null, "CONFIRM_DELIVERY", "DELIVERY_CHALLAN", savedDc.getId(),
                "Delivery confirmed for DC " + savedDc.getDcNumber() + " signed by " + dto.getRecipientName(), null));

        return mapToResponseDto(savedDc);
    }

    @Transactional(readOnly = true)
    public DCResponseDto getDeliveryChallanById(String id) {
        DeliveryChallan dc = dcRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DC_NOT_FOUND, "Delivery challan not found: " + id));
        return mapToResponseDto(dc);
    }

    @Transactional(readOnly = true)
    public DCResponseDto getDeliveryChallanByNumber(String dcNumber) {
        DeliveryChallan dc = dcRepository.findByDcNumber(dcNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.DC_NOT_FOUND, "Delivery challan not found: " + dcNumber));
        return mapToResponseDto(dc);
    }

    @Transactional(readOnly = true)
    public PageResponse<DCResponseDto> searchChallans(String query, String officeId, DCStatus status, Pageable pageable) {
        Page<DeliveryChallan> page = dcRepository.searchChallans(query, officeId, status, pageable);
        List<DCResponseDto> content = page.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<DCResponseDto> getChallansByPO(String poId) {
        return dcRepository.findByPurchaseOrderId(poId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public DCResponseDto mapToResponseDto(DeliveryChallan dc) {
        DCResponseDto dto = new DCResponseDto();
        dto.setId(dc.getId());
        dto.setDcNumber(dc.getDcNumber());
        dto.setPurchaseOrderId(dc.getPurchaseOrder().getId());
        dto.setPoNumber(dc.getPurchaseOrder().getPoNumber());
        dto.setClientId(dc.getClient().getId());
        dto.setClientCompanyName(dc.getClient().getCompanyName());
        dto.setOfficeId(dc.getOffice().getId());
        dto.setOfficeName(dc.getOffice().getName());
        dto.setDeliveryAddressId(dc.getDeliveryAddressId());
        dto.setDeliveryAddressSnapshot(dc.getDeliveryAddressSnapshot());
        dto.setStatus(dc.getStatus());
        dto.setVehicleNumber(dc.getVehicleNumber());
        dto.setDriverName(dc.getDriverName());
        dto.setDriverPhone(dc.getDriverPhone());
        dto.setTransporterName(dc.getTransporterName());
        dto.setLrNumber(dc.getLrNumber());
        dto.setEwayBillNumber(dc.getEwayBillNumber());
        dto.setEwayBillDate(dc.getEwayBillDate());
        dto.setChallanDate(dc.getChallanDate());
        dto.setDispatchedAt(dc.getDispatchedAt());
        dto.setDispatchedBy(dc.getDispatchedBy());
        dto.setDeliveredAt(dc.getDeliveredAt());
        dto.setDeliveryConfirmedBy(dc.getDeliveryConfirmedBy());
        dto.setRecipientName(dc.getRecipientName());
        dto.setRecipientPhone(dc.getRecipientPhone());
        dto.setProofOfDeliveryUrl(dc.getProofOfDeliveryUrl());
        dto.setNotes(dc.getNotes());
        dto.setCreatedBy(dc.getCreatedBy());
        dto.setCreatedAt(dc.getCreatedAt());
        dto.setUpdatedAt(dc.getUpdatedAt());

        if (dc.getItems() != null) {
            dto.setItems(dc.getItems().stream()
                    .map(this::mapItemEntityToDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private DCItemResponseDto mapItemEntityToDto(DCItem item) {
        DCItemResponseDto dto = new DCItemResponseDto();
        dto.setId(item.getId());
        dto.setPoItemId(item.getPoItem().getId());
        dto.setProductId(item.getProduct().getId());
        dto.setSku(item.getProduct().getSku());
        dto.setProductName(item.getProduct().getName());
        dto.setUnit(item.getProduct().getUnit().name());
        dto.setQuantity(item.getQuantity());
        dto.setNotes(item.getNotes());
        return dto;
    }

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
