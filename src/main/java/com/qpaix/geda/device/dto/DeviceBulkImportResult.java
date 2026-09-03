package com.qpaix.geda.device.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DeviceBulkImportResult {

    private int created;
    private int failed;
    private List<String> errors;
}
