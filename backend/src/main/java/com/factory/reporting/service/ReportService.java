package com.factory.reporting.service;

import com.factory.delivery.entity.DCItem;
import com.factory.delivery.entity.DeliveryChallan;
import com.factory.delivery.repository.DeliveryChallanRepository;
import com.factory.inventory.entity.Inventory;
import com.factory.inventory.repository.InventoryRepository;
import com.factory.po.entity.POItem;
import com.factory.po.entity.PurchaseOrder;
import com.factory.po.repository.PurchaseOrderRepository;
import com.factory.reporting.dto.DispatchReportItemDto;
import com.factory.reporting.dto.InventoryValuationReportItemDto;
import com.factory.reporting.dto.POFulfillmentReportItemDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository poRepository;
    private final DeliveryChallanRepository dcRepository;

    public ReportService(InventoryRepository inventoryRepository,
                         PurchaseOrderRepository poRepository,
                         DeliveryChallanRepository dcRepository) {
        this.inventoryRepository = inventoryRepository;
        this.poRepository = poRepository;
        this.dcRepository = dcRepository;
    }

    public List<InventoryValuationReportItemDto> getInventoryValuationReport(String officeId) {
        List<Inventory> list = (officeId != null && !officeId.isEmpty())
                ? inventoryRepository.findByOfficeId(officeId)
                : inventoryRepository.findAll();

        return list.stream().map(inv -> {
            InventoryValuationReportItemDto dto = new InventoryValuationReportItemDto();
            dto.setOfficeId(inv.getOffice().getId());
            dto.setOfficeName(inv.getOffice().getName());
            dto.setProductId(inv.getProduct().getId());
            dto.setSku(inv.getProduct().getSku());
            dto.setProductName(inv.getProduct().getName());
            dto.setCategory(inv.getProduct().getCategory().name());
            dto.setOnHandQuantity(inv.getOnHandQuantity());
            dto.setReservedQuantity(inv.getReservedQuantity());
            dto.setAvailableQuantity(inv.getAvailableQuantity());
            dto.setBasePriceINR(inv.getProduct().getBasePriceINR());
            dto.setTotalValuationINR(inv.getProduct().getBasePriceINR().multiply(BigDecimal.valueOf(inv.getOnHandQuantity())));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<POFulfillmentReportItemDto> getPOFulfillmentReport(String officeId, LocalDate startDate, LocalDate endDate) {
        List<PurchaseOrder> list = poRepository.findAll().stream()
                .filter(po -> officeId == null || officeId.isEmpty() || (po.getOffice() != null && po.getOffice().getId().equals(officeId)))
                .filter(po -> startDate == null || !po.getPoDate().isBefore(startDate))
                .filter(po -> endDate == null || !po.getPoDate().isAfter(endDate))
                .collect(Collectors.toList());

        return list.stream().map(po -> {
            POFulfillmentReportItemDto dto = new POFulfillmentReportItemDto();
            dto.setPoId(po.getId());
            dto.setPoNumber(po.getPoNumber());
            dto.setClientName(po.getClient().getCompanyName());
            dto.setOfficeName(po.getOffice().getName());
            dto.setPoDate(po.getPoDate());
            dto.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
            dto.setStatus(po.getStatus().name());
            dto.setPriority(po.getPriority().name());

            int totalOrdered = po.getItems().stream().mapToInt(POItem::getQuantity).sum();
            int totalDelivered = po.getItems().stream().mapToInt(POItem::getDeliveredQuantity).sum();
            double pct = totalOrdered > 0 ? ((double) totalDelivered / totalOrdered) * 100.0 : 0.0;

            dto.setTotalOrderedQuantity(totalOrdered);
            dto.setTotalDeliveredQuantity(totalDelivered);
            dto.setFulfillmentPercent(BigDecimal.valueOf(pct).setScale(1, RoundingMode.HALF_UP).doubleValue());
            dto.setTotalAmountINR(po.getTotalAmountINR());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<DispatchReportItemDto> getDispatchReport(LocalDate startDate, LocalDate endDate) {
        List<DeliveryChallan> list = dcRepository.findAll().stream()
                .filter(dc -> startDate == null || !dc.getChallanDate().isBefore(startDate))
                .filter(dc -> endDate == null || !dc.getChallanDate().isAfter(endDate))
                .collect(Collectors.toList());

        return list.stream().map(dc -> {
            DispatchReportItemDto dto = new DispatchReportItemDto();
            dto.setDcId(dc.getId());
            dto.setDcNumber(dc.getDcNumber());
            dto.setPoNumber(dc.getPurchaseOrder().getPoNumber());
            dto.setClientName(dc.getClient().getCompanyName());
            dto.setOfficeName(dc.getOffice().getName());
            dto.setChallanDate(dc.getChallanDate());
            dto.setDispatchedAt(dc.getDispatchedAt());
            dto.setDeliveredAt(dc.getDeliveredAt());
            dto.setStatus(dc.getStatus().name());
            dto.setVehicleNumber(dc.getVehicleNumber());
            dto.setTransporterName(dc.getTransporterName());
            dto.setTotalQuantity(dc.getItems().stream().mapToInt(DCItem::getQuantity).sum());
            return dto;
        }).collect(Collectors.toList());
    }

    public byte[] exportInventoryValuationCsv(String officeId) {
        List<InventoryValuationReportItemDto> data = getInventoryValuationReport(officeId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.println("Office,SKU,Product Name,Category,On Hand,Reserved,Available,Base Price (INR),Total Valuation (INR)");
            for (InventoryValuationReportItemDto row : data) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,%.2f,%.2f%n",
                        escape(row.getOfficeName()),
                        escape(row.getSku()),
                        escape(row.getProductName()),
                        escape(row.getCategory()),
                        row.getOnHandQuantity(),
                        row.getReservedQuantity(),
                        row.getAvailableQuantity(),
                        row.getBasePriceINR(),
                        row.getTotalValuationINR());
            }
        }
        return out.toByteArray();
    }

    public byte[] exportPOFulfillmentCsv(String officeId, LocalDate startDate, LocalDate endDate) {
        List<POFulfillmentReportItemDto> data = getPOFulfillmentReport(officeId, startDate, endDate);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.println("PO Number,Client,Office,PO Date,Expected Delivery,Status,Priority,Ordered Qty,Delivered Qty,Fulfillment %,Total Amount (INR)");
            for (POFulfillmentReportItemDto row : data) {
                writer.printf("\"%s\",\"%s\",\"%s\",%s,%s,\"%s\",\"%s\",%d,%d,%.1f%%,%.2f%n",
                        escape(row.getPoNumber()),
                        escape(row.getClientName()),
                        escape(row.getOfficeName()),
                        row.getPoDate(),
                        row.getExpectedDeliveryDate(),
                        row.getStatus(),
                        row.getPriority(),
                        row.getTotalOrderedQuantity(),
                        row.getTotalDeliveredQuantity(),
                        row.getFulfillmentPercent(),
                        row.getTotalAmountINR());
            }
        }
        return out.toByteArray();
    }

    private String escape(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
