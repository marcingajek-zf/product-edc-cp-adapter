package net.catenax.edc.cp.adapter.process.contractnotification;

import net.catenax.edc.cp.adapter.dto.DataReferenceRetrievalDto;

public interface NotificationSyncService {
  DataReferenceRetrievalDto exchangeConfirmedContract(
      String contractNegotiationId, String contractAgreementId);

  DataReferenceRetrievalDto exchangeDeclinedContract(String contractNegotiationId);

  DataReferenceRetrievalDto exchangeErrorContract(String contractNegotiationId);

  ContractInfo exchangeDto(DataReferenceRetrievalDto dto);

  void removeContractInfo(String key);

  void removeDto(String key);
}
