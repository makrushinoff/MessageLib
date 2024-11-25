package ua.kpi.ipze.messaging.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Inbox {

    private UUID id;
    private String queue;
    private String message;
    private LocalDateTime receivedDateTime;
    private LocalDateTime handledDateTime;

    public Inbox() {}

    public Inbox(UUID id, String queue, String message, LocalDateTime receivedDateTime, LocalDateTime handledDateTime) {
        this.id = id;
        this.queue = queue;
        this.message = message;
        this.receivedDateTime = receivedDateTime;
        this.handledDateTime = handledDateTime;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getReceivedDateTime() {
        return receivedDateTime;
    }

    public void setReceivedDateTime(LocalDateTime receivedDateTime) {
        this.receivedDateTime = receivedDateTime;
    }

    public LocalDateTime getHandledDateTime() {
        return handledDateTime;
    }

    public void setHandledDateTime(LocalDateTime handledDateTime) {
        this.handledDateTime = handledDateTime;
    }

}
