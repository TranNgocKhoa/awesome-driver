package com.khoa.driver;

import com.khoa.driver.proxy.ProxyConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;

import java.util.*;

public class AwesomeDriver implements WebDriver, HasDevTools, TakesScreenshot {
    private final ChromeDriver chromeDriver;
    private final ChromeOptions options;
    private final boolean isHeadless = false;


    public AwesomeDriver(ChromeOptions chromeOptions) {
        this(chromeOptions, null, false);
    }

    public AwesomeDriver(boolean isHeadless) {
        this(null, null, isHeadless);
    }

    public AwesomeDriver(ProxyConfig proxyConfig) {
        this(null, proxyConfig, false);
    }

    public AwesomeDriver(ProxyConfig proxyConfig, boolean isHeadless) {
        this(null, proxyConfig, isHeadless);
    }

    public AwesomeDriver() {
        this(null, null, false);
    }

    public AwesomeDriver(ChromeOptions chromeOptions, ProxyConfig proxyConfig, boolean isHeadless) {
        new Patcher().setup();
        this.options = this.patchingOption(chromeOptions);
        this.options.setHeadless(isHeadless);
        if (proxyConfig != null) {
            this.setProxy(proxyConfig);
        }
        this.chromeDriver = new ChromeDriver(options);

        if (isHeadless) {
            this.patchingCdc();
        }
    }


    private void patchingCdc() {
        chromeDriver.executeCdpCommand("Network.setUserAgentOverride", Map.of(
                "userAgent", chromeDriver.executeScript("return navigator.userAgent").toString().replace("Headless", "")
        ));

        if (chromeDriver.executeScript("return navigator.webdriver") != null) {
            chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument",
                    Map.of("source", "Object.defineProperty(window, 'navigator', {\n" +
                            "                                value: new Proxy(navigator, {\n" +
                            "                                        has: (target, key) => (key === 'webdriver' ? false : key in target),\n" +
                            "                                        get: (target, key) =>\n" +
                            "                                                key === 'webdriver' ?\n" +
                            "                                                false :\n" +
                            "                                                typeof target[key] === 'function' ?\n" +
                            "                                                target[key].bind(target) :\n" +
                            "                                                target[key]\n" +
                            "                                        })\n" +
                            "                            });")
            );

            chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument",
                    Map.of("source", "Object.defineProperty(navigator, 'maxTouchPoints', {get: () => 1});\n" +
                            "                            Object.defineProperty(navigator.connection, 'rtt', {get: () => 100});\n" +
                            "                            // https://github.com/microlinkhq/browserless/blob/master/packages/goto/src/evasions/chrome-runtime.js\n" +
                            "                            window.chrome = {\n" +
                            "                                app: {\n" +
                            "                                    isInstalled: false,\n" +
                            "                                    InstallState: {\n" +
                            "                                        DISABLED: 'disabled',\n" +
                            "                                        INSTALLED: 'installed',\n" +
                            "                                        NOT_INSTALLED: 'not_installed'\n" +
                            "                                    },\n" +
                            "                                    RunningState: {\n" +
                            "                                        CANNOT_RUN: 'cannot_run',\n" +
                            "                                        READY_TO_RUN: 'ready_to_run',\n" +
                            "                                        RUNNING: 'running'\n" +
                            "                                    }\n" +
                            "                                },\n" +
                            "                                runtime: {\n" +
                            "                                    OnInstalledReason: {\n" +
                            "                                        CHROME_UPDATE: 'chrome_update',\n" +
                            "                                        INSTALL: 'install',\n" +
                            "                                        SHARED_MODULE_UPDATE: 'shared_module_update',\n" +
                            "                                        UPDATE: 'update'\n" +
                            "                                    },\n" +
                            "                                    OnRestartRequiredReason: {\n" +
                            "                                        APP_UPDATE: 'app_update',\n" +
                            "                                        OS_UPDATE: 'os_update',\n" +
                            "                                        PERIODIC: 'periodic'\n" +
                            "                                    },\n" +
                            "                                    PlatformArch: {\n" +
                            "                                        ARM: 'arm',\n" +
                            "                                        ARM64: 'arm64',\n" +
                            "                                        MIPS: 'mips',\n" +
                            "                                        MIPS64: 'mips64',\n" +
                            "                                        X86_32: 'x86-32',\n" +
                            "                                        X86_64: 'x86-64'\n" +
                            "                                    },\n" +
                            "                                    PlatformNaclArch: {\n" +
                            "                                        ARM: 'arm',\n" +
                            "                                        MIPS: 'mips',\n" +
                            "                                        MIPS64: 'mips64',\n" +
                            "                                        X86_32: 'x86-32',\n" +
                            "                                        X86_64: 'x86-64'\n" +
                            "                                    },\n" +
                            "                                    PlatformOs: {\n" +
                            "                                        ANDROID: 'android',\n" +
                            "                                        CROS: 'cros',\n" +
                            "                                        LINUX: 'linux',\n" +
                            "                                        MAC: 'mac',\n" +
                            "                                        OPENBSD: 'openbsd',\n" +
                            "                                        WIN: 'win'\n" +
                            "                                    },\n" +
                            "                                    RequestUpdateCheckStatus: {\n" +
                            "                                        NO_UPDATE: 'no_update',\n" +
                            "                                        THROTTLED: 'throttled',\n" +
                            "                                        UPDATE_AVAILABLE: 'update_available'\n" +
                            "                                    }\n" +
                            "                                }\n" +
                            "                            }\n" +
                            "                            // https://github.com/microlinkhq/browserless/blob/master/packages/goto/src/evasions/navigator-permissions.js\n" +
                            "                            if (!window.Notification) {\n" +
                            "                                window.Notification = {\n" +
                            "                                    permission: 'denied'\n" +
                            "                                }\n" +
                            "                            }\n" +
                            "                            const originalQuery = window.navigator.permissions.query\n" +
                            "                            window.navigator.permissions.__proto__.query = parameters =>\n" +
                            "                                parameters.name === 'notifications'\n" +
                            "                                    ? Promise.resolve({ state: window.Notification.permission })\n" +
                            "                                    : originalQuery(parameters)\n" +
                            "                            const oldCall = Function.prototype.call\n" +
                            "                            function call() {\n" +
                            "                                return oldCall.apply(this, arguments)\n" +
                            "                            }\n" +
                            "                            Function.prototype.call = call\n" +
                            "                            const nativeToStringFunctionString = Error.toString().replace(/Error/g, 'toString')\n" +
                            "                            const oldToString = Function.prototype.toString\n" +
                            "                            function functionToString() {\n" +
                            "                                if (this === window.navigator.permissions.query) {\n" +
                            "                                    return 'function query() { [native code] }'\n" +
                            "                                }\n" +
                            "                                if (this === functionToString) {\n" +
                            "                                    return nativeToStringFunctionString\n" +
                            "                                }\n" +
                            "                                return oldCall.call(oldToString, this)\n" +
                            "                            }\n" +
                            "                            // eslint-disable-next-line\n" +
                            "                            Function.prototype.toString = functionToString")
            );
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
}
