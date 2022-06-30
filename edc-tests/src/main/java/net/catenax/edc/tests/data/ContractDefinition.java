package net.catenax.edc.tests.data;

import java.util.List;

import lombok.Value;

@Value
public class ContractDefinition {

    private String id;
    private List<String> assetIds;
    private String contractPolicyId;
    private String acccessPolicyId;
}
