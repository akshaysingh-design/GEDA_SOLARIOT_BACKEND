package com.qpaix.geda.org.dto;

import java.util.ArrayList;
import java.util.List;

public class OrgTreeNodeDto {

    private Long id;
    private String name;
    private String type;
    private long deviceCount;
    private List<OrgTreeNodeDto> children = new ArrayList<>();

    public OrgTreeNodeDto() {
    }

    public OrgTreeNodeDto(Long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(long deviceCount) {
        this.deviceCount = deviceCount;
    }

    public void addDeviceCount(long delta) {
        this.deviceCount += delta;
    }

    public List<OrgTreeNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<OrgTreeNodeDto> children) {
        this.children = children;
    }
}
