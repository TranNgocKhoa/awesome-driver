package io.github.tranngockhoa.driver;

import io.github.tranngockhoa.driver.enums.ArchitectureType;
import io.github.tranngockhoa.driver.enums.PlatformType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Patcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Patcher.class);
    private static final String REPOSITORY_HOST = "https://chromedriver.storage.googleapis.com";
    private static final String DRIVER_ZIP_PATTERN = "chromedriver_%s.zip";
    private static final String EXECUTE_FILE_PATTERN = "chromedriver%s";
    private static final String CACHE_PATH = "/.cache/awesome_driver";
    private static final String CACHE_WIN_PATH = ".\\awesome_driver";
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
        this.driverZipPath = driverFolder + File.separator + String.format(DRIVER_ZIP_PATTERN, this.getRemoteFileName());
    }

    public Patcher() {
        this(null);
    }


    public void setup() {
        File driverFile = Path.of(driverExecutablePath).toFile();
        LOGGER.info("Checking driver exist....");
        if (!driverFile.exists()) {
            LOGGER.info("Not exist, trying to create new...");
            this.fetchDriver();
            this.unzip();
        } else {
            LOGGER.info("Driver is existed!");
        }
        this.makeDriverExecutable();
        if (!this.isPatched()) {
            this.patchDriver();
        }

        System.setProperty("webdriver.chrome.driver", this.driverExecutablePath);
    }

    @Deprecated
    private void patchDriverOld() {
        File file = new File(this.driverExecutablePath);
        int length = (int) file.length();
        byte[] data;
        try (FileInputStream in = new FileInputStream(file);
             ByteArrayOutputStream bs = new ByteArrayOutputStream(length)) {
            byte[] buffer = new byte[128_000];
            int len;
            while ((len = in.read(buffer)) > 0) {
                bs.write(buffer, 0, len);
            }
            data = bs.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.searchAndReplace(data);

        try (FileOutputStream out = new FileOutputStream(file);
             ByteArrayInputStream bs = new ByteArrayInputStream(data)) {
            byte[] buffer = new byte[128_000];
            int len;
            while ((len = bs.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void patchDriver() {
        String randomCdc = this.generateRandomCdc();

        int chunkSize = 1024;
        byte[] bufferArray = new byte[chunkSize];
        long currentPosition = 0L;
        String pattern = "cdc_";
        try (RandomAccessFile driverFile = new RandomAccessFile(this.driverExecutablePath, "rw")) {
            while (currentPosition < driverFile.length()) {

                int readingByteCount = driverFile.read(bufferArray);
                String readingString = new String(bufferArray, 0, readingByteCount);
                int targetPosition = readingString.indexOf(pattern);

                while (targetPosition != -1) {
                    driverFile.seek(currentPosition + targetPosition);
                    driverFile.write(randomCdc.getBytes(StandardCharsets.US_ASCII));

                    currentPosition += targetPosition + randomCdc.length();
                    driverFile.seek(currentPosition);

                    readingByteCount = driverFile.read(bufferArray);
                    readingString = new String(bufferArray, 0, readingByteCount);
                    targetPosition = readingString.indexOf(randomCdc);
                }

                currentPosition += chunkSize;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error when reading driver file to patch!");
        }
    }

    private void searchAndReplace(byte[] data) {
        byte[] newCdc = this.generateRandomCdc().getBytes(StandardCharsets.US_ASCII);
        byte[] patternCharArray = "cdc_".getBytes(StandardCharsets.US_ASCII);
        Pattern pattern = Pattern.compile("cdc_.{22}");

        for (int i = 0; i < data.length - newCdc.length; i++) {
            if (data[i] == patternCharArray[0] && data[i + 1] == patternCharArray[1] && data[i + 2] == patternCharArray[2] && data[i + 3] == patternCharArray[3]) {
                String comparingValue = new String(data, i, newCdc.length, StandardCharsets.US_ASCII);
                if (pattern.matcher(comparingValue).matches()) {
                    System.arraycopy(newCdc, 0, data, i, newCdc.length);
                    i += newCdc.length;
                }
            }
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
        new File(this.driverFolder).mkdirs();
        try {
            LOGGER.info(String.format("Downloading driver version %s...", fullVersion));

            URL url = new URL(this.getDownloadDriverUrl());
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            try (FileOutputStream fileOutputStream = new FileOutputStream(this.driverZipPath, false)) {
                FileChannel fileChannel = fileOutputStream.getChannel();
                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }

            LOGGER.info(String.format("Downloaded driver version %s!", fullVersion));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void unzip() {
        byte[] buffer = new byte[1024];
        try (FileInputStream fileInputStream = new FileInputStream(this.driverZipPath); ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            while (nextEntry != null) {
                String fileName = nextEntry.getName();
                File newFile = new File(this.driverFolder + File.separator + fileName);
                LOGGER.info("Unzipping to " + newFile.getAbsolutePath());
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
        String driverFolder = System.getProperty("user.home") + getCachePath() + File.separator + fullVersion;
        return driverFolder + File.separator + getExecuteFileName();
    }

    private String getDriverFolderPath() {
        return System.getProperty("user.home") + getCachePath() + File.separator + fullVersion;
    }

    private String getCachePath() {
        PlatformType platform = SystemHelper.instance().platform();
        if (platform == PlatformType.WINDOWS) {
            return CACHE_WIN_PATH;
        }

        return CACHE_PATH;
    }

    private String generateRandomCdc() {
        char[] array = new char[26];
        Random random = new Random();
        for (int i = 0; i < 26; i++) {
            if (i == 0) {
                char aChar = ALPHABET[random.nextInt(26)];
                if (aChar == 'c') {
                    aChar += 1;
                }
                array[i] = aChar;
            } else if (i == 3) {
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
