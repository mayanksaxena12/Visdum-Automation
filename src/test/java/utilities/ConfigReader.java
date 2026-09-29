package utilities;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigReader {

    static Properties properties;

    static {

        try {

            FileInputStream file =
                    new FileInputStream(
                            "src/test/resources/config.properties");

            properties = new Properties();

            properties.load(file);

        } catch(Exception e) {

            e.printStackTrace();
        }
    }

    public static String get(String key) {
        if (properties == null) {
            return null;
        }
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        if (properties == null) {
            return defaultValue;
        }
        String value = properties.getProperty(key);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }
}