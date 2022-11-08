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

package net.catenax.edc.cp.adapter.process.contractnotification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.Response;
import net.catenax.edc.cp.adapter.dto.DataReferenceRetrievalDto;
import net.catenax.edc.cp.adapter.dto.ProcessData;
import net.catenax.edc.cp.adapter.messaging.Channel;
import net.catenax.edc.cp.adapter.messaging.Message;
import net.catenax.edc.cp.adapter.messaging.MessageService;
import net.catenax.edc.cp.adapter.process.contractdatastore.ContractDataStore;
import org.eclipse.dataspaceconnector.api.datamanagement.contractnegotiation.service.ContractNegotiationService;
import org.eclipse.dataspaceconnector.api.datamanagement.transferprocess.service.TransferProcessService;
import org.eclipse.dataspaceconnector.api.result.ServiceResult;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.agreement.ContractAgreement;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiationStates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ContractNotificationHandlerTest {
  @Mock Monitor monitor;
  @Mock MessageService messageService;
  @Mock ContractNegotiationService contractNegotiationService;
  @Mock ContractNotificationSyncService notificationSyncService;
  @Mock ContractDataStore contractDataStore;
  @Mock TransferProcessService transferProcessService;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void process_shouldNotInitiateTransferWhenNoContractNotification() {
    // given
    when(transferProcessService.initiateTransfer(any())).thenReturn(ServiceResult.success(null));
    ContractNotificationHandler contractNotificationHandler =
        new ContractNotificationHandler(
            monitor,
            messageService,
            notificationSyncService,
            contractNegotiationService,
            transferProcessService,
            contractDataStore);
    DataReferenceRetrievalDto dto = new DataReferenceRetrievalDto(getProcessData(), 3);

    // when
    contractNotificationHandler.process(dto);

    // then
    verify(notificationSyncService, times(1)).exchangeDto(any());
    verify(transferProcessService, times(0)).initiateTransfer(any());
    verify(messageService, times(0)).send(eq(Channel.DATA_REFERENCE), any(Message.class));
  }

  @Test
  public void process_shouldInitiateTransferWhenContractConfirmedFromCache() {
    // given
    when(transferProcessService.initiateTransfer(any())).thenReturn(ServiceResult.success(null));
    ContractNotificationHandler contractNotificationHandler =
        new ContractNotificationHandler(
            monitor,
            messageService,
            notificationSyncService,
            contractNegotiationService,
            transferProcessService,
            contractDataStore);
    DataReferenceRetrievalDto dto = new DataReferenceRetrievalDto(getProcessData(), 3);
    dto.getPayload().setContractConfirmed(true);

    // when
    contractNotificationHandler.process(dto);

    // then
    verify(transferProcessService, times(1)).initiateTransfer(any());
    verify(messageService, times(1)).send(eq(Channel.DATA_REFERENCE), any(Message.class));
  }

  @Test
  public void process_shouldInitiateTransferWhenContractAlreadyConfirmedAtProvider() {
    // given
    when(contractNegotiationService.findbyId(any())).thenReturn(getConfirmedContractNegotiation());
    when(transferProcessService.initiateTransfer(any())).thenReturn(ServiceResult.success(null));
    ContractNotificationHandler contractNotificationHandler =
        new ContractNotificationHandler(
            monitor,
            messageService,
            notificationSyncService,
            contractNegotiationService,
            transferProcessService,
            contractDataStore);
    DataReferenceRetrievalDto dto = new DataReferenceRetrievalDto(getProcessData(), 3);

    // when
    contractNotificationHandler.process(dto);

    // then
    verify(notificationSyncService, times(0)).exchangeDto(any());
    verify(transferProcessService, times(1)).initiateTransfer(any());
    verify(messageService, times(1)).send(eq(Channel.DATA_REFERENCE), any(Message.class));
  }

  @Test
  public void process_shouldInitiateTransferWhenContractConfirmedByNotification() {
    // given
    when(notificationSyncService.exchangeDto(any()))
        .thenReturn(
            new ContractInfo("confirmedContractAgreementId", ContractInfo.ContractState.CONFIRMED));
    when(transferProcessService.initiateTransfer(any())).thenReturn(ServiceResult.success(null));
    ContractNotificationHandler contractNotificationHandler =
        new ContractNotificationHandler(
            monitor,
            messageService,
            notificationSyncService,
            contractNegotiationService,
            transferProcessService,
            contractDataStore);
    DataReferenceRetrievalDto dto = new DataReferenceRetrievalDto(getProcessData(), 3);

    // when
    contractNotificationHandler.process(dto);

    // then
    verify(transferProcessService, times(1)).initiateTransfer(any());
    verify(messageService, times(1)).send(eq(Channel.DATA_REFERENCE), any(Message.class));
    verify(notificationSyncService, times(1)).removeContractInfo(any());
  }

  @Test
  public void preConfirmed_shouldNotInitiateTransferIfMessageNotAvailable() {
    // given
    ContractNotificationHandler contractNotificationHandler =
        new ContractNotificationHandler(
            monitor,
            messageService,
            notificationSyncService,
            contractNegotiationService,
            transferProcessService,
            contractDataStore);
    ContractNegotiation contractNegotiation = getConfirmedContractNegotiation();

    // when
    contractNotificationHandler.confirmed(contractNegotiation);

    // then
    verify(notificationSyncService, times(1)).exchangeConfirmedContract(any(), any());
    verify(transferProcessService, times(0)).initiateTransfer(any());
    verify(messageService, times(0)).send(eq(Channel.DATA_REFERENCE), any(Message.class));
  }

  @Test
  public void preConfirmed_shouldInitiateTransferIfMessageIsAvailable() {
    // given
    when(notificationSyncService.exchangeConfirmedContract(any(), any()))
        .thenReturn(new DataReferenceRetrievalDto(getProcessData(), 3));
    when(transferProcessService.initiateTransfer(any())).thenReturn(ServiceResult.success(null));
    ContractNotificationHandler contractNotificationHandler =
        new ContractNotificationHandler(
            monitor,
            messageService,
            notificationSyncService,
            contractNegotiationService,
            transferProcessService,
            contractDataStore);
    ContractNegotiation contractNegotiation = getConfirmedContractNegotiation();

    // when
    contractNotificationHandler.confirmed(contractNegotiation);

    // then
    verify(transferProcessService, times(1)).initiateTransfer(any());
    verify(messageService, times(1)).send(eq(Channel.DATA_REFERENCE), any(Message.class));
  }

  @Test
  public void preDeclined_shouldSendErrorResultIfMessageIsAvailable() {
    // given
    when(notificationSyncService.exchangeDeclinedContract(any()))
        .thenReturn(new DataReferenceRetrievalDto(getProcessData(), 3));
    ContractNotificationHandler contractNotificationHandler =
        new ContractNotificationHandler(
            monitor,
            messageService,
            notificationSyncService,
            contractNegotiationService,
            transferProcessService,
            contractDataStore);
    ContractNegotiation contractNegotiation = getConfirmedContractNegotiation();

    // when
    contractNotificationHandler.declined(contractNegotiation);

    // then
    ArgumentCaptor<DataReferenceRetrievalDto> messageArg =
        ArgumentCaptor.forClass(DataReferenceRetrievalDto.class);
    verify(messageService, times(1)).send(eq(Channel.RESULT), messageArg.capture());
    Assertions.assertEquals(
        Response.Status.BAD_GATEWAY, messageArg.getValue().getPayload().getErrorStatus());
  }

  @Test
  public void preError_shouldSendErrorResultIfMessageIsAvailable() {
    // given
    when(notificationSyncService.exchangeErrorContract(any()))
        .thenReturn(new DataReferenceRetrievalDto(getProcessData(), 3));
    ContractNotificationHandler contractNotificationHandler =
        new ContractNotificationHandler(
            monitor,
            messageService,
            notificationSyncService,
            contractNegotiationService,
            transferProcessService,
            contractDataStore);
    ContractNegotiation contractNegotiation = getConfirmedContractNegotiation();

    // when
    contractNotificationHandler.failed(contractNegotiation);

    // then
    ArgumentCaptor<DataReferenceRetrievalDto> messageArg =
        ArgumentCaptor.forClass(DataReferenceRetrievalDto.class);
    verify(messageService, times(1)).send(eq(Channel.RESULT), messageArg.capture());
    Assertions.assertEquals(
        Response.Status.BAD_GATEWAY, messageArg.getValue().getPayload().getErrorStatus());
  }

  private ContractNegotiation getConfirmedContractNegotiation() {
    return ContractNegotiation.Builder.newInstance()
        .state(ContractNegotiationStates.CONFIRMED.code())
        .id("contractNegotiationId")
        .counterPartyId("counterPartyId")
        .counterPartyAddress("counterPartyAddress")
        .protocol("protocol")
        .contractAgreement(
            ContractAgreement.Builder.newInstance()
                .id("contractAgreementId")
                .providerAgentId("providerAgentId")
                .consumerAgentId("consumerAgentId")
                .assetId("assetId")
                .policy(Policy.Builder.newInstance().build())
                .build())
        .build();
  }

  private ProcessData getProcessData() {
    return ProcessData.builder().assetId("assetId").provider("provider").build();
  }
}