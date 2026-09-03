package com.qpaix.geda.device;

import com.qpaix.geda.common.ApiResponse;
import com.qpaix.geda.device.csv.DeviceCsvImportService;
import com.qpaix.geda.device.dto.DeviceBulkImportResult;
import com.qpaix.geda.device.dto.DeviceCreateRequest;
import com.qpaix.geda.device.dto.DeviceDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceCsvImportService deviceCsvImportService;

    @GetMapping
    public ApiResponse<Page<DeviceDto>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(deviceService.list(search, type, status, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<DeviceDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(deviceService.getById(id));
    }

    @PostMapping
    public ApiResponse<DeviceDto> create(@Valid @RequestBody DeviceCreateRequest request) {
        return ApiResponse.ok(deviceService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DeviceDto> update(@PathVariable Long id, @RequestBody DeviceCreateRequest request) {
        return ApiResponse.ok(deviceService.update(id, request));
    }

    @PostMapping("/bulk-import")
    public ApiResponse<DeviceBulkImportResult> bulkImport(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(deviceCsvImportService.importCsv(file));
    }

    @PostMapping("/{id}/regenerate-cert")
    public ApiResponse<DeviceDto> regenerateCert(@PathVariable Long id) {
        return ApiResponse.ok(deviceService.regenerateCert(id));
    }
}
