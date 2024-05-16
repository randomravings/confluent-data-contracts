package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.cli.*;
import org.example.clients.ConsumerClient;
import org.example.clients.ProducerClient;
import org.example.utils.HttpUtils;
import org.example.utils.JsonUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class Main {

    private static final String BOOTSTRAP_URLS = "localhost:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";
    private static final String COMPATIBILITY_GROUP = "application.major.version";
    private static final String DEFAULT_TOPiC = "test";
    private static final String DEFAULT_SCHEMA = "person";
    private static final String DEFAULT_VERSION = "1";

    private static final String DEFAULT_DIRECTORY = Paths.get(
            System.getProperty("user.dir"),
            "src",
            "main",
            "resources",
            "data").toString();

    private static final Option DIRECTORY_OPT =  Option
            .builder("d")
            .longOpt("directory")
            .hasArg(true)
            .required(false)
            .type(String.class)
            .desc(String.format("Root directory for schemas and data, current: %s", DEFAULT_DIRECTORY))
            .build();

    private static final Option BOOTSTRAP_OPT = Option
            .builder("b")
            .longOpt("bootstrap-server")
            .hasArg(true)
            .required(false)
            .type(String.class)
            .desc(String.format("list of bootstrap servers, default: %s", BOOTSTRAP_URLS))
            .build();

    private static final Option SCHEMA_REGISTRY_OPT = Option
            .builder("r")
            .longOpt("schema-registry-url")
            .hasArg(true)
            .required(false)
            .type(String.class)
            .desc(String.format("Url for the schema registry, default: %s", SCHEMA_REGISTRY_URL))
            .build();

    private static final Option PRODUCER_OPT = Option
            .builder("p")
            .longOpt("producer")
            .required(true)
            .desc("Run as producer")
            .build();

    private static final Option CONSUMER_OPT = Option
            .builder("c")
            .longOpt("consumer")
            .required(true)
            .desc("Run as consumer")
            .build();

    private static final Option CONSUMER_GROUP_OPT = Option
            .builder("g")
            .longOpt("consumer-group")
            .hasArg(true)
            .required(false)
            .type(String.class)
            .desc("Consumer group, if omitted will read from start")
            .build();

    private static final Option TOPIC_OPT = Option
            .builder("t")
            .longOpt("topic")
            .hasArg(true)
            .required(false)
            .type(String.class)
            .desc(String.format("Topic name to use, default: %s",  DEFAULT_TOPiC))
            .build();

    private static final Option SCHEMA_OPT = Option
            .builder("s")
            .longOpt("schema-name")
            .hasArg(true)
            .required(false)
            .type(String.class)
            .desc(String.format("Schema prefix name for schema, used as: <schema-name>_v<version>_[data|envelope|schema].json, default: %s", DEFAULT_SCHEMA))
            .build();

    private static final Option VERSION_OPT = Option
            .builder("v")
            .longOpt("version")
            .hasArg(true)
            .required(true)
            .type(String.class)
            .desc(String.format("Major schema version, used as: <schema-name>_v<version>_[data|envelope|schema].json, default: %s", DEFAULT_VERSION))
            .build();

    public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException, AlreadySelectedException {

        final Options options = getOptions();
        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter formatter = new HelpFormatter();
        try {
            final CommandLine cmd = parser.parse(options, args);
            String bootstrapServers = cmd.getOptionValue(BOOTSTRAP_OPT.getOpt(), BOOTSTRAP_URLS);
            String schemaRegistry = cmd.getOptionValue(SCHEMA_REGISTRY_OPT.getOpt(), SCHEMA_REGISTRY_URL);
            String sourceDirectory = cmd.getOptionValue(DIRECTORY_OPT.getOpt(), DEFAULT_DIRECTORY);
            String topicName = cmd.getOptionValue(TOPIC_OPT.getOpt(), DEFAULT_TOPiC);
            String schemaName = cmd.getOptionValue(SCHEMA_OPT.getOpt(), DEFAULT_SCHEMA);
            String majorVersion = cmd.getOptionValue(VERSION_OPT.getOpt(), DEFAULT_VERSION);
            String consumerGroupName = cmd.getOptionValue(CONSUMER_GROUP_OPT.getOpt(), "");

            final JsonNode schemaNode = JsonUtils.getSchema(sourceDirectory, schemaName, majorVersion);
            final JsonNode envelope = JsonUtils.getSchemaEnvelope(sourceDirectory, schemaName, majorVersion);
            HttpUtils.EnsureCompatibilityGroup(SCHEMA_REGISTRY_URL, topicName, "BACKWARD", COMPATIBILITY_GROUP);
            HttpUtils.PublishSchema(SCHEMA_REGISTRY_URL, topicName, envelope);

            if(cmd.hasOption(PRODUCER_OPT.getOpt())) {
                final ArrayNode data = JsonUtils.getData(sourceDirectory, schemaName, majorVersion);
                ProducerClient.run(
                        bootstrapServers,
                        schemaRegistry,
                        topicName,
                        schemaNode,
                        data,
                        COMPATIBILITY_GROUP,
                        majorVersion
                );
            }
            else if (cmd.hasOption(CONSUMER_OPT.getOpt())) {
                ConsumerClient.run(
                        bootstrapServers,
                        schemaRegistry,
                        topicName,
                        consumerGroupName,
                        COMPATIBILITY_GROUP,
                        majorVersion
                );
            }
            else {
                throw new MissingArgumentException(String.format("Missing -%s or -%s", PRODUCER_OPT.getOpt(), CONSUMER_OPT.getOpt()));
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
    }

    private static Options getOptions() throws AlreadySelectedException {
        final OptionGroup producerOptions = new OptionGroup();
        producerOptions.addOption(PRODUCER_OPT);

        final OptionGroup consumerOptions = new OptionGroup();
        consumerOptions.addOption(CONSUMER_OPT);
        consumerOptions.addOption(CONSUMER_GROUP_OPT);

        final Options options = new Options();
        options.addOption(TOPIC_OPT);
        options.addOption(VERSION_OPT);
        options.addOption(BOOTSTRAP_OPT);
        options.addOption(SCHEMA_REGISTRY_OPT);
        options.addOption(DIRECTORY_OPT);
        options.addOption(SCHEMA_OPT);
        options.addOptionGroup(producerOptions);
        options.addOptionGroup(consumerOptions);
        return  options;
    }
}
