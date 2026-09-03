package com.qpaix.geda.device.csv;

import com.qpaix.geda.device.Device;
import com.qpaix.geda.device.DeviceRepository;
import com.qpaix.geda.device.dto.DeviceBulkImportResult;
import com.qpaix.geda.org.OrgUnit;
import com.qpaix.geda.org.OrgUnitRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a CSV of devices with columns: deviceCode,name,type,orgUnitCode,status
 * (orgUnitId is also accepted in place of orgUnitCode). Valid rows are inserted;
 * invalid rows are collected as per-row errors without aborting the whole import.
 */
@Service
@RequiredArgsConstructor
public class DeviceCsvImportService {

    private final DeviceRepository deviceRepository;
    private final OrgUnitRepository orgUnitRepository;

    public DeviceBulkImportResult importCsv(MultipartFile file) {
        int created = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreSurroundingSpaces(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                long rowNum = record.getRecordNumber() + 1; // +1 for header line
                try {
                    processRow(record);
                    created++;
                } catch (Exception e) {
                    failed++;
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            errors.add("Failed to read CSV file: " + e.getMessage());
        }

        return new DeviceBulkImportResult(created, failed, errors);
    }

    private void processRow(CSVRecord record) {
        String deviceCode = getField(record, "deviceCode");
        String name = getField(record, "name");
        String typeRaw = getField(record, "type");
        String statusRaw = getField(record, "status");

        if (deviceCode == null || deviceCode.isBlank()) {
            throw new IllegalArgumentException("deviceCode is required");
        }
        if (deviceRepository.existsByDeviceCode(deviceCode)) {
            throw new IllegalArgumentException("deviceCode '" + deviceCode + "' already exists");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        Device.Type type;
        try {
            type = Device.Type.valueOf(typeRaw == null ? "" : typeRaw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid type '" + typeRaw + "' (expected SOLAR_RMS/WIND_RMS/HYBRID_RMS)");
        }

        OrgUnit orgUnit = resolveOrgUnit(record);

        Device.Status status;
        if (statusRaw == null || statusRaw.isBlank()) {
            status = Device.Status.OFFLINE;
        } else {
            try {
                status = Device.Status.valueOf(statusRaw.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("invalid status '" + statusRaw + "' (expected ONLINE/WARNING/OFFLINE)");
            }
        }

        Device device = new Device();
        device.setDeviceCode(deviceCode.trim());
        device.setName(name.trim());
        device.setType(type);
        device.setOrgUnit(orgUnit);
        device.setStatus(status);
        device.setUptimePercent(BigDecimal.ZERO);
        device.setTlsCertStatus(Device.CertStatus.VALID);
        device.setTlsCertValidUntil(LocalDate.now().plusYears(1));
        device.setCreatedAt(Instant.now());
        deviceRepository.save(device);
    }

    private OrgUnit resolveOrgUnit(CSVRecord record) {
        String orgUnitCode = getField(record, "orgUnitCode");
        String orgUnitId = getField(record, "orgUnitId");

        if (orgUnitCode != null && !orgUnitCode.isBlank()) {
            return orgUnitRepository.findByCode(orgUnitCode.trim())
                    .orElseThrow(() -> new IllegalArgumentException("unknown orgUnitCode '" + orgUnitCode + "'"));
        }
        if (orgUnitId != null && !orgUnitId.isBlank()) {
            try {
                Long id = Long.valueOf(orgUnitId.trim());
                return orgUnitRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("unknown orgUnitId '" + orgUnitId + "'"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("orgUnitId '" + orgUnitId + "' is not a valid number");
            }
        }
        throw new IllegalArgumentException("either orgUnitCode or orgUnitId is required");
    }

    private String getField(CSVRecord record, String name) {
        if (!record.isMapped(name)) {
            return null;
        }
        String value = record.get(name);
        return value == null ? null : value.trim();
    }
}
