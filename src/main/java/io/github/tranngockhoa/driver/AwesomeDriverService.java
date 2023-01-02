package io.github.tranngockhoa.driver;

import com.google.auto.service.AutoService;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static org.openqa.selenium.remote.Browser.CHROME;

public class AwesomeDriverService extends ChromeDriverService {
    public AwesomeDriverService(File executable, int port, List<String> args, Map<String, String> environment) throws IOException {
        super(executable, port, args, environment);
    }

    public AwesomeDriverService(File executable, int port, Duration timeout, List<String> args, Map<String, String> environment) throws IOException {
        super(executable, port, timeout, args, environment);
    }

    @AutoService(DriverService.Builder.class)
    public static class Builder extends DriverService.Builder<
            AwesomeDriverService, AwesomeDriverService.Builder> {

        private boolean disableBuildCheck = Boolean.getBoolean(CHROME_DRIVER_DISABLE_BUILD_CHECK);
        private boolean readableTimestamp = Boolean.getBoolean(CHROME_DRIVER_READABLE_TIMESTAMP);
        private boolean appendLog = Boolean.getBoolean(CHROME_DRIVER_APPEND_LOG_PROPERTY);
        private boolean verbose = Boolean.getBoolean(CHROME_DRIVER_VERBOSE_LOG_PROPERTY);
        private boolean silent = Boolean.getBoolean(CHROME_DRIVER_SILENT_OUTPUT_PROPERTY);
        private String allowedListIps = System.getProperty(CHROME_DRIVER_ALLOWED_IPS_PROPERTY,
                System.getProperty(CHROME_DRIVER_WHITELISTED_IPS_PROPERTY));
        private ChromeDriverLogLevel logLevel = ChromeDriverLogLevel.fromString(System.getProperty(CHROME_DRIVER_LOG_LEVEL_PROPERTY));

        @Override
        public int score(Capabilities capabilities) {
            int score = 0;

            if (CHROME.is(capabilities.getBrowserName())) {
                score++;
            }

            if (capabilities.getCapability(ChromeOptions.CAPABILITY) != null) {
                score++;
            }

            return score;
        }

        /**
         * Configures the driver server appending to log file.
         *
         * @param appendLog True for appending to log file, false otherwise.
         * @return A self reference.
         */
        public AwesomeDriverService.Builder withAppendLog(boolean appendLog) {
            this.appendLog = appendLog;
            return this;
        }

        /**
         * Allows the driver to be used with potentially incompatible versions of the browser.
         *
         * @param noBuildCheck True for not enforcing matching versions.
         * @return A self reference.
         */
        public AwesomeDriverService.Builder withBuildCheckDisabled(boolean noBuildCheck) {
            this.disableBuildCheck = noBuildCheck;
            return this;
        }

        /**
         * Configures the driver server verbosity.
         *
         * @param verbose True for verbose output, false otherwise.
         * @return A self reference.
         */
        @SuppressWarnings("UnusedReturnValue")
        public AwesomeDriverService.Builder withVerbose(boolean verbose) {
            if (verbose) {
                this.logLevel = ChromeDriverLogLevel.ALL;
            }
            this.verbose = false;
            return this;
        }

        /**
         * Configures the driver server verbosity.
         *
         * @param logLevel {@link ChromeDriverLogLevel} for desired log level output.
         * @return A self reference.
         */
        public AwesomeDriverService.Builder withLogLevel(ChromeDriverLogLevel logLevel) {
            this.verbose = false;
            this.silent = false;
            this.logLevel = logLevel;
            return this;
        }

        /**
         * Configures the driver server for silent output.
         *
         * @param silent True for silent output, false otherwise.
         * @return A self reference.
         */
        public AwesomeDriverService.Builder withSilent(boolean silent) {
            if (silent) {
                this.logLevel = ChromeDriverLogLevel.OFF;
            }
            this.silent = false;
            return this;
        }

        /**
         * Configures the comma-separated list of remote IPv4 addresses which are allowed to connect
         * to the driver server.
         *
         * @param allowedListIps Comma-separated list of remote IPv4 addresses.
         * @return A self reference.
         */
        @Deprecated
        public AwesomeDriverService.Builder withWhitelistedIps(String allowedListIps) {
            this.allowedListIps = allowedListIps;
            return this;
        }

        /**
         * Configures the format of the logging for the driver server.
         *
         * @param readableTimestamp Whether the timestamp of the log is readable.
         * @return A self reference.
         */
        public AwesomeDriverService.Builder withReadableTimestamp(Boolean readableTimestamp) {
            this.readableTimestamp = readableTimestamp;
            return this;
        }

        @Override
        protected File findDefaultExecutable() {
            return new File(System.getProperty(CHROME_DRIVER_EXE_PROPERTY));
        }

        @Override
        protected List<String> createArgs() {
            if (getLogFile() == null) {
                String logFilePath = System.getProperty(CHROME_DRIVER_LOG_PROPERTY);
                if (logFilePath != null) {
                    withLogFile(new File(logFilePath));
                }
            }

            // If set in properties and not overwritten by method
            if (verbose) {
                withVerbose(true);
            }
            if (silent) {
                withSilent(true);
            }

            List<String> args = new ArrayList<>();

            args.add(String.format("--port=%d", getPort()));
            if (getLogFile() != null) {
                args.add(String.format("--log-path=%s", getLogFile().getAbsolutePath()));
                // This flag only works when logged to file
                if (readableTimestamp) {
                    args.add("--readable-timestamp");
                }
            }
            if (appendLog) {
                args.add("--append-log");
            }
            if (logLevel != null) {
                args.add(String.format("--log-level=%s", logLevel.toString().toUpperCase()));
            }
            if (allowedListIps != null) {
                args.add(String.format("--allowed-ips=%s", allowedListIps));
            }
            if (disableBuildCheck) {
                args.add("--disable-build-check");
            }

            return unmodifiableList(args);
        }

        @Override
        protected AwesomeDriverService createDriverService(
                File exe,
                int port,
                Duration timeout,
                List<String> args,
                Map<String, String> environment) {
            try {
                return new AwesomeDriverService(exe, port, timeout, args, environment);
            } catch (IOException e) {
                throw new WebDriverException(e);
            }
        }

        @Override
        public AwesomeDriverService build() {
            String driverProperty = System.getProperty(CHROME_DRIVER_EXE_PROPERTY);
            if (driverProperty == null || !driverProperty.contains("awesome")) {
                new Patcher().setup();
            }

            return super.build();
        }
    }


}
