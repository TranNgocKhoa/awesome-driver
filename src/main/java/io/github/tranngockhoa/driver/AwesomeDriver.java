package io.github.tranngockhoa.driver;

import io.github.tranngockhoa.driver.io.ResourceFileReader;
import io.github.tranngockhoa.driver.proxy.ProxyConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;

import java.util.*;

public class AwesomeDriver implements WebDriver, HasDevTools, TakesScreenshot, JavascriptExecutor, Interactive {
    private final ResourceFileReader resourceFileReader = new ResourceFileReader();
    private final ChromeDriver chromeDriver;
    private final ChromeOptions options;

    public AwesomeDriver(ChromeOptions chromeOptions) {
        this(null, chromeOptions, null, false);
    }

    public AwesomeDriver(ChromeDriverService service, ProxyConfig proxyConfig, boolean isHeadless) {
        this(service, null, proxyConfig, isHeadless);
    }

    public AwesomeDriver(boolean isHeadless) {
        this(null, null, null, isHeadless);
    }

    public AwesomeDriver(ProxyConfig proxyConfig) {
        this(null, null, proxyConfig, false);
    }

    public AwesomeDriver(ProxyConfig proxyConfig, boolean isHeadless) {
        this(null, null, proxyConfig, isHeadless);
    }

    public AwesomeDriver() {
        this(null, null, null, false);
    }

    public AwesomeDriver(ChromeDriverService service, ChromeOptions chromeOptions, ProxyConfig proxyConfig, boolean isHeadless) {
        new Patcher().setup();
        this.options = this.patchingOption(chromeOptions);
        this.options.setHeadless(isHeadless);
        if (proxyConfig != null) {
            this.setProxy(proxyConfig);
        }
        if (service != null) {
            this.chromeDriver = new ChromeDriver(service, options);
        } else {
            this.chromeDriver = new ChromeDriver(options);
        }

        if (isHeadless) {
            this.evade();
        }
    }


    public void setProxy(ProxyConfig proxyConfig) {
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyConfig.getHost() + ":" + proxyConfig.getPort());
        proxy.setSocksUsername(proxyConfig.getUsername());
        proxy.setSocksPassword(proxyConfig.getPassword());

        options.setCapability("proxy", proxy);
    }

    @Override
    public void get(String url) {
        this.executePatchCdp();

        chromeDriver.get(url);
    }

    @Override
    public String getCurrentUrl() {
        return null;
    }

    @Override
    public String getTitle() {
        return chromeDriver.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return chromeDriver.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return chromeDriver.findElement(by);
    }

    @Override
    public String getPageSource() {
        return chromeDriver.getPageSource();
    }

    @Override
    public void close() {
        chromeDriver.close();
    }

    @Override
    public void quit() {
        chromeDriver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return chromeDriver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return chromeDriver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return chromeDriver.switchTo();
    }

    @Override
    public Navigation navigate() {
        return chromeDriver.navigate();
    }

    @Override
    public Options manage() {
        return chromeDriver.manage();
    }

    private void executePatchCdp() {
        Object objectToInspect = chromeDriver.executeScript("let objectToInspect = window,\n" + "                result = [];\n" + "            while(objectToInspect !== null)\n" + "            { result = result.concat(Object.getOwnPropertyNames(objectToInspect));\n" + "              objectToInspect = Object.getPrototypeOf(objectToInspect); }\n" + "            return result.filter(i => i.match(/.+_.+_(Array|Promise|Symbol)/ig))");

        if (objectToInspect != null) {
            chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", "let objectToInspect = window,\n" + "                        result = [];\n" + "                    while(objectToInspect !== null) \n" + "                    { result = result.concat(Object.getOwnPropertyNames(objectToInspect));\n" + "                      objectToInspect = Object.getPrototypeOf(objectToInspect); }\n" + "                    result.forEach(p => p.match(/.+_.+_(Array|Promise|Symbol)/ig)\n" + "                                        &&delete window[p]&&console.log('removed',p))"));
        }
    }

    private ChromeOptions patchingOption(ChromeOptions options) {
        if (options == null) {
            options = new ChromeOptions();
        }
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--no-first-run");
        options.addArguments("--lang=en-US");
        options.addArguments("--start-maximized");
        options.addArguments("--no-sandbox");
        options.addArguments("--test-type");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.addArguments("disable-infobars");

        return options;
    }

    @Override
    public Optional<DevTools> maybeGetDevTools() {
        return chromeDriver.maybeGetDevTools();
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        return chromeDriver.getScreenshotAs(target);
    }

    @Override
    public Object executeScript(String script, Object... args) {
        return chromeDriver.executeScript(script, args);
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        return chromeDriver.executeAsyncScript(script, args);
    }

    private void evade() {
        String hideNavigationPlugin = resourceFileReader.getFileContent("navigationPlugin.js");
        chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", hideNavigationPlugin));

        String userAgentPatch = chromeDriver.executeScript("return navigator.userAgent").toString().replace("Headless", "");
        chromeDriver.executeCdpCommand("Network.setUserAgentOverride", Map.of("userAgent", userAgentPatch));

        if (chromeDriver.executeScript("return navigator.webdriver") != null) {
            String hideWebdriverNavigatorPatch = resourceFileReader.getFileContent("hideNavigatorWebDriver.js");
            chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", hideWebdriverNavigatorPatch));

            String chromeRuntimePatch = resourceFileReader.getFileContent("chromeRuntime.js");
            chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", chromeRuntimePatch));

            String navigatorPermissionPatch = resourceFileReader.getFileContent("navigatorPermission.js");
            chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", navigatorPermissionPatch));
        }

        String bGl = resourceFileReader.getFileContent("webGL.js");
        chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", bGl));

        String windowFramePatch = resourceFileReader.getFileContent("windowFrame.js");
        chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", windowFramePatch));

        String mediaCodec = resourceFileReader.getFileContent("mediaCodec.js");
        chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", mediaCodec));
    }

    @Override
    public void perform(Collection<Sequence> actions) {
        chromeDriver.perform(actions);
    }

    @Override
    public void resetInputState() {
        chromeDriver.resetInputState();
    }
}
