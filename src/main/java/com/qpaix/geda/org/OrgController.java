package com.qpaix.geda.org;

import com.qpaix.geda.common.ApiResponse;
import com.qpaix.geda.org.dto.OrgTreeNodeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/org")
@RequiredArgsConstructor
public class OrgController {

    private final OrgUnitService orgUnitService;

    @GetMapping("/tree")
    public ApiResponse<List<OrgTreeNodeDto>> tree() {
        return ApiResponse.ok(orgUnitService.buildTree());
    }
}
