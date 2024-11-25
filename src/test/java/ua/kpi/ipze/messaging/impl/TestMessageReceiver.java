package ua.kpi.ipze.messaging.impl;

import ua.kpi.ipze.messaging.api.MessageReceiver;

import java.text.MessageFormat;

public class TestMessageReceiver implements MessageReceiver {

    @Override
    public void receiveMessage(String message) {
        System.out.println(MessageFormat.format("Received message: {0}", message));
    }

}
