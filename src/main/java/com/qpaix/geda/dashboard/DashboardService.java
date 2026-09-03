package com.qpaix.geda.dashboard;

import com.qpaix.geda.alert.AlertRepository;
import com.qpaix.geda.dashboard.dto.DashboardKpiDto;
import com.qpaix.geda.device.Device;
import com.qpaix.geda.device.DeviceRepository;
import com.qpaix.geda.telemetry.GenerationReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;
    private final GenerationReadingRepository generationReadingRepository;

    public DashboardKpiDto kpis() {
        long totalDevices = deviceRepository.count();
        long onlineDevices = deviceRepository.countByStatus(Device.Status.ONLINE);
        double systemsOnlinePercent = totalDevices == 0
                ? 0.0
                : BigDecimal.valueOf(onlineDevices)
                    .divide(BigDecimal.valueOf(totalDevices), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

        long activeAlertsCount = alertRepository.countByAcknowledgedFalse();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);
        BigDecimal totalToday = generationReadingRepository.sumKwhBetween(startOfToday, startOfTomorrow);

        long hoursElapsedToday = Math.max(1, java.time.Duration.between(startOfToday, LocalDateTime.now()).toHours());
        BigDecimal avgGenerationTodayKwh = totalToday.divide(
                BigDecimal.valueOf(hoursElapsedToday), 2, RoundingMode.HALF_UP);

        return new DashboardKpiDto(totalDevices, systemsOnlinePercent, activeAlertsCount, avgGenerationTodayKwh);
    }
}
