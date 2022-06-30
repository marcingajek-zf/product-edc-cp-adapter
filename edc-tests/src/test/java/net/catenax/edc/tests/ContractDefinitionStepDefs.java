package net.catenax.edc.tests;

import io.cucumber.java.en.Given;
import java.util.stream.Stream;
import net.catenax.edc.tests.data.ContractDefinition;

public class ContractDefinitionStepDefs {

  @Given("'{connector}' has no contract definitions")
  public void hasNoContractDefinitions(Connector connector) throws Exception {

    final DataManagementAPI api = connector.getDataManagementAPI();

    Stream<ContractDefinition> contractDefinitions = api.getAllContractDefinitions();
    for (ContractDefinition contractDefinition :
        contractDefinitions.toArray(ContractDefinition[]::new)) {
      api.deleteAsset(contractDefinition.getId());
    }
  }
}
