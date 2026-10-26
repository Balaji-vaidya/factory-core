package com.factory.reporting.service;

import com.factory.audit.dto.AuditLogResponseDto;
import com.factory.audit.service.AuditLogService;
import com.factory.client.repository.ClientRepository;
import com.factory.delivery.dto.DCResponseDto;
import com.factory.delivery.entity.DCStatus;
import com.factory.delivery.repository.DeliveryChallanRepository;
import com.factory.delivery.service.DeliveryChallanService;
import com.factory.inventory.entity.Inventory;
import com.factory.inventory.entity.ShortageStatus;
import com.factory.inventory.repository.InventoryRepository;
import com.factory.inventory.repository.StockShortageRepository;
import com.factory.office.repository.OfficeRepository;
import com.factory.po.dto.POResponseDto;
import com.factory.po.entity.POItem;
import com.factory.po.entity.POStatus;
import com.factory.po.entity.PurchaseOrder;
import com.factory.po.repository.PurchaseOrderRepository;
import com.factory.po.service.PurchaseOrderService;
import com.factory.product.repository.ProductRepository;
import com.factory.reporting.dto.*;
import com.factory.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderService poService;
    private final InventoryRepository inventoryRepository;
    private final StockShortageRepository shortageRepository;
    private final DeliveryChallanRepository dcRepository;
    private final DeliveryChallanService dcService;
    private final ClientRepository clientRepository;
    private final OfficeRepository officeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public DashboardService(PurchaseOrderRepository poRepository,
                            PurchaseOrderService poService,
                            InventoryRepository inventoryRepository,
                            StockShortageRepository shortageRepository,
                            DeliveryChallanRepository dcRepository,
                            DeliveryChallanService dcService,
                            ClientRepository clientRepository,
                            OfficeRepository officeRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            AuditLogService auditLogService) {
        this.poRepository = poRepository;
        this.poService = poService;
        this.inventoryRepository = inventoryRepository;
        this.shortageRepository = shortageRepository;
        this.dcRepository = dcRepository;
        this.dcService = dcService;
        this.clientRepository = clientRepository;
        this.officeRepository = officeRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public OwnerDashboardDto getOwnerDashboard() {
        OwnerDashboardDto dto = new OwnerDashboardDto();

        List<PurchaseOrder> allPOs = poRepository.findAll();
        BigDecimal totalRev = allPOs.stream()
                .filter(po -> po.getStatus() != POStatus.CANCELLED)
                .map(PurchaseOrder::getTotalAmountINR)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalRevenueINR(totalRev);

        long pending = allPOs.stream().filter(po -> po.getStatus() == POStatus.PENDING_OWNER_APPROVAL).count();
        dto.setPendingApprovalCount(pending);

        long shortages = shortageRepository.findAll().stream()
                .filter(s -> s.getStatus() == ShortageStatus.OPEN || s.getStatus() == ShortageStatus.PARTIALLY_RESOLVED)
                .count();
        dto.setOpenShortageCount(shortages);

        List<Inventory> allInventory = inventoryRepository.findAll();
        BigDecimal totalValuation = allInventory.stream()
                .map(inv -> inv.getProduct().getBasePriceINR().multiply(BigDecimal.valueOf(inv.getOnHandQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalInventoryValuationINR(totalValuation);

        long totalOrdered = allPOs.stream().flatMap(po -> po.getItems().stream()).mapToLong(POItem::getQuantity).sum();
        long totalDelivered = allPOs.stream().flatMap(po -> po.getItems().stream()).mapToLong(POItem::getDeliveredQuantity).sum();
        double fulfillmentRate = totalOrdered > 0 ? ((double) totalDelivered / totalOrdered) * 100.0 : 100.0;
        dto.setPoFulfillmentRatePercent(BigDecimal.valueOf(fulfillmentRate).setScale(1, RoundingMode.HALF_UP).doubleValue());

        dto.setTotalPOCount(allPOs.size());
        dto.setTotalClientCount(clientRepository.count());

        List<POResponseDto> recentPOs = poRepository.findAll(PageRequest.of(0, 5)).getContent().stream()
                .map(poService::mapToResponseDto)
                .collect(Collectors.toList());
        dto.setRecentPOs(recentPOs);

        List<AuditLogResponseDto> recentActivities = auditLogService.searchLogs(null, null, null, null, null, PageRequest.of(0, 5)).getItems();
        dto.setRecentActivities(recentActivities);

        return dto;
    }

    public AdminDashboardDto getAdminDashboard() {
        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setTotalUsersCount(userRepository.count());
        dto.setActiveOfficesCount(officeRepository.count());
        dto.setTotalProductsCount(productRepository.count());

        List<AuditLogResponseDto> recentLogs = auditLogService.searchLogs(null, null, null, null, null, PageRequest.of(0, 10)).getItems();
        dto.setRecentAuditLogs(recentLogs);
        dto.setTotalAuditLogsCount(auditLogService.searchLogs(null, null, null, null, null, PageRequest.of(0, 1)).getTotalItems());

        return dto;
    }

    public MainOfficeDashboardDto getMainOfficeDashboard() {
        MainOfficeDashboardDto dto = new MainOfficeDashboardDto();
        List<PurchaseOrder> allPOs = poRepository.findAll();

        dto.setTotalPOCount(allPOs.size());
        dto.setDraftPOCount(allPOs.stream().filter(po -> po.getStatus() == POStatus.DRAFT).count());
        dto.setPendingApprovalCount(allPOs.stream().filter(po -> po.getStatus() == POStatus.PENDING_OWNER_APPROVAL).count());
        dto.setApprovedPOCount(allPOs.stream().filter(po -> po.getStatus() == POStatus.APPROVED).count());
        dto.setPartiallyDeliveredPOCount(allPOs.stream().filter(po -> po.getStatus() == POStatus.PARTIALLY_DELIVERED).count());
        dto.setDeliveredPOCount(allPOs.stream().filter(po -> po.getStatus() == POStatus.DELIVERED).count());

        long shortages = shortageRepository.findAll().stream()
                .filter(s -> s.getStatus() == ShortageStatus.OPEN || s.getStatus() == ShortageStatus.PARTIALLY_RESOLVED)
                .count();
        dto.setOpenShortageCount(shortages);

        List<POResponseDto> recentPOs = poRepository.findAll(PageRequest.of(0, 5)).getContent().stream()
                .map(poService::mapToResponseDto)
                .collect(Collectors.toList());
        dto.setRecentPOs(recentPOs);

        return dto;
    }

    public OuterStateDashboardDto getOuterStateDashboard(String officeId) {
        OuterStateDashboardDto dto = new OuterStateDashboardDto();
        dto.setOfficeId(officeId);

        officeRepository.findById(officeId).ifPresent(o -> dto.setOfficeName(o.getName()));

        List<PurchaseOrder> officePOs = poRepository.findAll().stream()
                .filter(po -> po.getOffice() != null && po.getOffice().getId().equals(officeId))
                .collect(Collectors.toList());

        dto.setTotalPOCount(officePOs.size());
        dto.setDraftPOCount(officePOs.stream().filter(po -> po.getStatus() == POStatus.DRAFT).count());
        dto.setPendingApprovalCount(officePOs.stream().filter(po -> po.getStatus() == POStatus.PENDING_OWNER_APPROVAL).count());
        dto.setApprovedPOCount(officePOs.stream().filter(po -> po.getStatus() == POStatus.APPROVED).count());
        dto.setPartiallyDeliveredPOCount(officePOs.stream().filter(po -> po.getStatus() == POStatus.PARTIALLY_DELIVERED).count());
        dto.setDeliveredPOCount(officePOs.stream().filter(po -> po.getStatus() == POStatus.DELIVERED).count());

        List<POResponseDto> recentPOs = officePOs.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(poService::mapToResponseDto)
                .collect(Collectors.toList());
        dto.setRecentPOs(recentPOs);

        return dto;
    }

    public GodownDashboardDto getGodownDashboard(String officeId) {
        GodownDashboardDto dto = new GodownDashboardDto();

        List<Inventory> inventories = (officeId != null && !officeId.isEmpty())
                ? inventoryRepository.findByOfficeId(officeId)
                : inventoryRepository.findAll();

        int onHand = inventories.stream().mapToInt(Inventory::getOnHandQuantity).sum();
        int reserved = inventories.stream().mapToInt(Inventory::getReservedQuantity).sum();
        int available = inventories.stream().mapToInt(Inventory::getAvailableQuantity).sum();

        dto.setTotalOnHandQuantity(onHand);
        dto.setTotalReservedQuantity(reserved);
        dto.setTotalAvailableQuantity(available);

        long draftDCs = dcRepository.findAll().stream()
                .filter(dc -> dc.getStatus() == DCStatus.DRAFT)
                .count();
        dto.setPendingDispatchDCCount(draftDCs);

        long shortages = shortageRepository.findAll().stream()
                .filter(s -> s.getStatus() == ShortageStatus.OPEN || s.getStatus() == ShortageStatus.PARTIALLY_RESOLVED)
                .count();
        dto.setOpenShortageCount(shortages);

        List<DCResponseDto> recentDispatches = dcRepository.findAll(PageRequest.of(0, 5)).getContent().stream()
                .map(dcService::mapToResponseDto)
                .collect(Collectors.toList());
        dto.setRecentDispatches(recentDispatches);

        return dto;
    }
}
