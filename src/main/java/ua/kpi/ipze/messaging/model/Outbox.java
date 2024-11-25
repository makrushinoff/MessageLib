package ua.kpi.ipze.messaging.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Outbox {

    private UUID id;
    private String queue;
    private String message;
    private LocalDateTime creationDateTime;
    private LocalDateTime sentDateTime;

    public Outbox() {
    }

    public Outbox(UUID id, String queue, String message, LocalDateTime creationDateTime, LocalDateTime sentDateTime) {
        this.id = id;
        this.queue = queue;
        this.message = message;
        this.creationDateTime = creationDateTime;
        this.sentDateTime = sentDateTime;
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

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public LocalDateTime getSentDateTime() {
        return sentDateTime;
    }

    public void setSentDateTime(LocalDateTime sentDateTime) {
        this.sentDateTime = sentDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Outbox outbox = (Outbox) o;
        return Objects.equals(id, outbox.id) &&
                Objects.equals(queue, outbox.queue) &&
                Objects.equals(message, outbox.message) &&
                Objects.equals(creationDateTime, outbox.creationDateTime) &&
                Objects.equals(sentDateTime, outbox.sentDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, queue, message, creationDateTime, sentDateTime);
    }

    @Override
    public String toString() {
        return "Inbox{" +
                "id=" + id +
                ", queue='" + queue + '\'' +
                ", message='" + message + '\'' +
                ", creationDateTime=" + creationDateTime +
                ", sentDateTime=" + sentDateTime +
                '}';
    }

}
