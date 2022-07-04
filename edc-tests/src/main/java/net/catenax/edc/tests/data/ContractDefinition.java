package net.catenax.edc.tests.data;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class ContractDefinition {

  @NonNull private String id;

  @NonNull private String contractPolicyId;
  @NonNull private String acccessPolicyId;

  private List<String> assetIds;
}
