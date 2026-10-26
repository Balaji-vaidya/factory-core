package com.factory.reporting.controller;

import com.factory.common.response.ApiResponse;
import com.factory.reporting.dto.DispatchReportItemDto;
import com.factory.reporting.dto.InventoryValuationReportItemDto;
import com.factory.reporting.dto.POFulfillmentReportItemDto;
import com.factory.reporting.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MAIN_OFFICE')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/inventory-valuation")
    public ResponseEntity<ApiResponse<List<InventoryValuationReportItemDto>>> getInventoryValuationReport(
            @RequestParam(required = false) String officeId) {
        List<InventoryValuationReportItemDto> report = reportService.getInventoryValuationReport(officeId);
        return ResponseEntity.ok(ApiResponse.success("Inventory valuation report generated successfully", report));
    }

    @GetMapping(value = "/inventory-valuation/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportInventoryValuationCsv(
            @RequestParam(required = false) String officeId) {
        byte[] csv = reportService.exportInventoryValuationCsv(officeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventory_valuation_" + LocalDate.now() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/po-fulfillment")
    public ResponseEntity<ApiResponse<List<POFulfillmentReportItemDto>>> getPOFulfillmentReport(
            @RequestParam(required = false) String officeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<POFulfillmentReportItemDto> report = reportService.getPOFulfillmentReport(officeId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("PO fulfillment report generated successfully", report));
    }

    @GetMapping(value = "/po-fulfillment/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportPOFulfillmentCsv(
            @RequestParam(required = false) String officeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] csv = reportService.exportPOFulfillmentCsv(officeId, startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"po_fulfillment_" + LocalDate.now() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/dispatches")
    public ResponseEntity<ApiResponse<List<DispatchReportItemDto>>> getDispatchReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DispatchReportItemDto> report = reportService.getDispatchReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Dispatch report generated successfully", report));
    }
}
