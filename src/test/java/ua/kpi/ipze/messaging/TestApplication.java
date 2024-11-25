package ua.kpi.ipze.messaging;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ua.kpi.ipze.messaging.api.Message;
import ua.kpi.ipze.messaging.api.MessageReceiver;
import ua.kpi.ipze.messaging.api.SendMessageService;
import ua.kpi.ipze.messaging.api.StableMessagingConfigurer;
import ua.kpi.ipze.messaging.impl.DatabaseSetup;
import ua.kpi.ipze.messaging.impl.TestMessageReceiver;
import ua.kpi.ipze.messaging.impl.TestMessageSender;

import java.util.UUID;

public class TestApplication {

    private static final String TEST_QUEUE_NAME = "test-queue";

    @Test
    public void testApplication() throws JsonProcessingException {
        new DatabaseSetup().createTablesForTest();
        SendMessageService sendMessageService = new SendMessageService();
        MessageReceiver testMessageReceiver = new TestMessageReceiver();
        TestMessageSender testMessageSender = new TestMessageSender();

        StableMessagingConfigurer stableMessagingConfigurer = StableMessagingConfigurer.configure()
                .messageReceiver(TEST_QUEUE_NAME, testMessageReceiver)
                .messageFetchAmount(10)
                .senderFunction(testMessageSender)
                .frequency(1L)
                .build();

        sendMessageService.prepareMessageToSend("newmessage", TEST_QUEUE_NAME);
        testMessageReceiver.receiveMessage(new ObjectMapper().writeValueAsString(new Message(UUID.randomUUID(), "newmessage")));

        for(int i = 0; i < 20; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
