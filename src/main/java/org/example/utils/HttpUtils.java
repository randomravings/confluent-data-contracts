package org.example.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtils {
    public static void EnsureCompatibilityGroup(
            String url,
            String topic,
            String compatibility,
            String compatibilityGroup
    ) throws URISyntaxException, IOException, InterruptedException {
        final String body = String.format("{\"compatibility\": \"%s\", \"compatibilityGroup\": \"%s\"}", compatibility, compatibilityGroup);
        final HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(new URI(String.format("%s/config/%s-value", url, topic)))
                .headers("Content-Type", "application/vnd.schemaregistry.v1+json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200)
            throw new RuntimeException(response.body());
    }

    public static void PublishSchema(
            String url,
            String topic,
            JsonNode envelope
    ) throws URISyntaxException, IOException, InterruptedException {
        final HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(new URI(String.format("%s/subjects/%s-value/versions", url, topic)))
                .headers("Content-Type", "application/vnd.schemaregistry.v1+json")
                .POST(HttpRequest.BodyPublishers.ofString(envelope.toString()))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200)
            throw new RuntimeException(response.body());
    }
}
