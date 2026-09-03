package com.qpaix.geda.org;

import com.qpaix.geda.device.Device;
import com.qpaix.geda.device.DeviceRepository;
import com.qpaix.geda.org.dto.OrgTreeNodeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrgUnitService {

    private final OrgUnitRepository orgUnitRepository;
    private final DeviceRepository deviceRepository;

    public List<OrgTreeNodeDto> buildTree() {
        List<OrgUnit> allUnits = orgUnitRepository.findAllByOrderByIdAsc();

        // Count devices per org unit (plant-level, typically).
        Map<Long, Long> deviceCountByOrgUnitId = deviceRepository.findAll().stream()
                .map(Device::getOrgUnit)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(OrgUnit::getId, Collectors.counting()));

        Map<Long, OrgTreeNodeDto> nodesById = new HashMap<>();
        for (OrgUnit unit : allUnits) {
            OrgTreeNodeDto node = new OrgTreeNodeDto(unit.getId(), unit.getName(), unit.getType().name());
            node.setDeviceCount(deviceCountByOrgUnitId.getOrDefault(unit.getId(), 0L));
            nodesById.put(unit.getId(), node);
        }

        List<OrgTreeNodeDto> roots = new java.util.ArrayList<>();
        for (OrgUnit unit : allUnits) {
            OrgTreeNodeDto node = nodesById.get(unit.getId());
            if (unit.getParent() == null) {
                roots.add(node);
            } else {
                OrgTreeNodeDto parentNode = nodesById.get(unit.getParent().getId());
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }

        // Roll device counts up from children to parents (leaf counts already set above).
        for (OrgTreeNodeDto root : roots) {
            rollUpCounts(root);
        }

        return roots;
    }

    private long rollUpCounts(OrgTreeNodeDto node) {
        long childTotal = 0;
        for (OrgTreeNodeDto child : node.getChildren()) {
            childTotal += rollUpCounts(child);
        }
        node.addDeviceCount(childTotal);
        return node.getDeviceCount();
    }
}
