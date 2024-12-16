package ua.kpi.ipze.messaging.api;

/**
 * Responsible for sending messages to desired message broker.
 * Objects of this class will be used as a tool by library, to send messages to desired message broker.
 * <p/>
 * <b>WARNING:</b> use {@link SendMessageService} object as a service to send messages,
 * otherwise library will not perform as expected
 *
 */
public interface MessageSender {

    void sendMessageToQueue(Message message, String queueName);

}
