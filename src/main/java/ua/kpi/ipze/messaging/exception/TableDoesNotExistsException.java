package ua.kpi.ipze.messaging.exception;

public class TableDoesNotExistsException extends DatabaseIntegrationException {

    public TableDoesNotExistsException(String message) {
        super(message);
    }

    public TableDoesNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
