package com.qpaix.geda.alert;

import com.qpaix.geda.alert.dto.AlertDto;
import com.qpaix.geda.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ApiResponse<List<AlertDto>> list(
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(alertService.list(severity, limit));
    }
}
