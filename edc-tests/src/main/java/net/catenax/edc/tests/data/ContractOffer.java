package net.catenax.edc.tests.data;

import lombok.NonNull;
import lombok.Value;

@Value
public class ContractOffer {
  @NonNull private String id;
  private Policy policy;
  private String assetId;
}
