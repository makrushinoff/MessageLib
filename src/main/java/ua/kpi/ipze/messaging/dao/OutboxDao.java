package ua.kpi.ipze.messaging.dao;

import ua.kpi.ipze.messaging.data.Datasource;
import ua.kpi.ipze.messaging.exception.DatabaseIntegrationException;
import ua.kpi.ipze.messaging.exception.TableDoesNotExistsException;
import ua.kpi.ipze.messaging.model.Outbox;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

public final class OutboxDao {

    private static final String TABLE_NAME = "outbox";

    private static final String INSERT_QUERY = "INSERT INTO %s.outbox (id, queue, message, creation_date_time) VALUES (?, ?, ?, ?);";
    private static final String UPDATE_QUERY = "UPDATE %s.outbox SET sent_date_time = ? WHERE id = ?;";
    private static final String FETCH_QUERY = "SELECT * FROM %s.outbox where sent_date_time is null LIMIT ?;";
    private static final String DATA_RETENTION_FETCH_QUERY = "SELECT * FROM %s.outbox where sent_date_time < ? LIMIT ?;";
    private static final String DELETE_QUERY = "DELETE FROM %s.outbox WHERE id in (_ID_LIST_);";

    private static OutboxDao instance;

    private final Datasource datasource;

    private OutboxDao(Datasource datasource) {
        this.datasource = datasource;
    }

    public static OutboxDao getInstance() {
        synchronized (OutboxDao.class) {
            Datasource datasource = Datasource.getInstance();
            if (instance == null) {
                instance = new OutboxDao(datasource);
            }
            instance.checkTableExists();
            return instance;
        }
    }

    public void create(Outbox outbox) {
        String sql = String.format(INSERT_QUERY, datasource.getSchema());
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, outbox.getId()); // UUID
            preparedStatement.setString(2, outbox.getQueue());
            preparedStatement.setString(3, outbox.getMessage());
            preparedStatement.setObject(4, outbox.getCreationDateTime());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseIntegrationException("Error when create Outbox object", e);
        }
    }

    public List<Outbox> fetch(int limit) {
        List<Outbox> entities = new ArrayList<>();
        String sql = String.format(FETCH_QUERY, datasource.getSchema());
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, limit);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Outbox outbox = new Outbox();
                    outbox.setId((UUID) resultSet.getObject("id"));
                    outbox.setQueue(resultSet.getString("queue"));
                    outbox.setMessage(resultSet.getString("message"));
                    outbox.setCreationDateTime(resultSet.getObject("creation_date_time", LocalDateTime.class));
                    outbox.setSentDateTime(resultSet.getObject("sent_date_time", LocalDateTime.class));
                    entities.add(outbox);
                }
            } catch (Exception e) {
                return entities;
            }
        } catch (SQLException e) {
            throw new DatabaseIntegrationException("Error when fetch Outbox objects", e);
        }
        return entities;
    }

    public List<Outbox> fetchByDateBefore(int limit, LocalDateTime dateTime) {
        List<Outbox> entities = new ArrayList<>();
        String sql = String.format(DATA_RETENTION_FETCH_QUERY, datasource.getSchema());
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, dateTime);
            preparedStatement.setInt(2, limit);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Outbox outbox = new Outbox();
                    outbox.setId((UUID) resultSet.getObject("id"));
                    outbox.setQueue(resultSet.getString("queue"));
                    outbox.setMessage(resultSet.getString("message"));
                    outbox.setCreationDateTime(resultSet.getObject("creation_date_time", LocalDateTime.class));
                    outbox.setSentDateTime(resultSet.getObject("sent_date_time", LocalDateTime.class));
                    entities.add(outbox);
                }
            } catch (Exception e) {
                return entities;
            }
        } catch (SQLException e) {
            throw new DatabaseIntegrationException("Error when fetch Outbox objects", e);
        }
        return entities;
    }

    public void updateEntities(List<Outbox> outboxEntities) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < outboxEntities.size(); i++) {
            stringBuilder.append(String.format(UPDATE_QUERY, datasource.getSchema()));
        }
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())) {
            int outboxIndex = 0;
            for (int i = 0; i < outboxEntities.size() * 2; i += 2) {
                if (outboxEntities.get(outboxIndex).getSentDateTime() != null) {
                    preparedStatement.setObject(i + 1, outboxEntities.get(outboxIndex).getSentDateTime());
                } else {
                    preparedStatement.setNull(i + 1, Types.TIMESTAMP);
                }
                preparedStatement.setObject(i + 2, outboxEntities.get(outboxIndex).getId());
                outboxIndex++;
            }
            int rowsAffected = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseIntegrationException("Error when update Outbox objects", e);
        }
    }

    public void deleteEntitiesBy(List<UUID> ids) {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (UUID id : ids) {
            stringJoiner.add(id.toString());
        }
        String query = DELETE_QUERY.replace("_ID_LIST_", stringJoiner.toString());
        String sql = String.format(query, datasource.getSchema());
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            int rowsAffected = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseIntegrationException("Error when delete Outbox objects", e);
        }
    }

    public void checkTableExists() {
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword())) {
            if (!doesTableExist(connection, TABLE_NAME)) {
                throw new TableDoesNotExistsException("Table " + TABLE_NAME + " does not exists");
            }
        } catch (SQLException e) {
            throw new DatabaseIntegrationException("Error when check outbox table", e);
        }
    }

    private boolean doesTableExist(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, datasource.getSchema(), tableName, new String[] {"TABLE"})) {
             return resultSet.next();
        }
    }

}
