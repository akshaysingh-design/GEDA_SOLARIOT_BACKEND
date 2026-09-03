package com.qpaix.geda.telemetry;

import com.qpaix.geda.common.ApiResponse;
import com.qpaix.geda.telemetry.dto.GenerationPointDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @GetMapping("/generation-trend")
    public ApiResponse<List<GenerationPointDto>> generationTrend(
            @RequestParam(defaultValue = "24") int hours) {
        return ApiResponse.ok(telemetryService.generationTrend(hours));
    }
}
