package com.qpaix.geda.alert;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Alert> findAllBySeverityOrderByCreatedAtDesc(Alert.Severity severity, Pageable pageable);

    long countByAcknowledgedFalse();

    @Query("select cast(a.createdAt as date) as day, a.severity as severity, count(a) as cnt " +
            "from Alert a " +
            "where a.createdAt >= :from " +
            "group by cast(a.createdAt as date), a.severity " +
            "order by cast(a.createdAt as date) asc")
    List<DailySeverityCount> countByDayAndSeveritySince(@Param("from") Instant from);

    interface DailySeverityCount {
        java.sql.Date getDay();
        Alert.Severity getSeverity();
        long getCnt();
    }
}
