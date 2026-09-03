package com.qpaix.geda.device;

import com.qpaix.geda.common.exception.ApiException;
import com.qpaix.geda.common.exception.ResourceNotFoundException;
import com.qpaix.geda.device.dto.DeviceCreateRequest;
import com.qpaix.geda.device.dto.DeviceDto;
import com.qpaix.geda.org.OrgUnit;
import com.qpaix.geda.org.OrgUnitRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final OrgUnitRepository orgUnitRepository;

    @Transactional(readOnly = true)
    public Page<DeviceDto> list(String search, String type, String status, Pageable pageable) {
        Specification<Device> spec = buildSpecification(search, type, status);
        return deviceRepository.findAll(spec, pageable).map(this::toDto);
    }

    private Specification<Device> buildSpecification(String search, String type, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("deviceCode")), pattern),
                        cb.like(cb.lower(root.get("name")), pattern)
                ));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), Device.Type.valueOf(type.toUpperCase())));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), Device.Status.valueOf(status.toUpperCase())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public DeviceDto getById(Long id) {
        Device device = findEntityById(id);
        return toDto(device);
    }

    private Device findEntityById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + id));
    }

    @Transactional
    public DeviceDto create(DeviceCreateRequest request) {
        if (deviceRepository.existsByDeviceCode(request.getDeviceCode())) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_DEVICE_CODE",
                    "A device with code " + request.getDeviceCode() + " already exists");
        }
        OrgUnit orgUnit = orgUnitRepository.findById(request.getOrgUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Org unit not found: " + request.getOrgUnitId()));

        Device device = new Device();
        device.setDeviceCode(request.getDeviceCode());
        device.setName(request.getName());
        device.setType(Device.Type.valueOf(request.getType().toUpperCase()));
        device.setOrgUnit(orgUnit);
        device.setStatus(request.getStatus() != null
                ? Device.Status.valueOf(request.getStatus().toUpperCase())
                : Device.Status.OFFLINE);
        device.setUptimePercent(BigDecimal.ZERO);
        device.setTlsCertStatus(Device.CertStatus.VALID);
        device.setTlsCertValidUntil(LocalDate.now().plusYears(1));
        device.setCreatedAt(Instant.now());

        Device saved = deviceRepository.save(device);
        return toDto(saved);
    }

    @Transactional
    public DeviceDto update(Long id, DeviceCreateRequest request) {
        Device device = findEntityById(id);

        if (request.getOrgUnitId() != null) {
            OrgUnit orgUnit = orgUnitRepository.findById(request.getOrgUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Org unit not found: " + request.getOrgUnitId()));
            device.setOrgUnit(orgUnit);
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            device.setName(request.getName());
        }
        if (request.getType() != null && !request.getType().isBlank()) {
            device.setType(Device.Type.valueOf(request.getType().toUpperCase()));
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            device.setStatus(Device.Status.valueOf(request.getStatus().toUpperCase()));
        }

        Device saved = deviceRepository.save(device);
        return toDto(saved);
    }

    @Transactional
    public DeviceDto regenerateCert(Long id) {
        Device device = findEntityById(id);
        device.setTlsCertValidUntil(LocalDate.now().plusYears(1));
        device.setTlsCertStatus(Device.CertStatus.VALID);
        Device saved = deviceRepository.save(device);
        return toDto(saved);
    }

    Device createFromCsvRow(String deviceCode, String name, Device.Type type, OrgUnit orgUnit, Device.Status status) {
        Device device = new Device();
        device.setDeviceCode(deviceCode);
        device.setName(name);
        device.setType(type);
        device.setOrgUnit(orgUnit);
        device.setStatus(status);
        device.setUptimePercent(BigDecimal.ZERO);
        device.setTlsCertStatus(Device.CertStatus.VALID);
        device.setTlsCertValidUntil(LocalDate.now().plusYears(1));
        device.setCreatedAt(Instant.now());
        return deviceRepository.save(device);
    }

    boolean existsByDeviceCode(String deviceCode) {
        return deviceRepository.existsByDeviceCode(deviceCode);
    }

    public DeviceDto toDto(Device device) {
        return new DeviceDto(
                device.getId(),
                device.getDeviceCode(),
                device.getName(),
                device.getType().name(),
                device.getOrgUnit() != null ? device.getOrgUnit().getId() : null,
                device.getOrgUnit() != null ? device.getOrgUnit().getName() : null,
                device.getStatus().name(),
                device.getUptimePercent(),
                device.getTlsCertValidUntil(),
                device.getTlsCertStatus() != null ? device.getTlsCertStatus().name() : null,
                device.getCreatedAt(),
                device.getLastSeenAt()
        );
    }
}
