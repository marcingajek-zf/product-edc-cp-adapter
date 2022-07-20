package net.catenax.edc.cp.adapter.process.datareference;

import lombok.RequiredArgsConstructor;
import net.catenax.edc.cp.adapter.messaging.Channel;
import net.catenax.edc.cp.adapter.messaging.Listener;
import net.catenax.edc.cp.adapter.messaging.Message;
import net.catenax.edc.cp.adapter.messaging.MessageService;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.eclipse.dataspaceconnector.spi.transfer.edr.EndpointDataReferenceReceiver;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class DataReferenceHandler implements Listener, EndpointDataReferenceReceiver /*TransferProcessListener*/ {
    private final Monitor monitor;
    private final MessageService messageService;
    private final DataStore dataStore;

    @Override
    public void process(Message message) {
        String contractAgreementId = message.getPayload().getContractAgreementId();
        monitor.info("DataReferenceHandler: message received / " + message.getTraceId());

        EndpointDataReference dataReference = dataStore.getDataReference(contractAgreementId);
        if (Objects.isNull(dataReference)) {
            dataStore.storeMessage(message);
            return;
        }

        message.getPayload().setEndpointDataReference(dataReference);
        messageService.send(Channel.RESULT, message);
        dataStore.removeDataReference(contractAgreementId);
        monitor.info("DataReferenceHandler: message processed / " + message.getTraceId());
    }

    @Override
    public CompletableFuture<Result<Void>> send(@NotNull EndpointDataReference dataReference) {
        String contractAgreementId = dataReference.getProperties().get("cid");
        monitor.info("DataReferenceHandler: dataReference received, contractAgreement: " + contractAgreementId);

        Message message = dataStore.getMessage(contractAgreementId);
        if (Objects.isNull(message)) {
            dataStore.storeDataReference(contractAgreementId, dataReference);
            return CompletableFuture.completedFuture(Result.success());
        }
        message.getPayload().setEndpointDataReference(dataReference);
        messageService.send(Channel.RESULT, message);
        dataStore.removeMessage(contractAgreementId);

        monitor.info("DataReferenceHandler: dataReference processed / " + message.getTraceId());
        return CompletableFuture.completedFuture(Result.success());
    }
}
