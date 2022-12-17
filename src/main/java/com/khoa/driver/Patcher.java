package com.khoa.driver;

import com.khoa.driver.enums.ArchitectureType;
import com.khoa.driver.enums.PlatformType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.Random;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Patcher {
    private static final Logger LOGGER = Logger.getLogger(Patcher.class.getName());
    private static final String REPOSITORY_HOST = "https://chromedriver.storage.googleapis.com";
    private static final String DRIVER_ZIP_PATTERN = "chromedriver_%s.zip";
    private static final String EXECUTE_FILE_PATTERN = "chromedriver%s";
    private static final String CACHE_PATH = "/.cache/awesome_driver";
    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private final String mainVersion;
    private final String fullVersion;
    private final String driverExecutablePath;
    private final String driverFolder;
    private final String driverZipPath;

    public Patcher(String mainVersion) {
        this.mainVersion = mainVersion;
        this.fullVersion = fetchReleaseNumber();
        this.driverFolder = this.getDriverFolderPath();
        this.driverExecutablePath = this.getDriverExecutablePath();
        this.driverZipPath = driverFolder + "/" + String.format(DRIVER_ZIP_PATTERN, this.getRemoteFileName());
    }

    public void setup() {
        this.fetchDriver();
        this.unzip();
        this.makeDriverExecutable();
        if (!this.isPatched()) {
            this.patchDriver();
        }

        System.setProperty("webdriver.chrome.driver", this.driverExecutablePath);
    }

    private void patchDriver() {
        StringBuilder inputBuffer = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(this.driverExecutablePath))) {
            String newCdc = this.generateRandomCdc();
            String line = reader.readLine();
            while (line != null) {

                if (line.contains("cdc_")) {
                    inputBuffer.append(line.replaceAll("cdc_.{22}", newCdc));
                } else {
                    inputBuffer.append(line);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while patching driver!");
        }

        try (FileOutputStream fileOut = new FileOutputStream(this.driverExecutablePath)) {
            fileOut.write(inputBuffer.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void makeDriverExecutable() {
        new File(this.driverExecutablePath).setExecutable(true);
    }

    private String getDownloadDriverUrl() {
        String baseUrl = REPOSITORY_HOST + "/" + fullVersion + "/";

        return baseUrl + this.getRemoteFileName();
    }

    private String getRemoteFileName() {
        PlatformType platform = SystemHelper.instance().platform();
        if (platform == PlatformType.WINDOWS) {
            return String.format(DRIVER_ZIP_PATTERN, "win32");
        }
        if (platform == PlatformType.LINUX) {
            return String.format(DRIVER_ZIP_PATTERN, "linux64");
        }

        ArchitectureType architecture = SystemHelper.instance().architecture();
        if (platform == PlatformType.MAC) {
            if (architecture == ArchitectureType.ARM64) {
                return String.format(DRIVER_ZIP_PATTERN, "mac64_m1");
            }
            return String.format(DRIVER_ZIP_PATTERN, "mac64");
        }

        throw new UnsupportedOperationException();
    }

    private String getExecuteFileName() {
        PlatformType platform = SystemHelper.instance().platform();
        if (platform == PlatformType.WINDOWS) {
            return String.format(EXECUTE_FILE_PATTERN, ".exe");
        }

        return String.format(EXECUTE_FILE_PATTERN, "");
    }

    private String fetchReleaseNumber() {
        String versionInfoUrl = this.getVersionInfoUrl();

        HttpURLConnection con = null;
        try {
            URL url = new URL(versionInfoUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private String getVersionInfoUrl() {
        String path = "/latest_release".toUpperCase();

        String urlString = REPOSITORY_HOST + path;
        if (mainVersion != null && !mainVersion.isEmpty()) {
            urlString += "_" + mainVersion;
        }
        return urlString;
    }

    private void fetchDriver() {
        if ((Path.of(driverExecutablePath).toFile().exists())) {
            new File(driverExecutablePath).setExecutable(true);
            LOGGER.info("Driver exist. Made it executable.");
            return;
        } else {
            new File(this.driverFolder).mkdirs();
        }

        try {
            LOGGER.info(() -> String.format("Downloading driver version %s...", fullVersion));

            URL url = new URL(this.getDownloadDriverUrl());
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            try (FileOutputStream fileOutputStream = new FileOutputStream(this.driverZipPath, false)) {
                FileChannel fileChannel = fileOutputStream.getChannel();
                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }

            LOGGER.info(() -> String.format("Downloaded driver version %s!", fullVersion));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void unzip() {
        File dir = new File(this.driverFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        byte[] buffer = new byte[1024];
        try (FileInputStream fileInputStream = new FileInputStream(this.driverZipPath); ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            while (nextEntry != null) {
                String fileName = nextEntry.getName();
                File newFile = new File(this.driverFolder + File.separator + fileName);
                LOGGER.info(() -> "Unzipping to " + newFile.getAbsolutePath());
                new File(newFile.getParent()).mkdirs();
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                zipInputStream.closeEntry();
                nextEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException("Error when unzip driver!");
        }
    }

    private boolean isPatched() {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.driverExecutablePath))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("cdc_")) {
                    return false;
                }
                line = reader.readLine();
            }

            return true;
        } catch (IOException e) {
            throw new RuntimeException("Error while checking driver patched or not!");
        }
    }

    private String getDriverExecutablePath() {
        String driverFolder = System.getProperty("user.home") + CACHE_PATH + "/" + fullVersion;

        return driverFolder + "/" + getExecuteFileName();
    }

    private String getDriverFolderPath() {
        return System.getProperty("user.home") + CACHE_PATH + "/" + fullVersion;
    }

    private String generateRandomCdc() {
        char[] array = new char[26];
        Random random = new Random();
        for (int i = 0; i < 26; i++) {
            if (i == 3) {
                array[i] = '_';
            } else if (i == 20 || i == 21) {
                array[i] = Character.toUpperCase(ALPHABET[random.nextInt(26)]);
            } else {
                array[i] = ALPHABET[random.nextInt(26)];
            }

            array[2] = array[0];
        }

        return new String(array);
    }
}
