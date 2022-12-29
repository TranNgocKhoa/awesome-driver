package io.github.tranngockhoa.driver;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserAgentTest {

    @Test
    void getUserAgentList() {
        List<String> userAgentList = new UserAgent().getUserAgentList();
    }
}