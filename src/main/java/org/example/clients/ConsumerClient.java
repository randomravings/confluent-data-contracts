package org.example.clients;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.utils.JsonUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConsumerClient {
    public  static void run(
            String bootstrapServers,
            String schemaRegistryUrl,
            String topic,
            String group,
            String versionKey,
            String version
    ) {
        final Duration pollDuration = Duration.ofMillis(2000);
        Properties props = createConsumerProps(bootstrapServers, schemaRegistryUrl, group, versionKey, version);
        System.out.println(props);
        final Consumer<String, ObjectNode> consumer = new KafkaConsumer<>(props);
        if(group == null || group == ""){
            final List<PartitionInfo> partitionInfo =  consumer.partitionsFor(topic);
            final List<TopicPartition> partitions = partitionInfo.stream().map(p -> new TopicPartition(topic, p.partition())).toList();
            consumer.assign(partitions);
            consumer.seekToBeginning(partitions);
        }
        else {
            consumer.subscribe(Arrays.asList(topic));
        }

        try {
            while (true) {
                ConsumerRecords<?, ?> records = consumer.poll(pollDuration);
                for(var record : records) {
                    //System.out.println(String.format("v:%d|p:%d|o:%d|k:%s|v:%s", version, record.partition(), record.offset(), record.key(), record.value()));
                    System.out.println(JsonUtils.toJson(record.value()).toString());
                }
            }

        } catch (WakeupException we) {
            // Ignore
        } catch (Exception ex) {
            System.out.println(String.format("Json consumer exception '%s'", ex.getMessage()));
        } finally {
            consumer.close();
        }
    }

    private static Properties createConsumerProps(
            String bootstrapServers,
            String schemaRegistryUrl,
            String group,
            String versionKey,
            String version
    ) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class);
        props.put(ConsumerConfig.METRICS_RECORDING_LEVEL_CONFIG, "INFO");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("use.latest.with.metadata", String.format("%s=%s", versionKey, version));
        return props;
    }
}
