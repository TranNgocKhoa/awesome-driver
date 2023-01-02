package io.github.tranngockhoa.driver;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput;

import java.io.File;
import java.io.IOException;

public class TestNowSecure {
    @Test
    void test() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(false);
        try {
            awesomeDriver.get("https://nowsecure.nl");

            Thread.sleep(20000);
            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshotAs, new File("./out.png"));
        } finally {
            awesomeDriver.quit();
        }
    }

    @Test
    void testHeadless() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(true);
        try {
            awesomeDriver.get("https://nowsecure.nl");
            Thread.sleep(10000);
            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            System.out.println(awesomeDriver.getPageSource());

            FileUtils.copyFile(screenshotAs, new File("./outHeadless.png"));
        } finally {
            awesomeDriver.quit();
        }
    }

    @Test
    void testSanny() throws IOException, InterruptedException {
        var chromeDriverService = new AwesomeDriverService.Builder()
                .usingAnyFreePort()
                .build();
        AwesomeDriver awesomeDriver = new AwesomeDriver(chromeDriverService, null, false);

        System.out.println(System.getProperty("webdriver.chrome.driver"));
        try {
            awesomeDriver.get("https://bot.sannysoft.com/");
            Thread.sleep(100);
            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshotAs, new File("./outSanny.png"));
        } finally {
//            awesomeDriver.quit();
        }
    }

    @Test
    void creepJs() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(false);
        try {
            awesomeDriver.get("https://abrahamjuliot.github.io/creepjs/");
//            Thread.sleep(60000);
            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshotAs, new File("./outCreppJs.png"));
        } finally {
//            awesomeDriver.quit();
        }
    }

    @Test
    void creepJsHeadless() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(true);
        try {
            awesomeDriver.get("https://abrahamjuliot.github.io/creepjs/");
            Thread.sleep(60000);
            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshotAs, new File("./outCreppJsHeadless1.png"));
            WheelInput.ScrollOrigin scrollOrigin = WheelInput.ScrollOrigin.fromViewport();
            new Actions(awesomeDriver)
                    .scrollFromOrigin(scrollOrigin, 0, 500)
                    .perform();
            FileUtils.copyFile(screenshotAs, new File("./outCreppJsHeadless2.png"));
            scrollOrigin = WheelInput.ScrollOrigin.fromViewport(0, 500);
            new Actions(awesomeDriver)
                    .scrollFromOrigin(scrollOrigin, 0, 1000)
                    .perform();
            FileUtils.copyFile(screenshotAs, new File("./outCreppJsHeadless3.png"));
        } finally {
            awesomeDriver.quit();
        }
    }

    @Test
    void testHeadlessSanny() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(true);
        try {
            awesomeDriver.get("https://bot.sannysoft.com/");

            Thread.sleep(2000);
            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshotAs, new File("./outSannyHeadless.png"));
        } finally {
            awesomeDriver.quit();
        }
    }

    @Test
    void testHeadlessSite() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(true);
        try {
            awesomeDriver.get("https://arh.antoinevastel.com/bots/areyouheadless");

            Thread.sleep(5000);
            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshotAs, new File("./outHeadlessSite.png"));
        } finally {
            awesomeDriver.quit();
        }
    }
}
