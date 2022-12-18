package com.khoa.driver;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class TestNowSecure {
    @Test
    void test() {
        AwesomeDriver awesomeDriver = new AwesomeDriver(false);
        try {
            awesomeDriver.get("https://nowsecure.nl");
        } finally {
            awesomeDriver.quit();
        }
    }

    @Test
    void testHeadless() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(true);
        try {
            awesomeDriver.get("https://nowsecure.nl");

            WebDriverWait webDriverWait = new WebDriverWait(awesomeDriver, Duration.ofSeconds(20));

            webDriverWait.until(ExpectedConditions.invisibilityOfElementWithText(By.tagName("h1"), "OH YEAH, you passed!"));

            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshotAs, new File("./out.png"));
        } finally {
            awesomeDriver.quit();
        }
    }
}
