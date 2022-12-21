package io.github.tranngockhoa.driver;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.OutputType;

import java.io.File;
import java.io.IOException;

public class TestNowSecure {
    @Test
    void test() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(false);
        try {
            awesomeDriver.get("https://nowsecure.nl");

            Thread.sleep(10000);
            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshotAs, new File("./out.png"));
        } finally {
            awesomeDriver.quit();
        }
    }

    @Test
    void testSanny() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(false);
        try {
            awesomeDriver.get("https://bot.sannysoft.com/");
            Thread.sleep(10000);
            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshotAs, new File("./outSanny.png"));
        } finally {
            awesomeDriver.quit();
        }
    }

    @Test
    void testHeadless() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(true);
        try {
            awesomeDriver.get("https://nowsecure.nl");
            Thread.sleep(30000);
            File screenshotAs = awesomeDriver.getScreenshotAs(OutputType.FILE);

            System.out.println(awesomeDriver.getPageSource());

            FileUtils.copyFile(screenshotAs, new File("./outHeadless.png"));
        } finally {
            awesomeDriver.quit();
        }
    }

    @Test
    void testHeadlessSanny() throws IOException, InterruptedException {
        AwesomeDriver awesomeDriver = new AwesomeDriver(true);
        try {
            awesomeDriver.get("https://bot.sannysoft.com/");

            Thread.sleep(10000);
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
