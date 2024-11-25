package ua.kpi.ipze.messaging.data;

import ua.kpi.ipze.messaging.exception.PropertyFileInvalidException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DatasourceReader {

    private static final String PROPERTY_FILE_NAME = "messaging.properties";

    public static final String DATABASE_URL_PROPERTY = "messaging.database.url";
    public static final String DATABASE_USERNAME_PROPERTY = "messaging.database.username";
    public static final String DATABASE_PASSWORD_PROPERTY = "messaging.database.password";
    public static final String DATABASE_SCHEMA_PROPERTY = "messaging.database.schema";

    public Properties readProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME)) {
            if (input == null) {
                throw new PropertyFileInvalidException("Could not find " + PROPERTY_FILE_NAME + " file");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new PropertyFileInvalidException("Could not read " + PROPERTY_FILE_NAME + " file", ex);
        }

        return properties;
    }

}
