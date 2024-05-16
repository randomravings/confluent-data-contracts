package org.example.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode getSchema(
            String dir,
            String name,
            String version
    ) throws IOException {
        final String schemaText = readSchema(dir, name, version);
        final JsonNode schemaNode = MAPPER.readTree(schemaText);
        return schemaNode;
    }

    public static JsonNode getSchemaEnvelope(
            String dir,
            String name,
            String version
    ) throws IOException {
        final JsonNode schema = getSchema(dir, name, version);
        final String envelopeText = readEnvelope(dir, name, version);;
        final ObjectNode envelope = (ObjectNode)MAPPER.readTree(envelopeText);
        envelope.put("schema", schema.toString());
        return envelope;
    }

    public static ArrayNode getData(
            String dir,
            String name,
            String version
    ) throws IOException {
        final String dataText = readData(dir, name, version);
        final JsonNode dataNode = MAPPER.readTree(dataText);
        return (ArrayNode)dataNode.get("data");
    }

    public static JsonNode toJson(Object stuff) {
        return MAPPER.convertValue(stuff, JsonNode.class);
    }

    private static String readSchema(
            String dir,
            String name,
            String version
    ) throws IOException {
        return readFile(dir, "schema", name, version);
    }

    private static String readEnvelope(
            String dir,
            String name,
            String version
    ) throws IOException {
        return readFile(dir, "envelope", name, version);
    }

    private static String readData(
            String dir,
            String name,
            String version
    ) throws IOException {
        return readFile(dir, "data", name, version);
    }

    private static String readFile(
            String dir,
            String kind,
            String name,
            String version
    ) throws IOException {
        final Path path = Paths.get(dir, name, String.format("v%s", version), String.format("%s_v%s_%s.json", name, version, kind));
        return Files.readString(path);
    }
}
