package com.qpaix.geda.org;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, Long> {

    List<OrgUnit> findAllByOrderByIdAsc();

    Optional<OrgUnit> findByCode(String code);
}
