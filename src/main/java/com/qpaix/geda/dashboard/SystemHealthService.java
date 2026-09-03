package com.qpaix.geda.dashboard;

import com.qpaix.geda.alert.AlertRepository;
import com.qpaix.geda.dashboard.dto.AlertTrendPointDto;
import com.qpaix.geda.dashboard.dto.CertStatusBreakdownDto;
import com.qpaix.geda.dashboard.dto.DeviceStatusBreakdownDto;
import com.qpaix.geda.device.Device;
import com.qpaix.geda.device.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemHealthService {

    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public DeviceStatusBreakdownDto deviceStatusBreakdown() {
        long online = deviceRepository.countByStatus(Device.Status.ONLINE);
        long warning = deviceRepository.countByStatus(Device.Status.WARNING);
        long offline = deviceRepository.countByStatus(Device.Status.OFFLINE);
        return new DeviceStatusBreakdownDto(online, warning, offline);
    }

    @Transactional(readOnly = true)
    public CertStatusBreakdownDto certStatusBreakdown() {
        long valid = deviceRepository.countByTlsCertStatus(Device.CertStatus.VALID);
        long expiring = deviceRepository.countByTlsCertStatus(Device.CertStatus.EXPIRING);
        long expired = deviceRepository.countByTlsCertStatus(Device.CertStatus.EXPIRED);
        return new CertStatusBreakdownDto(valid, expiring, expired);
    }

    /**
     * Alert counts for each of the last {@code days} calendar days (including
     * today), broken down by severity. Days with no alerts still appear with
     * zero counts so the chart's X axis is contiguous.
     */
    @Transactional(readOnly = true)
    public java.util.List<AlertTrendPointDto> alertTrend(int days) {
        int windowDays = Math.max(days, 1);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(windowDays - 1L);
        Instant from = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Map<LocalDate, long[]> countsByDay = new LinkedHashMap<>();
        for (int i = 0; i < windowDays; i++) {
            countsByDay.put(startDate.plusDays(i), new long[3]); // [high, med, low]
        }

        for (AlertRepository.DailySeverityCount row : alertRepository.countByDayAndSeveritySince(from)) {
            LocalDate day = row.getDay().toLocalDate();
            long[] counts = countsByDay.get(day);
            if (counts == null) {
                continue; // defensive: outside requested window
            }
            switch (row.getSeverity()) {
                case HIGH -> counts[0] += row.getCnt();
                case MED -> counts[1] += row.getCnt();
                case LOW -> counts[2] += row.getCnt();
            }
        }

        return countsByDay.entrySet().stream()
                .map(e -> new AlertTrendPointDto(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                .toList();
    }
}
