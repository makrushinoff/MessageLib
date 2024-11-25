package ua.kpi.ipze.messaging.exception;

public class DatabaseIntegrationException extends RuntimeException {

    public DatabaseIntegrationException(String message) {
        super(message);
    }

    public DatabaseIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

}
