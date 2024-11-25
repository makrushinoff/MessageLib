package ua.kpi.ipze.messaging.api;

import java.util.Objects;
import java.util.UUID;

public class Message {

    private UUID id;
    private String value;

    public Message() {
    }

    public Message(UUID id, String value) {
        this.id = id;
        this.value = value;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id) && Objects.equals(value, message.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }
}
