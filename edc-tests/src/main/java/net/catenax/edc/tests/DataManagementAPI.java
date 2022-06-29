package net.catenax.edc.tests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import lombok.Value;

public class DataManagementAPI {

    private final String ASSET_PATH = "/assets";
    private final String POLICY_PATH = "/policies";

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

    public Stream<Asset> getAllAssets() throws IOException, ClientProtocolException {
        final Stream<DataManagementApiAsset> assets = get(ASSET_PATH);
        return assets.map(this::mapAsset);
    }

    public Stream<Policy> getAllPolicies() throws IOException, ClientProtocolException {
        final Stream<DataManagementApiPolicy> policies = get(POLICY_PATH);
        return policies.map(this::mapPolicy);
    }

    public void deleteAsset(String id) throws IOException, ClientProtocolException {
        delete(ASSET_PATH, id);
    }

    public void deletePolicy(String id) throws IOException, ClientProtocolException {
        delete(POLICY_PATH, id);
    }

    private <T> T get(String pathSegment1, String pathSegment2) throws IOException, ClientProtocolException {
        return get(pathSegment1 + "/" + pathSegment2);
    }

    private <T> T get(String path) throws IOException, ClientProtocolException {
        final String url = Paths.get(dataMgmtUrl, path).toString();
        final HttpHost host = new HttpHost(url);
        final HttpRequest request = RequestBuilder.create("GET")
                .addHeader("X-Api-Key", Deployment.DATA_MGMT_ACCESS_KEY)
                .build();

        final HttpResponse response = httpClient.execute(host, request);
        if (200 != response.getStatusLine().getStatusCode()) {
            throw new RuntimeException("Unexpected response: " + response.getStatusLine());
        }

        final InputStream contentStream = response.getEntity().getContent();
        return SerializationUtils.deserialize(contentStream);
    }

    private void delete(String pathSegment1, String pathSegment2) throws IOException, ClientProtocolException {
        final String url = Paths.get(dataMgmtUrl, pathSegment1, pathSegment2).toString();
        final HttpHost host = new HttpHost(url);
        final HttpRequest request = RequestBuilder.create("DELETE")
                .addHeader("X-Api-Key", Deployment.DATA_MGMT_ACCESS_KEY)
                .build();

        final HttpResponse response = httpClient.execute(host, request);
        if (200 != response.getStatusLine().getStatusCode()) {
            throw new RuntimeException("Unexpected response: " + response.getStatusLine());
        }
    }

    private Asset mapAsset(DataManagementApiAsset DataManagementApiAsset) {
        final String id = (String) DataManagementApiAsset.properties.get("asset:prop:id");
        final String description = (String) DataManagementApiAsset.properties.get("asset:prop:description");

        return new Asset(id, description);
    }

    private Policy mapPolicy(DataManagementApiPolicy dataManagementApiPolicy) {
        final String id = dataManagementApiPolicy.uid;

        return new Policy(id);
    }

    @Value
    private class DataManagementApiAsset {
        private Map<String, Object> properties;
    }

    @Value
    private class DataManagementApiPolicy {
        private String uid;
    }
}
