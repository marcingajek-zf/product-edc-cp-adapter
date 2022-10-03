package net.catenax.edc.cp.adapter.process.datareference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import net.catenax.edc.cp.adapter.dto.DataReferenceRetrievalDto;
import net.catenax.edc.cp.adapter.dto.ProcessData;
import net.catenax.edc.cp.adapter.messaging.Channel;
import net.catenax.edc.cp.adapter.messaging.Message;
import net.catenax.edc.cp.adapter.messaging.MessageService;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataReferenceHandlerTest {
  @Mock Monitor monitor;
  @Mock MessageService messageService;
  @Mock NotificationSyncService notificationSyncService;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void process_shouldNotSendResultWhenDataReferenceNotAvailable() {
    // given
    DataReferenceHandler dataReferenceHandler =
        new DataReferenceHandler(monitor, messageService, notificationSyncService);
    DataReferenceRetrievalDto dto =
        new DataReferenceRetrievalDto(new ProcessData("assetId", "providerUrl"));

    // when
    dataReferenceHandler.process(dto);

    // then
    verify(notificationSyncService, times(1)).exchangeDto(eq(dto), any());
    verify(messageService, times(0)).send(eq(Channel.RESULT), any(Message.class));
  }

  @Test
  public void process_shouldSendResultWhenDataReferenceIsAvailable() {
    // given
    when(notificationSyncService.exchangeDto(any(), any())).thenReturn(getEndpointDataReference());
    DataReferenceHandler dataReferenceHandler =
        new DataReferenceHandler(monitor, messageService, notificationSyncService);
    DataReferenceRetrievalDto dto =
        new DataReferenceRetrievalDto(new ProcessData("assetId", "providerUrl"));

    // when
    dataReferenceHandler.process(dto);

    // then
    verify(messageService, times(1)).send(eq(Channel.RESULT), any(Message.class));
    verify(notificationSyncService, times(1)).removeDataReference(any());
  }

  @Test
  public void send_shouldNotSendResultWhenMessageNotAvailable() {
    // given
    DataReferenceHandler dataReferenceHandler =
        new DataReferenceHandler(monitor, messageService, notificationSyncService);

    // when
    dataReferenceHandler.send(getEndpointDataReference());

    // then
    verify(messageService, times(0)).send(eq(Channel.RESULT), any(Message.class));
  }

  @Test
  public void send_shouldSendResultWhenMessageIsAvailable() {
    // given
    DataReferenceRetrievalDto dto =
        new DataReferenceRetrievalDto(new ProcessData("assetId", "providerUrl"));
    when(notificationSyncService.exchangeDataReference(any(), any())).thenReturn(dto);
    DataReferenceHandler dataReferenceHandler =
        new DataReferenceHandler(monitor, messageService, notificationSyncService);

    // when
    dataReferenceHandler.send(getEndpointDataReference());

    // then
    verify(messageService, times(1)).send(eq(Channel.RESULT), any(Message.class));
    verify(notificationSyncService, times(1)).removeDto(any());
  }

  private EndpointDataReference getEndpointDataReference() {
    return EndpointDataReference.Builder.newInstance()
        .endpoint("endpoint")
        .authCode("authCode")
        .authKey("authKey")
        .build();
  }
}
