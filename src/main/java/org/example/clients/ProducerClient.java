package org.example.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.confluent.kafka.schemaregistry.json.JsonSchemaUtils;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;

public class ProducerClient {
    public static void run(
            String bootstrapServers,
            String schemaRegistryUrl,
            String topic,
            JsonNode schema,
            ArrayNode data,
            int version
    ) {
        Properties props = createProducerProps(bootstrapServers, schemaRegistryUrl, version);
        final Producer<String, ObjectNode> producer = new KafkaProducer<>(props);
        try {
            for(final JsonNode value : data) {
                final String key = UUID.randomUUID().toString();
                ObjectNode jsonValue = JsonSchemaUtils.envelope(schema, value);
                ProducerRecord<String, ObjectNode> jsonRecord = new ProducerRecord<>(topic, key, jsonValue);
                producer.send(jsonRecord).get();
            }
        } catch (WakeupException we) {
            // Ignore
        } catch (Exception ex) {
            System.out.println(String.format("Json producer exception '%s'", ex.getMessage()));
        } finally {
            producer.close();
        }
    }

    private static Properties createProducerProps(String bootstrapServers, String schemaRegistryUrl, int version) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("auto.register.schemas", false);
        props.put("json.fail.invalid.schema", true);
        props.put("use.latest.with.metadata", String.format("application.major.version=%d", version));
        return props;
    }
}
