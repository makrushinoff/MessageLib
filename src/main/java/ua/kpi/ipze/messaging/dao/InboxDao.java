package ua.kpi.ipze.messaging.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.ipze.messaging.data.Datasource;
import ua.kpi.ipze.messaging.exception.DatabaseIntegrationException;
import ua.kpi.ipze.messaging.exception.TableDoesNotExistsException;
import ua.kpi.ipze.messaging.model.Inbox;
import ua.kpi.ipze.messaging.model.Outbox;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

public final class InboxDao {

    private static final Logger log = LoggerFactory.getLogger(InboxDao.class);

    private static final String TABLE_NAME = "inbox";
    private static final String INSERT_QUERY = "INSERT INTO %s.inbox (id, queue, message, received_date_time, handled_date_time) VALUES (?, ?, ?, ?, ?);";
    private static final String UPDATE_QUERY = "UPDATE %s.inbox SET handled_date_time = ? WHERE id = ?;";
    private static final String FETCH_QUERY = "SELECT id, queue, message, received_date_time, handled_date_time FROM %s.inbox where handled_date_time is null LIMIT ?;";
    private static final String EXISTS_QUERY = "SELECT COUNT(*) > 0 FROM %s.inbox WHERE id = ?;";
    private static final String DATA_RETENTION_FETCH_QUERY = "SELECT * FROM %s.inbox where handled_date_time < ? LIMIT ?;";
    private static final String DELETE_QUERY = "DELETE FROM %s.inbox WHERE id in (_ID_LIST_);";

    private static InboxDao instance;

    private final Datasource datasource;

    private InboxDao(Datasource datasource) {
        this.datasource = datasource;
    }

    public static InboxDao getInstance() {
        synchronized (InboxDao.class) {
            Datasource datasource = Datasource.getInstance();
            if (instance == null) {
                instance = new InboxDao(datasource);
            }
            instance.checkTableExists();
            return instance;
        }
    }

    public void create(Inbox inbox) {
        String sql = String.format(INSERT_QUERY, datasource.getSchema());
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, inbox.getId()); // UUID
            preparedStatement.setString(2, inbox.getQueue());
            preparedStatement.setString(3, inbox.getMessage());
            preparedStatement.setObject(4, inbox.getReceivedDateTime());
            preparedStatement.setObject(5, inbox.getHandledDateTime());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error creating Inbox", e);
            throw new DatabaseIntegrationException("Error creating Inbox", e);
        }
    }

    public boolean existsById(UUID id) {
        String sql = String.format(EXISTS_QUERY, datasource.getSchema());
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    return resultSet.getBoolean("exists");
                }
            } catch (SQLException e) {
                return false;
            }

        } catch (SQLException e) {
            log.error("Error checking Inbox existence", e);
            throw new DatabaseIntegrationException("Error checking Inbox existence", e);
        }

        return false;
    }

    public List<Inbox> fetch(int limit) {
        List<Inbox> entities = new ArrayList<>();
        String sql = String.format(FETCH_QUERY, datasource.getSchema());
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, limit);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Inbox inbox = new Inbox();
                    inbox.setId((UUID) resultSet.getObject("id"));
                    inbox.setQueue(resultSet.getString("queue"));
                    inbox.setMessage(resultSet.getString("message"));
                    inbox.setReceivedDateTime(resultSet.getObject("received_date_time", LocalDateTime.class));
                    inbox.setHandledDateTime(resultSet.getObject("handled_date_time", LocalDateTime.class));

                    entities.add(inbox);
                }
            } catch (SQLException e) {
                return entities;
            }

        } catch (SQLException e) {
            log.error("Error fetching Inbox messages", e);
            throw new DatabaseIntegrationException("Error fetching Inbox messages", e);
        }

        return entities;
    }

    public void updateEntities(List<Inbox> inboxList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < inboxList.size(); i++) {
            stringBuilder.append(String.format(UPDATE_QUERY, datasource.getSchema()));
        }

        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())) {
            int inboxIndex = 0;
            for (int i = 0; i < inboxList.size() * 2; i += 2) {
                if (inboxList.get(inboxIndex).getHandledDateTime() != null) {
                    preparedStatement.setObject(i + 1, inboxList.get(inboxIndex).getHandledDateTime());
                } else {
                    preparedStatement.setNull(i + 1, Types.TIMESTAMP_WITH_TIMEZONE);
                }

                preparedStatement.setObject(i + 2, inboxList.get(inboxIndex).getId());
                inboxIndex++;
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating Inbox messages", e);
            throw new DatabaseIntegrationException("Error fetching Inbox messages", e);
        }
    }

    public List<Inbox> fetchByDateBefore(int limit, LocalDateTime dateTime) {
        List<Inbox> entities = new ArrayList<>();
        String sql = String.format(DATA_RETENTION_FETCH_QUERY, datasource.getSchema());
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, dateTime);
            preparedStatement.setInt(2, limit);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Inbox inbox = new Inbox();
                    inbox.setId((UUID) resultSet.getObject("id"));
                    inbox.setQueue(resultSet.getString("queue"));
                    inbox.setMessage(resultSet.getString("message"));
                    inbox.setReceivedDateTime(resultSet.getObject("received_date_time", LocalDateTime.class));
                    inbox.setHandledDateTime(resultSet.getObject("handled_date_time", LocalDateTime.class));
                    entities.add(inbox);
                }
            } catch (Exception e) {
                return entities;
            }
        } catch (SQLException e) {
            throw new DatabaseIntegrationException("Error when fetch Outbox objects", e);
        }
        return entities;
    }

    public void checkTableExists() {
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword())) {
            if (!doesTableExist(connection)) {
                throw new TableDoesNotExistsException("Table " + TABLE_NAME + " does not exists");
            }
        } catch (SQLException e) {
            log.error("Error checking Inbox table", e);
            throw new TableDoesNotExistsException("Error checking Inbox table", e);
        }
    }

    private boolean doesTableExist(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, datasource.getSchema(), TABLE_NAME, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }

    public void deleteEntitiesBy(List<UUID> inboxIds) {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (UUID id : inboxIds) {
            stringJoiner.add(id.toString());
        }
        String query = DELETE_QUERY.replace("_ID_LIST_", stringJoiner.toString());
        String sql = String.format(query, datasource.getSchema());
        try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            int rowsAffected = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseIntegrationException("Error when delete Inbox objects", e);
        }
    }
}
