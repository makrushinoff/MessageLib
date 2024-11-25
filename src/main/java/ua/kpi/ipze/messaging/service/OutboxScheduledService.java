package ua.kpi.ipze.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.ipze.messaging.api.Message;
import ua.kpi.ipze.messaging.api.MessageSender;
import ua.kpi.ipze.messaging.api.StableMessagingConfigurer;
import ua.kpi.ipze.messaging.dao.OutboxDao;
import ua.kpi.ipze.messaging.model.Outbox;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OutboxScheduledService {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduledService.class);

    private final int messageFetchAmount;
    private final MessageSender messageSender;
    private final ObjectMapper objectMapper = StableMessagingConfigurer.objectMapper();
    private final ScheduledExecutorService outboxScheduler;
    private final OutboxDao outboxDao;

    public OutboxScheduledService(int messageFetchAmount, MessageSender messageSender, Long frequency) {
        this.messageFetchAmount = messageFetchAmount;
        this.messageSender = messageSender;

        outboxScheduler = Executors.newScheduledThreadPool(1);
        outboxScheduler.scheduleWithFixedDelay(this::startOutboxScan, frequency, frequency, TimeUnit.SECONDS);
        outboxDao = OutboxDao.getInstance();
    }

    private void startOutboxScan() {
        log.debug("Starting outbox scan");
        List<Outbox> outboxEntityList = outboxDao.fetch(messageFetchAmount);
        if (outboxEntityList.isEmpty()) {
            log.debug("Not found any not processed outbox messages");
            return;
        }
        log.debug("Found {} not processed outbox messages", outboxEntityList.size());
        List<Outbox> handledMessages = new ArrayList<>();
        for(Outbox outbox : outboxEntityList) {
            try {
                Message message = new Message(
                        outbox.getId(),
                        objectMapper.writeValueAsString(outbox.getMessage())
                );
                log.debug("Sending outbox message {} to queue {}", message, outbox.getQueue());
                messageSender.sendMessageToQueue(message, outbox.getQueue());
                outbox.setSentDateTime(LocalDateTime.now());
                handledMessages.add(outbox);
            } catch (Exception e) {
                log.error("Error when send outbox message", e);
                break;
            }
        }
        outboxDao.updateEntities(handledMessages);
    }

}
