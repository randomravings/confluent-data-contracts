package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.example.clients.ConsumerClient;
import org.example.clients.ProducerClient;
import org.example.utils.HttpUtils;
import org.example.utils.JsonUtils;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    private static final String BOOTSTRAP_URLS = "localhost:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";
    final static int PRODUCER_VERSION = 1;
    final static int CONSUMER_VERSION = 1;

    public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {

        final String topic = "test";
        final String group = "test-cg";
        final JsonNode schema = JsonUtils.getSchema("person", PRODUCER_VERSION);
        final JsonNode envelope = JsonUtils.getSchemaEnvelope("person", PRODUCER_VERSION);
        final ArrayNode data = JsonUtils.getData("person", PRODUCER_VERSION);

        HttpUtils.PublishSchema(SCHEMA_REGISTRY_URL, topic, envelope);

        ProducerClient.run(
                BOOTSTRAP_URLS,
                SCHEMA_REGISTRY_URL,
                topic,
                schema,
                data,
                PRODUCER_VERSION
        );

        ConsumerClient.run(
                BOOTSTRAP_URLS,
                SCHEMA_REGISTRY_URL,
                topic,
                group,
                CONSUMER_VERSION
        );
    }
}
