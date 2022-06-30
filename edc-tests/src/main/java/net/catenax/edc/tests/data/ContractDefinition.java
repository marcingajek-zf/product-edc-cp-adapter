package net.catenax.edc.tests.data;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class ContractDefinition {

  @NonNull private String id;

  private List<String> assetIds;

  @NonNull private String contractPolicyId;
  @NonNull private String acccessPolicyId;
}
