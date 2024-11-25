package ua.kpi.ipze.messaging.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.ipze.messaging.api.MessageReceiver;
import ua.kpi.ipze.messaging.dao.InboxDao;
import ua.kpi.ipze.messaging.model.Inbox;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InboxScheduledService {

    private static final Logger log = LoggerFactory.getLogger(InboxScheduledService.class);

    private final Integer messageFetchAmount;
    private final ScheduledExecutorService inboxScheduler;
    private final InboxDao inboxDao;
    private final Map<String, MessageReceiver> messageReceivers;

    public InboxScheduledService(Integer messageFetchAmount, Map<String, MessageReceiver> messageReceivers, Long frequency) {
        this.messageFetchAmount = messageFetchAmount;
        this.messageReceivers = messageReceivers;

        inboxScheduler = Executors.newScheduledThreadPool(1);
        inboxScheduler.scheduleWithFixedDelay(this::startInboxScan, frequency, frequency, TimeUnit.SECONDS);
        inboxDao = InboxDao.getInstance();
    }

    void startInboxScan() {
        log.debug("Starting inbox scan");
        List<Inbox> inboxList = inboxDao.fetch(messageFetchAmount);
        if (inboxList.isEmpty()) {
            log.debug("Not found any not processed inbox messages");
            return;
        }
        log.debug("Found {} not processed inbox messages", inboxList.size());
        List<Inbox> handledMessages = new ArrayList<>();
        for (Inbox inbox : inboxList) {
            try {
                String message = inbox.getMessage();
                if(message.startsWith("\"") && message.endsWith("\"")) {
                    message = message.substring(1, message.length() - 1);
                }
                message = message.replace("\\", "");
                log.debug("Start to handle inbox message {}", message);
                messageReceivers.get(inbox.getQueue())
                        .receiveMessage(message);
                inbox.setHandledDateTime(LocalDateTime.now());
                handledMessages.add(inbox);
            } catch (Exception e) {
                log.error("Error when send inbox message", e);
                break;
            }
        }
        inboxDao.updateEntities(handledMessages);
    }

}
