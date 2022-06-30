package net.catenax.edc.tests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Value;
import net.catenax.edc.tests.data.Asset;
import net.catenax.edc.tests.data.ContractDefinition;
import net.catenax.edc.tests.data.Policy;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

public class DataManagementAPI {

  private final String ASSET_PATH = "/assets";
  private final String POLICY_PATH = "/policies";
  private final String CONTRACT_DEFINITIONS_PATH = "/contractdefinitions";

  private final String dataMgmtUrl;
  private final HttpClient httpClient;

  public DataManagementAPI(String baseUrl) {
    this.httpClient = HttpClientBuilder.create().build();
    this.dataMgmtUrl = Path.of(baseUrl, "data").toString();
  }

  public Asset getAsset(String id) throws IOException, ClientProtocolException {
    final DataManagementApiAsset asset = get(ASSET_PATH, id);
    return mapAsset(asset);
  }

  public Policy getPolicy(String id) throws IOException, ClientProtocolException {
    final DataManagementApiPolicy policy = get(POLICY_PATH, id);
    return mapPolicy(policy);
  }

  public ContractDefinition getContractDefinition(String id)
      throws IOException, ClientProtocolException {
    final DataManagementApiContractDefinition contractDefinition =
        get(CONTRACT_DEFINITIONS_PATH, id);
    return mapContractDefinition(contractDefinition);
  }

  public Stream<Asset> getAllAssets() throws IOException, ClientProtocolException {
    final Stream<DataManagementApiAsset> assets = get(ASSET_PATH);
    return assets.map(this::mapAsset);
  }

  public Stream<Policy> getAllPolicies() throws IOException, ClientProtocolException {
    final Stream<DataManagementApiPolicy> policies = get(POLICY_PATH);
    return policies.map(this::mapPolicy);
  }

  public Stream<ContractDefinition> getAllContractDefinitions()
      throws IOException, ClientProtocolException {
    final Stream<DataManagementApiContractDefinition> contractDefinitions =
        get(CONTRACT_DEFINITIONS_PATH);
    return contractDefinitions.map(this::mapContractDefinition);
  }

  public void deleteAsset(String id) throws IOException, ClientProtocolException {
    delete(ASSET_PATH, id);
  }

  public void deletePolicy(String id) throws IOException, ClientProtocolException {
    delete(POLICY_PATH, id);
  }

  public void deleteContractDefinition(String id) throws IOException, ClientProtocolException {
    delete(CONTRACT_DEFINITIONS_PATH, id);
  }

  private <T> T get(String pathSegment1, String pathSegment2)
      throws IOException, ClientProtocolException {
    return get(Path.of(pathSegment1, pathSegment2).toString());
  }

  private <T> T get(String path) throws IOException, ClientProtocolException {
    final String url = Path.of(dataMgmtUrl, path).toString();
    final HttpHost host = new HttpHost(url);
    final HttpRequest request =
        RequestBuilder.create("GET")
            .addHeader("X-Api-Key", Deployment.DATA_MGMT_ACCESS_KEY)
            .build();

    final HttpResponse response = httpClient.execute(host, request);
    if (200 != response.getStatusLine().getStatusCode()) {
      throw new RuntimeException("Unexpected response: " + response.getStatusLine());
    }

    final InputStream contentStream = response.getEntity().getContent();
    return SerializationUtils.deserialize(contentStream);
  }

  private void delete(String pathSegment1, String pathSegment2)
      throws IOException, ClientProtocolException {
    final String url = Path.of(dataMgmtUrl, pathSegment1, pathSegment2).toString();
    final HttpHost host = new HttpHost(url);
    final HttpRequest request =
        RequestBuilder.create("DELETE")
            .addHeader("X-Api-Key", Deployment.DATA_MGMT_ACCESS_KEY)
            .build();

    final HttpResponse response = httpClient.execute(host, request);
    if (200 != response.getStatusLine().getStatusCode()) {
      throw new RuntimeException("Unexpected response: " + response.getStatusLine());
    }
  }

  private Asset mapAsset(DataManagementApiAsset DataManagementApiAsset) {
    final String id = (String) DataManagementApiAsset.properties.get(DataManagementApiAsset.ID);
    final String description =
        (String) DataManagementApiAsset.properties.get(DataManagementApiAsset.DESCRIPTION);

    return new Asset(id, description);
  }

  private Policy mapPolicy(DataManagementApiPolicy dataManagementApiPolicy) {
    final String id = dataManagementApiPolicy.uid;

    return new Policy(id);
  }

  private ContractDefinition mapContractDefinition(
      DataManagementApiContractDefinition dataManagementContractDefinition) {
    final String id = dataManagementContractDefinition.id;
    final String accessPolicy = dataManagementContractDefinition.accessPolicyId;
    final String contractPolicy = dataManagementContractDefinition.contractPolicyId;
    final List<String> assetIds =
        dataManagementContractDefinition.selectorExpression.getCriteria().stream()
            .filter(c -> c.left.equals(DataManagementApiAsset.ID))
            .filter(c -> c.op.equals("="))
            .map(c -> c.getRight())
            .map(c -> (String) c)
            .collect(Collectors.toList());

    return new ContractDefinition(id, assetIds, accessPolicy, contractPolicy);
  }

  @Value
  private class DataManagementApiAsset {
    public static final String ID = "asset:prop:id";
    public static final String DESCRIPTION = "asset:prop:description";

    private Map<String, Object> properties;
  }

  @Value
  private class DataManagementApiPolicy {
    private String uid;
  }

  @Value
  private class DataManagementApiContractDefinition {
    private String id;
    private String accessPolicyId;
    private String contractPolicyId;
    private DataManagementApiAssetSelectorExpression selectorExpression;
  }

  @Value
  private class DataManagementApiAssetSelectorExpression {

    private List<DataManagementApiCriterion> criteria;
  }

  @Value
  private class DataManagementApiCriterion {
    private Object left;
    private String op;
    private Object right;
  }
}
