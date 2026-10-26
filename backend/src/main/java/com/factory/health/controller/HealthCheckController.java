package com.factory.health.controller;

import com.factory.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {

    @Value("${spring.application.name:factory-core}")
    private String applicationName;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealthStatus() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("application", applicationName);
        healthInfo.put("system", "Factory Core - Factory Management & Inventory Management System");
        healthInfo.put("brand", "Indo American Equipment / Agrisep®");
        healthInfo.put("timestamp", Instant.now().toString());
        healthInfo.put("version", "1.0.0");

        return ResponseEntity.ok(ApiResponse.success("Factory Core system service is operational", healthInfo));
    }
}
