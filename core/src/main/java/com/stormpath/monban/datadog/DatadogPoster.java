package com.stormpath.monban.datadog;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DatadogPoster {

    private static final Logger log = LoggerFactory.getLogger(DatadogPoster.class);

    private static final String BASE_URL = "https://app.datadoghq.com/api/v1/series?api_key=";

    private final HttpClient client;
    private final String URI;

    public DatadogPoster(HttpClient client, String apiKey) {
        this.client = client;
        URI = BASE_URL + apiKey;
    }

    public void post(String json) {
        HttpPost post = new HttpPost(URI);

        post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        try {
            //System.out.println("Posting JSON to Datadog: URI: " + URI + ", json: " + json);
            HttpResponse response = client.execute(post);
            //System.out.println("Response status: " + response.getStatusLine());
        } catch (IOException e) {
            log.warn("Unable to POST content to Ducksboard.", e);
        }
    }
}
