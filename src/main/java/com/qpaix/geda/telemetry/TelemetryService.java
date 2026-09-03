package com.qpaix.geda.telemetry;

import com.qpaix.geda.telemetry.dto.GenerationPointDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final GenerationReadingRepository generationReadingRepository;

    public List<GenerationPointDto> generationTrend(int hours) {
        LocalDateTime from = LocalDateTime.now().minusHours(hours);
        return generationReadingRepository.findHourlyTotalsSince(from).stream()
                .map(row -> new GenerationPointDto(row.getHour(), row.getTotalKwh()))
                .toList();
    }
}
