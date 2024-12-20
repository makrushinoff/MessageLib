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

/**
 * Responsible for configuration of library functions. Use {@link StableMessagingConfigurer#configure()}
 * static method to create {@link StableMessagingConfigurerBuilder} instance, which has handy interface for the configuration.
 */
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

        /**
         * Defines how many messages at once is needed to handle by scheduled jobs
         *
         * @param messageFetchAmount amount of messages which will be fetched from DB and handled
         */
        public StableMessagingConfigurerBuilder messageFetchAmount(int messageFetchAmount) {
            this.messageFetchAmount = messageFetchAmount;
            return this;
        }

        /**
         * Registers listeners for messages for a specific queue
         *
         * @param queueName queue name to receive message
         * @param messageReceiver object, which has to handle messages from specified queue
         */
        public StableMessagingConfigurerBuilder messageReceiver(String queueName, MessageReceiver messageReceiver) {
            messageReceivers.put(queueName, messageReceiver);
            return this;
        }

        /**
         * Defines has often messages will be sent to message broker, and how often messages will be handled, when they are received.
         *
         * @param frequency interval in seconds between handling of scheduled jobs.
         */
        public StableMessagingConfigurerBuilder frequency(Long frequency) {
            this.frequency = frequency;
            return this;
        }

        /**
         * Defines how much time stored messages in <i>outbox</i> and <i>inbox</i> tables is needed to save.
         *
         * @param retentionPolicy time interval with time units to specify
         */
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
