package ua.kpi.ipze.messaging.api;

import ua.kpi.ipze.messaging.dao.OutboxDao;
import ua.kpi.ipze.messaging.model.Outbox;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Responsible to save messages, and queue them to be sent.
 * This class - a proxy between a system and message broker, and provides
 * Transactional Outbox functions in the library.
 * <p/>
 * It is needed to use instance of this class , and only it,
 * to send messages to message broker, when library functions are needed.
 */
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
