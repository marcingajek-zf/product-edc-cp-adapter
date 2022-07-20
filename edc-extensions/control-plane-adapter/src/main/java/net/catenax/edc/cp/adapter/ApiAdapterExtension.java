package net.catenax.edc.cp.adapter;

import net.catenax.edc.cp.adapter.messaging.Channel;
import net.catenax.edc.cp.adapter.messaging.InMemoryMessageService;
import net.catenax.edc.cp.adapter.process.contractconfirmation.ContractConfirmationHandler;
import net.catenax.edc.cp.adapter.process.contractconfirmation.InMemoryDataStore;
import net.catenax.edc.cp.adapter.process.datareference.DataReferenceHandler;
import net.catenax.edc.cp.adapter.process.contractnegotiation.ContractNegotiationHandler;
import net.catenax.edc.cp.adapter.service.ResultService;
import org.eclipse.dataspaceconnector.api.datamanagement.catalog.service.CatalogService;
import org.eclipse.dataspaceconnector.api.datamanagement.catalog.service.CatalogServiceImpl;
import org.eclipse.dataspaceconnector.api.datamanagement.contractnegotiation.service.ContractNegotiationService;
import org.eclipse.dataspaceconnector.api.datamanagement.contractnegotiation.service.ContractNegotiationServiceImpl;
import org.eclipse.dataspaceconnector.api.datamanagement.transferprocess.service.TransferProcessService;
import org.eclipse.dataspaceconnector.api.datamanagement.transferprocess.service.TransferProcessServiceImpl;
import org.eclipse.dataspaceconnector.spi.WebService;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.observe.ContractNegotiationObservable;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transaction.NoopTransactionContext;
import org.eclipse.dataspaceconnector.spi.transaction.TransactionContext;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.edr.EndpointDataReferenceReceiverRegistry;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;

import static java.util.Optional.ofNullable;

public class ApiAdapterExtension implements ServiceExtension {
    @Inject
    private WebService webService;
    @Inject
    private ContractNegotiationStore store;
    @Inject
    private ConsumerContractNegotiationManager manager;
    @Inject
    private TransactionContext transactionContext;
    @Inject
    private RemoteMessageDispatcherRegistry dispatcher;
    @Inject
    private TransferProcessStore transferProcessStore;
    @Inject
    private TransferProcessManager transferProcessManager;
    @Inject
    private EndpointDataReferenceReceiverRegistry receiverRegistry;


    @Override
    public String name() {
        return "Control Plane Adapter Extension";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        /** external dependencies * */
        Monitor monitor = context.getMonitor();
        ContractNegotiationObservable negotiationObservable = context.getService(ContractNegotiationObservable.class, false);


        /** internal dependencies * */
        ContractNegotiationService contractNegotiationService = new ContractNegotiationServiceImpl(store, manager, getTransactionContext(monitor));
        InMemoryMessageService messageService = new InMemoryMessageService(monitor);
        ResultService resultService = new ResultService(monitor);
        messageService.addListener(Channel.RESULT, resultService);


        initHttpController(messageService, resultService);
        initContractNegotiationHandler(monitor, contractNegotiationService, messageService);
        initContractConfirmationHandler(monitor, negotiationObservable, contractNegotiationService, messageService);
        initDataReferenceHandler(monitor, messageService);
    }

    private void initHttpController(InMemoryMessageService messageService, ResultService resultService) {
        HttpController controller = new HttpController(resultService, messageService);
        webService.registerResource(controller);
    }

    private void initContractNegotiationHandler(Monitor monitor, ContractNegotiationService contractNegotiationService, InMemoryMessageService messageService) {
        CatalogService catalogService = new CatalogServiceImpl(dispatcher);
        ContractNegotiationHandler contractNegotiationHandler = new ContractNegotiationHandler(monitor, messageService, contractNegotiationService, catalogService);
        messageService.addListener(Channel.INITIAL, contractNegotiationHandler);
    }

    private void initContractConfirmationHandler(Monitor monitor,
                                                ContractNegotiationObservable negotiationObservable,
                                                ContractNegotiationService contractNegotiationService,
                                                InMemoryMessageService messageService) {
        TransferProcessService transferProcessService = new TransferProcessServiceImpl(
                transferProcessStore,
                transferProcessManager,
                getTransactionContext(monitor));

        ContractConfirmationHandler contractConfirmationHandler = new ContractConfirmationHandler(
                monitor,
                messageService,
                new InMemoryDataStore(),
                contractNegotiationService,
                transferProcessService);

        messageService.addListener(Channel.CONTRACT_CONFIRMATION, contractConfirmationHandler);
        if (negotiationObservable != null) {
            negotiationObservable.registerListener(contractConfirmationHandler);
        }
    }

    private void initDataReferenceHandler(Monitor monitor, InMemoryMessageService messageService) {
        DataReferenceHandler dataReferenceHandler = new DataReferenceHandler(monitor, messageService, new net.catenax.edc.cp.adapter.process.datareference.InMemoryDataStore());
        messageService.addListener(Channel.DATA_REFERENCE, dataReferenceHandler);
        receiverRegistry.registerReceiver(dataReferenceHandler);
    }

    private TransactionContext getTransactionContext(Monitor monitor) {
        return ofNullable(transactionContext)
                .orElseGet(() -> {
                    monitor.warning("No TransactionContext registered, a no-op implementation will be used, not suitable for production environments");
                    return new NoopTransactionContext();
                });
    }
}
