package ua.kpi.ipze.messaging.exception;

public class PropertyFileInvalidException extends RuntimeException {

    public PropertyFileInvalidException(String message) {
        super(message);
    }

    public PropertyFileInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

}
