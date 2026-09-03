package com.qpaix.geda.dashboard;

import com.qpaix.geda.common.ApiResponse;
import com.qpaix.geda.dashboard.dto.AlertTrendPointDto;
import com.qpaix.geda.dashboard.dto.CertStatusBreakdownDto;
import com.qpaix.geda.dashboard.dto.DeviceStatusBreakdownDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard/health")
@RequiredArgsConstructor
public class SystemHealthController {

    private final SystemHealthService systemHealthService;

    @GetMapping("/device-status-breakdown")
    public ApiResponse<DeviceStatusBreakdownDto> deviceStatusBreakdown() {
        return ApiResponse.ok(systemHealthService.deviceStatusBreakdown());
    }

    @GetMapping("/alert-trend")
    public ApiResponse<List<AlertTrendPointDto>> alertTrend(
            @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(systemHealthService.alertTrend(days));
    }

    @GetMapping("/cert-status-breakdown")
    public ApiResponse<CertStatusBreakdownDto> certStatusBreakdown() {
        return ApiResponse.ok(systemHealthService.certStatusBreakdown());
    }
}
