package net.catenax.edc.tests.data;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class ContractDefinition {

  @NonNull String id;

  @NonNull String contractPolicyId;
  @NonNull String acccessPolicyId;

  List<String> assetIds;
}
