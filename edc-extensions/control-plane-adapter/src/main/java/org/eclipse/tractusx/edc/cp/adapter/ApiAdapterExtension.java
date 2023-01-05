/*
 * Copyright (c) 2022 ZF Friedrichshafen AG
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Contributors:
 * ZF Friedrichshafen AG - Initial API and Implementation
 *
 */

package org.eclipse.tractusx.edc.cp.adapter;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.connector.api.management.configuration.ManagementApiConfiguration;
import org.eclipse.edc.connector.contract.spi.negotiation.observe.ContractNegotiationListener;
import org.eclipse.edc.connector.contract.spi.negotiation.observe.ContractNegotiationObservable;
import org.eclipse.edc.connector.spi.catalog.CatalogService;
import org.eclipse.edc.connector.spi.contractagreement.ContractAgreementService;
import org.eclipse.edc.connector.spi.contractnegotiation.ContractNegotiationService;
import org.eclipse.edc.connector.spi.transferprocess.TransferProcessService;
import org.eclipse.edc.connector.transfer.spi.edr.EndpointDataReferenceReceiver;
import org.eclipse.edc.connector.transfer.spi.edr.EndpointDataReferenceReceiverRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.tractusx.edc.cp.adapter.messaging.*;
import org.eclipse.tractusx.edc.cp.adapter.process.contractnegotiation.CatalogCachedRetriever;
import org.eclipse.tractusx.edc.cp.adapter.process.contractnegotiation.CatalogRetriever;
import org.eclipse.tractusx.edc.cp.adapter.process.contractnegotiation.ContractAgreementRetriever;
import org.eclipse.tractusx.edc.cp.adapter.process.contractnegotiation.ContractNegotiationHandler;
import org.eclipse.tractusx.edc.cp.adapter.process.contractnotification.*;
import org.eclipse.tractusx.edc.cp.adapter.process.datareference.DataRefSyncService;
import org.eclipse.tractusx.edc.cp.adapter.process.datareference.DataRefNotificationSyncService;
import org.eclipse.tractusx.edc.cp.adapter.process.datareference.DataReferenceHandler;
import org.eclipse.tractusx.edc.cp.adapter.process.datareference.EndpointDataReferenceReceiverImpl;
import org.eclipse.tractusx.edc.cp.adapter.service.ErrorResultService;
import org.eclipse.tractusx.edc.cp.adapter.service.objectstore.ObjectStoreService;
import org.eclipse.tractusx.edc.cp.adapter.service.objectstore.ObjectStoreServiceInMemory;
import org.eclipse.tractusx.edc.cp.adapter.service.ResultService;
import org.eclipse.tractusx.edc.cp.adapter.service.objectstore.ObjectStoreServiceSql;
import org.eclipse.tractusx.edc.cp.adapter.store.SqlObjectStore;
import org.eclipse.tractusx.edc.cp.adapter.store.SqlQueueStore;
import org.eclipse.tractusx.edc.cp.adapter.store.schema.postgres.PostgresDialectObjectStoreStatements;
import org.eclipse.tractusx.edc.cp.adapter.store.schema.postgres.PostgresDialectQueueStatements;
import org.eclipse.tractusx.edc.cp.adapter.util.ExpiringMap;
import org.eclipse.tractusx.edc.cp.adapter.util.LockMap;

import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ApiAdapterExtension implements ServiceExtension {
  @Inject private Monitor monitor;
  @Inject private ContractNegotiationObservable negotiationObservable;
  @Inject private WebService webService;
  @Inject private ContractNegotiationService contractNegotiationService;
  @Inject private EndpointDataReferenceReceiverRegistry receiverRegistry;
  @Inject private ManagementApiConfiguration apiConfig;
  @Inject private TransferProcessService transferProcessService;
  @Inject private TransactionContext transactionContext;
  @Inject private CatalogService catalogService;
  @Inject private ContractAgreementService agreementService;
  @Inject private DataSourceRegistry dataSourceRegistry;
  @Inject private Clock clock;

  @Override
  public String name() {
    return "Control Plane Adapter Extension";
  }

  @Override
  public void initialize(ServiceExtensionContext context) {
    ApiAdapterConfig config = new ApiAdapterConfig(context);
    ListenerService listenerService = new ListenerService();

    MessageBus messageBus = createMessageBus(listenerService, context, config);
    ObjectStoreService storeService = getStoreService(context, config);

    ResultService resultService = new ResultService(config.getDefaultSyncRequestTimeout(), monitor);
    ErrorResultService errorResultService = new ErrorResultService(monitor, messageBus);

    ContractNotificationSyncService contractSyncService =
        new ContractSyncService(storeService, new LockMap());

    DataTransferInitializer dataTransferInitializer =
        new DataTransferInitializer(monitor, transferProcessService);

    ContractNotificationHandler contractNotificationHandler =
        new ContractNotificationHandler(
            monitor,
            messageBus,
            contractSyncService,
            contractNegotiationService,
            dataTransferInitializer);

    ContractNegotiationHandler contractNegotiationHandler =
        getContractNegotiationHandler(monitor, contractNegotiationService, messageBus);

    DataRefNotificationSyncService dataRefSyncService =
        new DataRefSyncService(storeService, new LockMap());
    DataReferenceHandler dataReferenceHandler =
        new DataReferenceHandler(monitor, messageBus, dataRefSyncService);

    listenerService.addListener(Channel.INITIAL, contractNegotiationHandler);
    listenerService.addListener(Channel.CONTRACT_CONFIRMATION, contractNotificationHandler);
    listenerService.addListener(Channel.DATA_REFERENCE, dataReferenceHandler);
    listenerService.addListener(Channel.RESULT, resultService);
    listenerService.addListener(Channel.DLQ, errorResultService);

    initHttpController(monitor, messageBus, resultService, config);
    initContractNegotiationListener(
        monitor, negotiationObservable, messageBus, contractSyncService, dataTransferInitializer);
    initDataReferenceReceiver(monitor, messageBus, dataRefSyncService);
  }

  private MessageBus createMessageBus(ListenerService listenerService, ServiceExtensionContext context, ApiAdapterConfig config) {
    // TODO config SQL + default inmemory
    if (false) {
      return new InMemoryMessageBus(
            monitor, listenerService, config.getInMemoryMessageBusThreadNumber());
    }

    SqlQueueStore sqlQueueStore = new SqlQueueStore(dataSourceRegistry, config.getDataSourceName(),
        transactionContext, context.getTypeManager().getMapper(), new PostgresDialectQueueStatements(), context.getConnectorId(),
        clock);
    SqlMessageBus messageBus = new SqlMessageBus(monitor, listenerService, sqlQueueStore,
        config.getSqlMessageBusThreadNumber(),
        config.getSqlMessageBusMaxDelivery());
    initMessageBus(messageBus, config);

    return messageBus;
  }

  private ObjectStoreService getStoreService(ServiceExtensionContext context, ApiAdapterConfig config) {
    // TODO config SQL + default inmemory
    if (false) {
      return new ObjectStoreServiceInMemory(context.getTypeManager().getMapper());
    }

    ObjectMapper mapper = context.getTypeManager().getMapper();

    SqlObjectStore objectStore = new SqlObjectStore(
        dataSourceRegistry,
        config.getDataSourceName(),
        transactionContext,
        mapper,
        new PostgresDialectObjectStoreStatements());

    return new ObjectStoreServiceSql(mapper, objectStore);
  }

  private void initMessageBus(SqlMessageBus messageBus, ApiAdapterConfig config) {
    final int poolSize = 1;
    final int initialDelay = 5;
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(poolSize);
    scheduler.scheduleAtFixedRate(() -> messageBus.deliverMessages(config.getSqlMessageBusMaxDelivery()),
        initialDelay, config.getSqlMessageBusDeliveryInterval(), TimeUnit.SECONDS);
  }

  private void initHttpController(
      Monitor monitor,
      MessageBus messageBus,
      ResultService resultService,
      ApiAdapterConfig config) {
    webService.registerResource(
        apiConfig.getContextAlias(),
        new HttpController(monitor, resultService, messageBus, config));
  }

  private ContractNegotiationHandler getContractNegotiationHandler(
      Monitor monitor,
      ContractNegotiationService contractNegotiationService,
      MessageBus messageBus) {
    return new ContractNegotiationHandler(
        monitor,
        messageBus,
        contractNegotiationService,
        new CatalogCachedRetriever(new CatalogRetriever(catalogService), new ExpiringMap<>()),
        new ContractAgreementRetriever(monitor, agreementService));
  }

  private void initDataReferenceReceiver(
      Monitor monitor,
      MessageBus messageBus,
      DataRefNotificationSyncService dataRefSyncService) {
    EndpointDataReferenceReceiver dataReferenceReceiver =
        new EndpointDataReferenceReceiverImpl(monitor, messageBus, dataRefSyncService);
    receiverRegistry.registerReceiver(dataReferenceReceiver);
  }

  private void initContractNegotiationListener(
      Monitor monitor,
      ContractNegotiationObservable negotiationObservable,
      MessageBus messageBus,
      ContractNotificationSyncService contractSyncService,
      DataTransferInitializer dataTransferInitializer) {
    ContractNegotiationListener contractNegotiationListener =
        new ContractNegotiationListenerImpl(
            monitor, messageBus, contractSyncService, dataTransferInitializer);
    if (nonNull(negotiationObservable)) {
      negotiationObservable.registerListener(contractNegotiationListener);
    }
  }
}
