package com.qpaix.geda.alert;

import com.qpaix.geda.alert.dto.AlertDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public List<AlertDto> list(String severity, int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));
        List<Alert> alerts;
        if (severity != null && !severity.isBlank()) {
            alerts = alertRepository.findAllBySeverityOrderByCreatedAtDesc(
                    Alert.Severity.valueOf(severity.toUpperCase()), pageable);
        } else {
            alerts = alertRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return alerts.stream().map(this::toDto).toList();
    }

    private AlertDto toDto(Alert alert) {
        return new AlertDto(
                alert.getId(),
                alert.getSeverity().name(),
                alert.getMessage(),
                alert.getDevice() != null ? alert.getDevice().getDeviceCode() : null,
                alert.getOrgUnit() != null ? alert.getOrgUnit().getName() : null,
                alert.getCreatedAt(),
                alert.isAcknowledged()
        );
    }
}
