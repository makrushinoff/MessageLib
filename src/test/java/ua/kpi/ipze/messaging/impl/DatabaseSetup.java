package ua.kpi.ipze.messaging.impl;

import ua.kpi.ipze.messaging.data.Datasource;

import java.sql.*;

public class DatabaseSetup {

    private static final String CREATE_TABLES_SQL = """
            CREATE TABLE IF NOT EXISTS inbox (
            id UUID primary key,
            queue varchar not null,
            message text not null,
            received_date_time timestamp not null,
            handled_date_time timestamp
            );
            CREATE TABLE IF NOT EXISTS outbox (
            id UUID primary key,
            queue varchar not null,
            message text not null,
            creation_date_time timestamp not null,
            sent_date_time timestamp 
            );
            """;
    private final Datasource datasource = Datasource.getInstance();

    public void createTablesForTest() {
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_TABLES_SQL);
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly in real-world applications
        }
    }
}
