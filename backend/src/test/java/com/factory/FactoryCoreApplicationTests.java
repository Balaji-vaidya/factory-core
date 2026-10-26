package com.factory;

import com.factory.common.response.ApiResponse;
import com.factory.health.controller.HealthCheckController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FactoryCoreApplicationTests {

    @Autowired
    private HealthCheckController healthCheckController;

    @Test
    @DisplayName("Context Loads Successfully")
    void contextLoads() {
        assertNotNull(healthCheckController, "HealthCheckController must be injected by Spring context");
    }

    @Test
    @DisplayName("Health Check Controller Returns UP with Brand Details")
    void healthCheckReturnsUp() {
        ResponseEntity<ApiResponse<Map<String, Object>>> response = healthCheckController.getHealthStatus();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("UP", response.getBody().getData().get("status"));
        assertEquals("Indo American Equipment / Agrisep®", response.getBody().getData().get("brand"));
    }
}
