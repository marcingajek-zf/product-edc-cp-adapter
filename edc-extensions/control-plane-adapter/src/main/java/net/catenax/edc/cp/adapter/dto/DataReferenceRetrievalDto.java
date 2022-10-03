package net.catenax.edc.cp.adapter.dto;

import net.catenax.edc.cp.adapter.messaging.Message;

public class DataReferenceRetrievalDto extends Message<ProcessData> {
  public DataReferenceRetrievalDto(ProcessData payload) {
    super(payload);
  }
}
