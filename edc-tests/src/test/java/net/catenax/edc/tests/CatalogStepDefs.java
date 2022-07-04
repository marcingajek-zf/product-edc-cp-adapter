package net.catenax.edc.tests;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.catenax.edc.tests.data.ContractOffer;
import org.apache.http.client.ClientProtocolException;
import org.junit.jupiter.api.Assertions;

public class CatalogStepDefs {

  private List<ContractOffer> lastRequestedOffers;

  @When("'{connector}' requests the catalog from '{connector}'")
  public void requestCatalog(Connector sender, Connector receiver)
      throws ClientProtocolException, IOException {

    final DataManagementAPI dataManagementAPI = sender.getDataManagementAPI();
    final String receiverIdsUrl = receiver.getEnvironment().getIdsUrl() + "/api/v1/ids/data";

    lastRequestedOffers =
        dataManagementAPI.requestCatalogFrom(receiverIdsUrl).collect(Collectors.toList());
  }

  @Then("the catalog contains the following offers")
  public void verifyCatalog(DataTable table) {
    for (Map<String, String> map : table.asMaps()) {
      final String sourceContractDefinitionId = map.get("source definition");
      final String assetId = map.get("asset");

      final boolean isInCatalog =
          lastRequestedOffers.stream()
              .anyMatch(
                  c ->
                      c.getAssetId().equals(assetId)
                          && c.getId().startsWith(sourceContractDefinitionId));

      Assertions.assertTrue(
          isInCatalog, "The catalog does not contain the offer for asset " + assetId);
    }
  }
}
