package ua.kpi.ipze.messaging.impl;

import ua.kpi.ipze.messaging.api.Message;
import ua.kpi.ipze.messaging.api.MessageSender;

import java.text.MessageFormat;

public class TestMessageSender implements MessageSender {

    @Override
    public void sendMessageToQueue(Message message, String queueName) {
        System.out.println(MessageFormat.format("Message {0} is being sent to queue {1}", message, queueName));
    }

}
