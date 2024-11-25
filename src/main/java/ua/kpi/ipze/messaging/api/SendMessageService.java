package ua.kpi.ipze.messaging.api;

import ua.kpi.ipze.messaging.dao.OutboxDao;
import ua.kpi.ipze.messaging.model.Outbox;

import java.time.LocalDateTime;
import java.util.UUID;

public class SendMessageService {

    private final OutboxDao outboxDao = OutboxDao.getInstance();

    public void prepareMessageToSend(String message, String queue) {
        Outbox outbox = new Outbox();
        outbox.setMessage(message);
        outbox.setQueue(queue);
        outbox.setCreationDateTime(LocalDateTime.now());
        outbox.setId(UUID.randomUUID());
        outboxDao.create(outbox);
    }

}
