package ua.kpi.ipze.messaging.service;

import ua.kpi.ipze.messaging.api.StableMessagingConfigurer;
import ua.kpi.ipze.messaging.dao.InboxDao;
import ua.kpi.ipze.messaging.dao.OutboxDao;
import ua.kpi.ipze.messaging.model.Inbox;
import ua.kpi.ipze.messaging.model.Outbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataRetentionService {

    private final OutboxDao outboxDao;
    private final InboxDao inboxDao;
    private final ScheduledExecutorService dataRetentionExecutor;

    private final StableMessagingConfigurer.RetentionPolicy retentionPolicy;
    private final Integer messageFetchAmount;

    public DataRetentionService(StableMessagingConfigurer.RetentionPolicy retentionPolicy, Integer messageFetchAmount) {
        this.retentionPolicy = retentionPolicy;
        this.messageFetchAmount = messageFetchAmount;
        this.outboxDao = OutboxDao.getInstance();
        this.inboxDao = InboxDao.getInstance();

        dataRetentionExecutor = Executors.newScheduledThreadPool(1);
        dataRetentionExecutor.scheduleWithFixedDelay(this::removeOldMessages, 1L, 1L, TimeUnit.HOURS);
    }

    private void removeOldMessages() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.minus(retentionPolicy.time(), retentionPolicy.temporalUnit());
        List<Outbox> outboxes = outboxDao.fetchByDateBefore(messageFetchAmount, expirationDate);
        List<UUID> outboxIds = outboxes.stream()
                .map(Outbox::getId)
                .toList();
        outboxDao.deleteEntitiesBy(outboxIds);

        List<Inbox> inboxes = inboxDao.fetchByDateBefore(messageFetchAmount, expirationDate);
        List<UUID> inboxIds = inboxes.stream()
                .map(Inbox::getId)
                .toList();
        inboxDao.deleteEntitiesBy(inboxIds);

    }

}
