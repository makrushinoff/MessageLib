package ua.kpi.ipze.messaging.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ua.kpi.ipze.messaging.service.DataRetentionService;
import ua.kpi.ipze.messaging.service.InboxProxyAspect;
import ua.kpi.ipze.messaging.service.InboxScheduledService;
import ua.kpi.ipze.messaging.service.OutboxScheduledService;
import ua.kpi.ipze.messaging.state.InboxStateHolder;

import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;

public class StableMessagingConfigurer {

    public record RetentionPolicy(Long time, TemporalUnit temporalUnit) {}

    private final OutboxScheduledService outboxScheduledService;
    private final InboxScheduledService inboxScheduledService;
    private final InboxProxyAspect inboxProxyAspect;
    private final DataRetentionService dataRetentionService;

    public StableMessagingConfigurer(int messageFetchAmount,
                                     MessageSender messageSender,
                                     Map<String, MessageReceiver> messageReceivers,
                                     Long frequency,
                                     RetentionPolicy retentionPolicy) {
        this.outboxScheduledService = new OutboxScheduledService(messageFetchAmount, messageSender, frequency);
        this.inboxScheduledService =  new InboxScheduledService(messageFetchAmount, messageReceivers, frequency);
        this.dataRetentionService = new DataRetentionService(retentionPolicy, messageFetchAmount);
        InboxStateHolder.instantiate(messageReceivers);
        this.inboxProxyAspect = new InboxProxyAspect();
    }

    public static StableMessagingConfigurerBuilder configure() {
        return new StableMessagingConfigurerBuilder();
    }

    public static class StableMessagingConfigurerBuilder {

        private int messageFetchAmount = 100;
        private MessageSender messageSender;
        private Map<String, MessageReceiver> messageReceivers = new HashMap<>();
        private Long frequency = 5L;
        private RetentionPolicy retentionPolicy;

        public StableMessagingConfigurerBuilder senderFunction(MessageSender messageSender) {
            this.messageSender = messageSender;
            return this;
        }

        public StableMessagingConfigurerBuilder messageFetchAmount(int messageFetchAmount) {
            this.messageFetchAmount = messageFetchAmount;
            return this;
        }

        public StableMessagingConfigurerBuilder messageReceiver(String queueName, MessageReceiver messageReceiver) {
            messageReceivers.put(queueName, messageReceiver);
            return this;
        }

        public StableMessagingConfigurerBuilder frequency(Long frequency) {
            this.frequency = frequency;
            return this;
        }

        public StableMessagingConfigurerBuilder retentionPolicy(RetentionPolicy retentionPolicy) {
            this.retentionPolicy = retentionPolicy;
            return this;
        }

        public StableMessagingConfigurer build() {
            return new StableMessagingConfigurer(messageFetchAmount, messageSender, messageReceivers, frequency, retentionPolicy);
        }

    }

    public static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

}
