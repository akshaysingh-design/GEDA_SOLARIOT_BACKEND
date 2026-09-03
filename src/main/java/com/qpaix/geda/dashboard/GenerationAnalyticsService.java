package com.qpaix.geda.dashboard;

import com.qpaix.geda.dashboard.dto.GenerationByTypeDto;
import com.qpaix.geda.dashboard.dto.PlantEfficiencyDto;
import com.qpaix.geda.dashboard.dto.PlantGenerationDto;
import com.qpaix.geda.device.Device;
import com.qpaix.geda.device.DeviceRepository;
import com.qpaix.geda.org.OrgUnit;
import com.qpaix.geda.org.OrgUnitRepository;
import com.qpaix.geda.telemetry.GenerationReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerationAnalyticsService {

    private final OrgUnitRepository orgUnitRepository;
    private final DeviceRepository deviceRepository;
    private final GenerationReadingRepository generationReadingRepository;

    /**
     * Per-plant generation comparison for today, descending by total kWh.
     * generation_reading rows carry org_unit_id directly (a plant's devices
     * report readings against that plant's org unit), so this groups by
     * org_unit_id with no ancestor-walking needed — then filters to
     * PLANT-type org units that have at least one device.
     */
    @Transactional(readOnly = true)
    public List<PlantGenerationDto> plantComparison() {
        Map<Long, BigDecimal> totalsByOrgUnitId = todayTotalsByOrgUnit();

        List<OrgUnit> plants = orgUnitRepository.findAllByOrderByIdAsc().stream()
                .filter(u -> u.getType() == OrgUnit.Type.PLANT)
                .toList();

        Map<Long, Long> deviceCountByOrgUnitId = deviceRepository.findAll().stream()
                .map(Device::getOrgUnit)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(OrgUnit::getId, Collectors.counting()));

        return plants.stream()
                .filter(plant -> deviceCountByOrgUnitId.getOrDefault(plant.getId(), 0L) > 0)
                .map(plant -> new PlantGenerationDto(
                        plant.getId(),
                        plant.getName(),
                        totalsByOrgUnitId.getOrDefault(plant.getId(), BigDecimal.ZERO),
                        deviceCountByOrgUnitId.getOrDefault(plant.getId(), 0L)))
                .sorted(Comparator.comparing(PlantGenerationDto::getTotalKwhToday).reversed())
                .toList();
    }

    /**
     * Generation totals for today grouped by device type. Each generation
     * reading is joined to its reporting device to get the device's type
     * directly (a plant's devices are all the same type in the seed data,
     * but joining via the reading's own device is correct even if a plant
     * ever hosts mixed-type devices).
     */
    @Transactional(readOnly = true)
    public List<GenerationByTypeDto> byType() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        return generationReadingRepository.sumKwhByDeviceTypeBetween(startOfToday, startOfTomorrow).stream()
                .map(row -> new GenerationByTypeDto(row.getDeviceType().name(), row.getTotalKwh()))
                .sorted(Comparator.comparing(GenerationByTypeDto::getTotalKwhToday).reversed())
                .toList();
    }

    /**
     * CUF% = (energy generated today / (rated capacity kW * 24h)) * 100.
     * Performance ratio is expressed here as CUF relative to a reasonable
     * expected capacity factor for the plant's dominant device type (solar
     * ~20%, wind ~30%, hybrid ~25%), capped at 100%, since no irradiance/
     * wind-speed data is seeded to compute a textbook PR.
     */
    @Transactional(readOnly = true)
    public List<PlantEfficiencyDto> efficiencySummary() {
        Map<Long, BigDecimal> totalsByOrgUnitId = todayTotalsByOrgUnit();

        Map<Long, Device.Type> dominantTypeByOrgUnitId = deviceRepository.findAll().stream()
                .filter(d -> d.getOrgUnit() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getOrgUnit().getId(),
                        Collectors.collectingAndThen(
                                Collectors.groupingBy(Device::getType, Collectors.counting()),
                                counts -> counts.entrySet().stream()
                                        .max(Map.Entry.comparingByValue())
                                        .map(Map.Entry::getKey)
                                        .orElse(Device.Type.SOLAR_RMS))));

        List<OrgUnit> plants = orgUnitRepository.findAllByOrderByIdAsc().stream()
                .filter(u -> u.getType() == OrgUnit.Type.PLANT && u.getCapacityKw() != null)
                .toList();

        return plants.stream()
                .map(plant -> {
                    BigDecimal totalToday = totalsByOrgUnitId.getOrDefault(plant.getId(), BigDecimal.ZERO);
                    BigDecimal maxPossibleKwh = plant.getCapacityKw().multiply(BigDecimal.valueOf(24));
                    BigDecimal cufPercent = maxPossibleKwh.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : totalToday.divide(maxPossibleKwh, 6, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);

                    Device.Type dominantType = dominantTypeByOrgUnitId.getOrDefault(plant.getId(), Device.Type.SOLAR_RMS);
                    BigDecimal expectedCufPercent = switch (dominantType) {
                        case WIND_RMS -> BigDecimal.valueOf(30);
                        case HYBRID_RMS -> BigDecimal.valueOf(25);
                        default -> BigDecimal.valueOf(20);
                    };
                    BigDecimal performanceRatio = expectedCufPercent.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : cufPercent.divide(expectedCufPercent, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .min(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);

                    return new PlantEfficiencyDto(plant.getId(), plant.getName(), cufPercent, performanceRatio);
                })
                .sorted(Comparator.comparing(PlantEfficiencyDto::getCufPercent).reversed())
                .toList();
    }

    private Map<Long, BigDecimal> todayTotalsByOrgUnit() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        Map<Long, BigDecimal> totals = new HashMap<>();
        for (var row : generationReadingRepository.sumKwhByOrgUnitBetween(startOfToday, startOfTomorrow)) {
            totals.put(row.getOrgUnitId(), row.getTotalKwh());
        }
        return totals;
    }
}
