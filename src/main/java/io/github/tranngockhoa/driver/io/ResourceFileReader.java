package io.github.tranngockhoa.driver.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ResourceFileReader {
    public String getFileContent(String resourceFileName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceFileName)) {
            if (is == null) {
                return "";
            }

            StringBuilder fileContent = new StringBuilder();
            try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(streamReader)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line);
                    fileContent.append(System.lineSeparator());
                }
            }

            return fileContent.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error when reading file " + resourceFileName);
        }
    }
}
