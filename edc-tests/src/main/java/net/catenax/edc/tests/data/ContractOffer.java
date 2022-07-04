package net.catenax.edc.tests.data;

import lombok.NonNull;
import lombok.Value;

@Value
public class ContractOffer {
  @NonNull String id;
  Policy policy;
  String assetId;
}
