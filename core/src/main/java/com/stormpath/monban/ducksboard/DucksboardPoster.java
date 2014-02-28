package com.stormpath.monban.ducksboard;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.shiro.codec.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

public class DucksboardPoster {

    private static final Logger log = LoggerFactory.getLogger(DucksboardPoster.class);

    private static final String BASE_URL = "https://push.ducksboard.com/v/";

    private final HttpClient client;
    private final String apiKey;

    public DucksboardPoster(HttpClient client, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
    }

    public void post(String valueId, String json) {
        String uri = BASE_URL + valueId;

        HttpPost post = new HttpPost(uri);
        String authzVal = apiKey + ":unused";
        authzVal = Base64.encodeToString(authzVal.getBytes(Charset.forName("UTF-8")));
        authzVal = "Basic " + authzVal;
        post.setHeader("Authorization", authzVal);

        post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        try {
            System.out.println("Posting JSON to DucksBoard: URI: " + uri + ", json: " + json);
            HttpResponse response = client.execute(post);
            System.out.println("Response status: " + response.getStatusLine());
        } catch (IOException e) {
            log.warn("Unable to POST content to Ducksboard.", e);
        }
    }

}
