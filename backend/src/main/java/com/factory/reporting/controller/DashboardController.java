package com.factory.reporting.controller;

import com.factory.common.response.ApiResponse;
import com.factory.common.security.CustomUserDetails;
import com.factory.reporting.dto.*;
import com.factory.reporting.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/owner")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<OwnerDashboardDto>> getOwnerDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getOwnerDashboard()));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getAdminDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getAdminDashboard()));
    }

    @GetMapping("/main-office")
    @PreAuthorize("hasRole('MAIN_OFFICE')")
    public ResponseEntity<ApiResponse<MainOfficeDashboardDto>> getMainOfficeDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getMainOfficeDashboard()));
    }

    @GetMapping("/outer-state")
    @PreAuthorize("hasRole('OUTER_STATE_OFFICE')")
    public ResponseEntity<ApiResponse<OuterStateDashboardDto>> getOuterStateDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getOuterStateDashboard(userDetails.getOfficeId())));
    }

    @GetMapping("/godown")
    @PreAuthorize("hasRole('GODOWN_MANAGER')")
    public ResponseEntity<ApiResponse<GodownDashboardDto>> getGodownDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getGodownDashboard(userDetails.getOfficeId())));
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> getRoleAppropriateSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String role = userDetails.getRole();
        switch (role) {
            case "OWNER":
                return ResponseEntity.ok(ApiResponse.success(dashboardService.getOwnerDashboard()));
            case "ADMIN":
                return ResponseEntity.ok(ApiResponse.success(dashboardService.getAdminDashboard()));
            case "MAIN_OFFICE":
                return ResponseEntity.ok(ApiResponse.success(dashboardService.getMainOfficeDashboard()));
            case "OUTER_STATE_OFFICE":
                return ResponseEntity.ok(ApiResponse.success(dashboardService.getOuterStateDashboard(userDetails.getOfficeId())));
            case "GODOWN_MANAGER":
                return ResponseEntity.ok(ApiResponse.success(dashboardService.getGodownDashboard(userDetails.getOfficeId())));
            default:
                return ResponseEntity.ok(ApiResponse.success(dashboardService.getMainOfficeDashboard()));
        }
    }
}
