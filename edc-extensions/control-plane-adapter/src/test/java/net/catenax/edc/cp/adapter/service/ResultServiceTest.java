package net.catenax.edc.cp.adapter.service;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;
import net.catenax.edc.cp.adapter.dto.DataReferenceRetrievalDto;
import net.catenax.edc.cp.adapter.dto.ProcessData;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResultServiceTest {
  @Test
  public void pull_shouldReturnDataReferenceWhenMessageOccursFirst() throws InterruptedException {
    // given
    ResultService resultService = new ResultService();
    String endpointDataRefId = "456";
    DataReferenceRetrievalDto dto = getDto(endpointDataRefId);
    ProcessData processData;

    // when
    resultService.process(dto);
    processData = resultService.pull(dto.getTraceId(), 200, TimeUnit.MILLISECONDS);

    // then
    Assertions.assertEquals(endpointDataRefId, processData.getEndpointDataReference().getId());
  }

  @Test
  public void pull_shouldReturnDataReferenceWhenMessageOccursSecond() throws InterruptedException {
    // given
    ResultService resultService = new ResultService();
    String endpointDataRefId = "456";
    DataReferenceRetrievalDto dto = getDto(endpointDataRefId);
    ProcessData processData;

    // when
    processMessageWithDelay(resultService, dto);
    processData = resultService.pull(dto.getTraceId(), 1000, TimeUnit.MILLISECONDS);

    // then
    Assertions.assertEquals(endpointDataRefId, processData.getEndpointDataReference().getId());
  }

  private void processMessageWithDelay(ResultService resultService, DataReferenceRetrievalDto dto) {
    new Thread(
            () -> {
              sleep(400);
              resultService.process(dto);
            })
        .start();
  }

  @Test
  public void pull_shouldReturnNullOnTimeout() throws InterruptedException {
    // given
    ResultService resultService = new ResultService();

    // when
    ProcessData processData = resultService.pull("123", 500, TimeUnit.MILLISECONDS);

    // then
    Assertions.assertNull(processData);
  }

  @Test
  public void process_shouldThrowIllegalArgumentExceptionIfNoDataPayload() {
    // given
    ResultService resultService = new ResultService();
    DataReferenceRetrievalDto dto = new DataReferenceRetrievalDto(null);

    // when then
    try {
      resultService.process(dto);
      fail("Method should throw IllegalArgumentException");
    } catch (IllegalArgumentException ignored) {
    }
  }

  private DataReferenceRetrievalDto getDto(String endpointDataRefId) {
    DataReferenceRetrievalDto dto =
        new DataReferenceRetrievalDto(new ProcessData("123", "providerUrl"));
    dto.getPayload()
        .setEndpointDataReference(
            EndpointDataReference.Builder.newInstance()
                .id(endpointDataRefId)
                .endpoint("e")
                .authCode("c")
                .authKey("k")
                .build());
    return dto;
  }

  private void sleep(long milisec) {
    try {
      Thread.sleep(milisec);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
