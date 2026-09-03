package com.qpaix.geda.dashboard;

import com.qpaix.geda.common.ApiResponse;
import com.qpaix.geda.dashboard.dto.GenerationByTypeDto;
import com.qpaix.geda.dashboard.dto.PlantEfficiencyDto;
import com.qpaix.geda.dashboard.dto.PlantGenerationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard/generation")
@RequiredArgsConstructor
public class GenerationAnalyticsController {

    private final GenerationAnalyticsService generationAnalyticsService;

    @GetMapping("/plant-comparison")
    public ApiResponse<List<PlantGenerationDto>> plantComparison() {
        return ApiResponse.ok(generationAnalyticsService.plantComparison());
    }

    @GetMapping("/by-type")
    public ApiResponse<List<GenerationByTypeDto>> byType() {
        return ApiResponse.ok(generationAnalyticsService.byType());
    }

    @GetMapping("/efficiency-summary")
    public ApiResponse<List<PlantEfficiencyDto>> efficiencySummary() {
        return ApiResponse.ok(generationAnalyticsService.efficiencySummary());
    }
}
