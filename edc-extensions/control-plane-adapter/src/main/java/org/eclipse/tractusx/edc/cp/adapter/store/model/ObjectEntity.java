package org.eclipse.tractusx.edc.cp.adapter.store.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ObjectEntity {
  private String id;
  private long createdAt;
  private String type;
  private String object;
}
