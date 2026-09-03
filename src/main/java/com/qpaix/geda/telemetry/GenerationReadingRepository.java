package com.qpaix.geda.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GenerationReadingRepository extends JpaRepository<GenerationReading, Long> {

    @Query("select gr.readingHour as hour, sum(gr.kwh) as totalKwh " +
            "from GenerationReading gr " +
            "where gr.readingHour >= :from " +
            "group by gr.readingHour " +
            "order by gr.readingHour asc")
    List<HourlyTotal> findHourlyTotalsSince(@Param("from") LocalDateTime from);

    @Query("select coalesce(sum(gr.kwh), 0) from GenerationReading gr " +
            "where gr.readingHour >= :from and gr.readingHour < :to")
    java.math.BigDecimal sumKwhBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select gr.orgUnit.id as orgUnitId, sum(gr.kwh) as totalKwh " +
            "from GenerationReading gr " +
            "where gr.orgUnit.id is not null and gr.readingHour >= :from and gr.readingHour < :to " +
            "group by gr.orgUnit.id")
    List<OrgUnitTotal> sumKwhByOrgUnitBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select d.type as deviceType, sum(gr.kwh) as totalKwh " +
            "from GenerationReading gr join gr.device d " +
            "where gr.readingHour >= :from and gr.readingHour < :to " +
            "group by d.type")
    List<DeviceTypeTotal> sumKwhByDeviceTypeBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    interface HourlyTotal {
        LocalDateTime getHour();
        java.math.BigDecimal getTotalKwh();
    }

    interface OrgUnitTotal {
        Long getOrgUnitId();
        java.math.BigDecimal getTotalKwh();
    }

    interface DeviceTypeTotal {
        com.qpaix.geda.device.Device.Type getDeviceType();
        java.math.BigDecimal getTotalKwh();
    }
}
