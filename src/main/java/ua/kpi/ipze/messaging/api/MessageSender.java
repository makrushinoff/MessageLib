package ua.kpi.ipze.messaging.api;

public interface MessageSender {

    void sendMessageToQueue(Message message, String queueName);

}
