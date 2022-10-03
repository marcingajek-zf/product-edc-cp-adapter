package net.catenax.edc.cp.adapter.process.datareference;

import net.catenax.edc.cp.adapter.dto.DataReferenceRetrievalDto;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;

public interface NotificationSyncService {
  EndpointDataReference exchangeDto(DataReferenceRetrievalDto dto, String contractAgreementId);

  DataReferenceRetrievalDto exchangeDataReference(
      EndpointDataReference dataReference, String contractAgreementId);

  void removeDataReference(String contractAgreementId);

  void removeDto(String key);
}
