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
    public static JsonNode getSchema(String name, int version) throws IOException {
        final String schemaText = readSchema(name, version);
        final JsonNode schemaNode = MAPPER.readTree(schemaText);
        return schemaNode;
    }

    public static JsonNode getSchemaEnvelope(String name, int version) throws IOException {
        final JsonNode schemaJson = getSchema(name, version);
        final String envelopeText = readEnvelope(name, version);;
        final ObjectNode envelopeJson = (ObjectNode)MAPPER.readTree(envelopeText);
        envelopeJson.put("schema", schemaJson.toString());
        return envelopeJson;
    }

    public static ArrayNode getData(String name, int version) throws IOException {
        final String dataText = readData(name, version);
        final JsonNode dataNode = MAPPER.readTree(dataText);
        return (ArrayNode)dataNode.get("data");
    }

    private static String readSchema(String name, int version) throws IOException {
        return readFile("schema", name, version);
    }

    private static String readEnvelope(String name, int version) throws IOException {
        return readFile("envelope", name, version);
    }

    private static String readData(String name, int version) throws IOException {
        return readFile("data", name, version);
    }

    private static String readFile(String kind, String name, int version) throws IOException {
        final String userDirectory = System.getProperty("user.dir");
        final Path path = Paths.get(userDirectory, "src", "main", "resources", "data", String.format("v%d", version), String.format("%s_v%d_%s.json", name, version, kind));
        return Files.readString(path);
    }
}
