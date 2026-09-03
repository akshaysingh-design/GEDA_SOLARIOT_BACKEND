package com.qpaix.geda.dashboard;

import com.qpaix.geda.common.ApiResponse;
import com.qpaix.geda.dashboard.dto.DashboardKpiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpis")
    public ApiResponse<DashboardKpiDto> kpis() {
        return ApiResponse.ok(dashboardService.kpis());
    }
}
