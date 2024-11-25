package ua.kpi.ipze.messaging.state;

import ua.kpi.ipze.messaging.api.MessageReceiver;

import java.util.Map;

public final class InboxStateHolder {

    private static InboxStateHolder instance;
    private static Map<String, MessageReceiver> receivers;

    private InboxStateHolder(Map<String, MessageReceiver> receivers) {
        this.receivers = receivers;
    }

    public static void instantiate(Map<String, MessageReceiver> receivers) {
        if (instance == null) {
            instance = new InboxStateHolder(receivers);
        }
    }

    public static String getQueueNameForReceiver(String receiverObject) {
        return receivers.entrySet().stream()
                .filter(entry -> entry.getValue().toString().contentEquals(receiverObject))
                .findFirst()
                .get()
                .getKey();
    }

}
