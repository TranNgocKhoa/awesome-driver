package com.khoa.driver.enums;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public enum ArchitectureType {
    DEFAULT(emptyList()), X32(asList("i686", "x86")), X64(emptyList()),
    ARM64(asList("aarch64", "m1"));

    private List<String> architectureValueList;

    ArchitectureType(List<String> emptyList) {
        this.architectureValueList = emptyList;
    }

    public List<String> getArchitectureValueList() {
        return architectureValueList;
    }

    public boolean matchString(String value) {
        return this.architectureValueList.stream()
                .anyMatch(architecture -> architecture.toLowerCase().contains(value));
    }
}
