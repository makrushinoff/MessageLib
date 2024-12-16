package ua.kpi.ipze.messaging.api;

/**
 * Responsible for receiving messages from any source.
 *
 * Each listener in the system, if it has to be used by library, needs to implement this interface.
 */
public interface MessageReceiver {

    void receiveMessage(String message);

}
