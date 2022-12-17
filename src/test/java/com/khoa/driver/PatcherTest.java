package com.khoa.driver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatcherTest {

    @Test
    void fetchDriver() {
        Patcher patcher = new Patcher(null);

        patcher.setup();
    }
}