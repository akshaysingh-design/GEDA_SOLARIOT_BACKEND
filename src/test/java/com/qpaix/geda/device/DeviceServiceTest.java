package com.qpaix.geda.device;

import com.qpaix.geda.common.exception.ApiException;
import com.qpaix.geda.device.dto.DeviceCreateRequest;
import com.qpaix.geda.device.dto.DeviceDto;
import com.qpaix.geda.org.OrgUnit;
import com.qpaix.geda.org.OrgUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private OrgUnitRepository orgUnitRepository;

    @InjectMocks
    private DeviceService deviceService;

    private OrgUnit plant;
    private Device device;

    @BeforeEach
    void setUp() {
        plant = new OrgUnit();
        plant.setId(8L);
        plant.setName("Rajkot Solar Plant 1");
        plant.setType(OrgUnit.Type.PLANT);

        device = new Device();
        device.setId(1L);
        device.setDeviceCode("DEV-RJK01-001");
        device.setName("Rajkot Plant 1 RMS A");
        device.setType(Device.Type.SOLAR_RMS);
        device.setOrgUnit(plant);
        device.setStatus(Device.Status.ONLINE);
        device.setUptimePercent(new BigDecimal("99.80"));
        device.setTlsCertStatus(Device.CertStatus.VALID);
        device.setCreatedAt(Instant.now());
    }

    @Test
    void list_returnsPagedDtos() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Device> page = new PageImpl<>(List.of(device), pageable, 1);
        when(deviceRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<DeviceDto> result = deviceService.list(null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDeviceCode()).isEqualTo("DEV-RJK01-001");
    }

    @Test
    void create_withNewDeviceCode_savesDevice() {
        DeviceCreateRequest request = new DeviceCreateRequest();
        request.setDeviceCode("DEV-NEW-001");
        request.setName("New Device");
        request.setType("SOLAR_RMS");
        request.setOrgUnitId(8L);
        request.setStatus("ONLINE");

        when(deviceRepository.existsByDeviceCode("DEV-NEW-001")).thenReturn(false);
        when(orgUnitRepository.findById(8L)).thenReturn(Optional.of(plant));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        DeviceDto result = deviceService.create(request);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getDeviceCode()).isEqualTo("DEV-NEW-001");
        assertThat(result.getStatus()).isEqualTo("ONLINE");

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        org.mockito.Mockito.verify(deviceRepository).save(captor.capture());
        assertThat(captor.getValue().getOrgUnit().getId()).isEqualTo(8L);
    }

    @Test
    void create_withDuplicateDeviceCode_throws() {
        DeviceCreateRequest request = new DeviceCreateRequest();
        request.setDeviceCode("DEV-RJK01-001");
        request.setName("Duplicate");
        request.setType("SOLAR_RMS");
        request.setOrgUnitId(8L);

        when(deviceRepository.existsByDeviceCode("DEV-RJK01-001")).thenReturn(true);

        assertThatThrownBy(() -> deviceService.create(request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void getById_whenMissing_throwsResourceNotFound() {
        when(deviceRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getById(404L))
                .isInstanceOf(com.qpaix.geda.common.exception.ResourceNotFoundException.class);
    }
}
