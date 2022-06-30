package net.catenax.edc.tests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.catenax.edc.tests.data.Asset;
import net.catenax.edc.tests.data.ContractDefinition;
import net.catenax.edc.tests.data.Policy;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;

public class DataManagementAPI {

  private final String ASSET_PATH = "/assets";
  private final String POLICY_PATH = "/policies";
  private final String CONTRACT_DEFINITIONS_PATH = "/contractdefinitions";

  private final String apiKey;
  private final String dataMgmtUrl;
  private final HttpClient httpClient;

  public DataManagementAPI(String dataManagementUrl, String apiKey) {
    this.httpClient = HttpClientBuilder.create().build();
    this.dataMgmtUrl = dataManagementUrl + "/data";
    this.apiKey = apiKey;
  }

  public Asset getAsset(String id) throws IOException, ClientProtocolException {
    final DataManagementApiAsset asset =
        get(ASSET_PATH, "/" + id, new TypeToken<DataManagementApiAsset>() {});
    return mapAsset(asset);
  }

  public Policy getPolicy(String id) throws IOException, ClientProtocolException {
    final DataManagementApiPolicy policy =
        get(POLICY_PATH, "/" + id, new TypeToken<DataManagementApiPolicy>() {});
    return mapPolicy(policy);
  }

  public ContractDefinition getContractDefinition(String id)
      throws IOException, ClientProtocolException {
    final DataManagementApiContractDefinition contractDefinition =
        get(
            CONTRACT_DEFINITIONS_PATH,
            "/" + id,
            new TypeToken<DataManagementApiContractDefinition>() {});
    return mapContractDefinition(contractDefinition);
  }

  public Stream<Asset> getAllAssets() throws IOException, ClientProtocolException {
    final List<DataManagementApiAsset> assets =
        get(ASSET_PATH, new TypeToken<ArrayList<DataManagementApiAsset>>() {});
    return assets.stream().map(this::mapAsset);
  }

  public Stream<Policy> getAllPolicies() throws IOException, ClientProtocolException {
    final List<DataManagementApiPolicy> policies =
        get(POLICY_PATH, new TypeToken<ArrayList<DataManagementApiPolicy>>() {});
    return policies.stream().map(this::mapPolicy);
  }

  public Stream<ContractDefinition> getAllContractDefinitions()
      throws IOException, ClientProtocolException {
    final List<DataManagementApiContractDefinition> contractDefinitions =
        get(
            CONTRACT_DEFINITIONS_PATH,
            new TypeToken<ArrayList<DataManagementApiContractDefinition>>() {});
    return contractDefinitions.stream().map(this::mapContractDefinition);
  }

  public void deleteAsset(String id) throws IOException, ClientProtocolException {
    delete(ASSET_PATH, "/" + id);
  }

  public void deletePolicy(String id) throws IOException, ClientProtocolException {
    delete(POLICY_PATH, "/" + id);
  }

  public void deleteContractDefinition(String id) throws IOException, ClientProtocolException {
    delete(CONTRACT_DEFINITIONS_PATH, "/" + id);
  }

  private <T> T get(String path, TypeToken typeToken) throws IOException, ClientProtocolException {
    return get(path, "", typeToken);
  }

  private <T> T get(String pathSegment1, String pathSegment2, TypeToken typeToken)
      throws IOException, ClientProtocolException {

    final String url = String.format("%s%s%s", dataMgmtUrl, pathSegment1, pathSegment2);
    final HttpGet get = new HttpGet(url);

    final HttpResponse response = sendRequest(get);
    final byte[] json = response.getEntity().getContent().readAllBytes();

    return new Gson().fromJson(new String(json), typeToken.getType());
  }

  private void delete(String pathSegment1, String pathSegment2)
      throws IOException, ClientProtocolException {
    final String url = String.format("%s%s%s", dataMgmtUrl, pathSegment1, pathSegment2);
    final HttpDelete delete = new HttpDelete(url);

    sendRequest(delete);
  }

  private HttpResponse sendRequest(HttpRequestBase request)
      throws IOException, ClientProtocolException {
    request.addHeader("X-Api-Key", apiKey);

    System.out.println(String.format("Send %-6s %s", request.getMethod(), request.getURI()));

    final HttpResponse response = httpClient.execute(request);
    if (200 > response.getStatusLine().getStatusCode()
        || response.getStatusLine().getStatusCode() >= 300) {
      throw new RuntimeException(
          String.format("Unexpected response: %s", response.getStatusLine()));
    }

    return response;
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

    final List<String> assetIds;
    if (dataManagementContractDefinition.selectorExpression == null
        || dataManagementContractDefinition.selectorExpression.getCriteria() == null)
      assetIds = new ArrayList<>();
    else
      assetIds =
          dataManagementContractDefinition.selectorExpression.getCriteria().stream()
              .filter(c -> c.left.equals(DataManagementApiAsset.ID))
              .filter(c -> c.op.equals("="))
              .map(c -> c.getRight())
              .map(c -> (String) c)
              .collect(Collectors.toList());

    return new ContractDefinition(id, assetIds, accessPolicy, contractPolicy);
  }

  @Setter
  @Getter
  @ToString
  private class DataManagementApiAsset {
    public static final String ID = "asset:prop:id";
    public static final String DESCRIPTION = "asset:prop:description";

    private Map<String, Object> properties;
  }

  @Getter
  @Setter
  @ToString
  private class DataManagementApiPolicy {
    private String uid;
  }

  @Getter
  @Setter
  @ToString
  private class DataManagementApiContractDefinition {
    private String id;
    private String accessPolicyId;
    private String contractPolicyId;
    private DataManagementApiAssetSelectorExpression selectorExpression;
  }

  @Getter
  @Setter
  @ToString
  private class DataManagementApiAssetSelectorExpression {

    private List<DataManagementApiCriterion> criteria;
  }

  @Getter
  @Setter
  @ToString
  private class DataManagementApiCriterion {
    private Object left;
    private String op;
    private Object right;
  }
}
