package ua.kpi.ipze.messaging.data;

import java.util.Properties;

public final class Datasource {

    private static Datasource datasource;

    private String url;
    private String username;
    private String password;
    private String schema;

    private Datasource(String url, String username, String password, String schema) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.schema = schema;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public static Datasource getInstance() {
        synchronized (Datasource.class) {
            if (datasource == null) {
                Properties properties = new DatasourceReader().readProperties();
                datasource =  new Datasource(
                        properties.getProperty(DatasourceReader.DATABASE_URL_PROPERTY),
                        properties.getProperty(DatasourceReader.DATABASE_USERNAME_PROPERTY),
                        properties.getProperty(DatasourceReader.DATABASE_PASSWORD_PROPERTY),
                        properties.getProperty(DatasourceReader.DATABASE_SCHEMA_PROPERTY)
                );
            }
            return datasource;
        }
    }

}
