package net.catenax.edc.cp.adapter.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import net.catenax.edc.cp.adapter.exception.ConfigurationException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

// TODO add logs

@RequiredArgsConstructor
public class InMemoryMessageService implements MessageService {
    private final Monitor monitor;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10); // todo move number to config
    /** only single listener for a message at the moment **/
    private final Map<Channel, Listener> listeners = new HashMap<>();

    @Override
    public void send(Channel name, Message message) {
        if (Objects.isNull(message)) {
            monitor.warning("Message is empty, channel: " + name);
        } else {
            monitor.info("Message " + message.getTraceId() + " is sent to the channel: " + name);
            executorService.submit(() -> run(name, message));
        }
    }

    public void addListener(Channel name, Listener listener) {
        listeners.put(name, listener);
    }

    public void removeListener(Channel name) {
        listeners.remove(name);
    }

    /**
     * Returns true if message is processed by the MessageService. *
     */
    protected boolean run(Channel name, Message message) {
        try {
            getListener(name).process(message);
            message.markAsProcessed();
            return true;
        } catch (Exception e) {
            monitor.warning("Message processing error with id: " + message.getTraceId(), e);
            if (!message.canRetry()) {
                monitor.warning("Message " + message.getTraceId() + " reached retry limit!");
                // TODO move to DLQ
                return true;
            }
            long delayTime = message.markAsUnprocessed();
            executorService.schedule(() -> run(name, message), delayTime, TimeUnit.MILLISECONDS);
            return false;
        }
    }

    private Listener getListener(Channel name) {
        Listener listener = listeners.get(name);
        if (Objects.isNull(listener)) {
            monitor.severe("No listener found for channel: " + name);
            throw new ConfigurationException();
        }
        return listener;
    }
}
