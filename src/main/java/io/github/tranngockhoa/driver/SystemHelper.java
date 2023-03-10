package io.github.tranngockhoa.driver;

import io.github.tranngockhoa.driver.enums.ArchitectureType;
import io.github.tranngockhoa.driver.enums.PlatformType;

public class SystemHelper {
    private static final class InstanceHolder {
        private static final SystemHelper INSTANCE = new SystemHelper();
    }

    public static SystemHelper instance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * @return Platform Type
     */
    public PlatformType platform() {
        String osName = System.getProperty("os.name");

        if (osName.toLowerCase().contains("windows")) {
            return PlatformType.WINDOWS;
        }
        if (osName.toLowerCase().contains("mac")) {
            return PlatformType.MAC;
        }

        return PlatformType.LINUX;
    }

    public ArchitectureType architecture() {
        if (ArchitectureType.ARM64.matchString(System.getProperty("os.arch"))) {
            return ArchitectureType.ARM64;
        }
        if (System.getProperty("sun.arch.data.model").contains("32")) {
            return ArchitectureType.X32;
        }

        return ArchitectureType.X64;
    }
}
