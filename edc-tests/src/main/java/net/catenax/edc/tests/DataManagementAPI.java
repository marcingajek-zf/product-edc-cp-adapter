package net.catenax.edc.tests;

import java.io.IOException;
import java.io.InputStream;
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

    private final String dataMgmtUrl;
    private final HttpClient httpClient;

    public DataManagementAPI(String baseUrl) {
        this.httpClient = HttpClientBuilder.create().build();
        this.dataMgmtUrl = baseUrl + "/data";
    }

    public Stream<Asset> getAllAssets() throws IOException, ClientProtocolException {
        final HttpHost host = new HttpHost(dataMgmtUrl + "/assets");
        final HttpRequest request = RequestBuilder.create("GET")
                .addHeader("X-Api-Key", Deployment.DATA_MGMT_ACCESS_KEY)
                .build();

        final HttpResponse response = httpClient.execute(host, request);
        if (200 != response.getStatusLine().getStatusCode()) {
            throw new RuntimeException("Unexpected response: " + response.getStatusLine());
        }

        final InputStream contentStream = response.getEntity().getContent();
        final Stream<DataManagementApiAsset> assets = SerializationUtils.deserialize(contentStream);

        return assets.map(this::mapAsset);
    }

    public void deleteAsset(Asset asset) throws IOException, ClientProtocolException {
        final HttpHost host = new HttpHost(dataMgmtUrl + "/assets/" + asset.getId());
        final HttpRequest request = RequestBuilder.create("DELETE")
                .addHeader("X-Api-Key", accessKey)
                .build();

        final HttpResponse response = httpClient.execute(host, request);
        if (200 != response.getStatusLine().getStatusCode()) {
            throw new RuntimeException("Unexpected response: " + response.getStatusLine());
        }
    }

    private HttpResponse sendRequest(String method, String path) {
        final String url = Paths.get(dataMgmtUrl, path).toString();
        final HttpHost host = new HttpHost(url);
        final HttpRequest request = RequestBuilder.create(method)
                .addHeader("X-Api-Key", Deployment.DATA_MGMT_ACCESS_KEY)
                .build();

        final HttpResponse response = httpClient.execute(host, request);
        if (200 != response.getStatusLine().getStatusCode()) {
            throw new RuntimeException("Unexpected response: " + response.getStatusLine());
        }

        return response;
    }

    private Asset mapAsset(DataManagementApiAsset DataManagementApiAsset) {
        final String id = (String) DataManagementApiAsset.properties.get("asset:prop:id");
        final String description = (String) DataManagementApiAsset.properties.get("asset:prop:description");

        return new Asset(id, description);
    }

    @Value
    private class DataManagementApiAsset {
        private Map<String, Object> properties;
    }

}
