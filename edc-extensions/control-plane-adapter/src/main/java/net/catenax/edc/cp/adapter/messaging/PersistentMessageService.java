package net.catenax.edc.cp.adapter.messaging;

import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

public class PersistentMessageService extends InMemoryMessageService {
    public PersistentMessageService(Monitor monitor) {
        super(monitor);
        // TODO init scheduler from DB (use send method?)
    }

    @Override
    public void send(Channel name, Message message) {
        // TODO save to db
        super.send(name, message);
    }

    @Override
    protected boolean run(Channel name, Message message) {
        boolean isProcessed = super.run(name, message);
        if (isProcessed) {
            // TODO remove from DB
        }
        return isProcessed;
    }
}
