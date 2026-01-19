package com.andara.content.cli.commands;

import com.andara.content.ContentType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import picocli.CommandLine;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Creates a new content file from a schema template.
 */
@CommandLine.Command(
    name = "create",
    description = "Create new content file from template"
)
public class CreateCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Content type (item-template, skill-definition, etc.)")
    private String contentTypeStr;

    @CommandLine.Option(names = "--output", description = "Output file path", required = true)
    private File outputFile;

    @CommandLine.Option(names = "--with-comments", description = "Include comments for optional fields", defaultValue = "true")
    private boolean withComments;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SCHEMA_BASE_PATH = "/docs/content-schemas/";

    @Override
    public Integer call() throws Exception {
        // Parse content type
        ContentType contentType;
        try {
            contentType = ContentType.valueOf(contentTypeStr.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid content type: " + contentTypeStr);
            System.err.println("Valid types: item-template, skill-definition, ability-definition, recipe, region-definition, zone-template, poi-template, npc-template, faction-definition, encounter-template, dialogue-tree");
            return 1;
        }

        // Load schema
        JsonNode schemaNode = loadSchema(contentType);
        if (schemaNode == null) {
            System.err.println("Error: Schema not found for " + contentType);
            return 1;
        }

        // Generate template from schema
        ObjectNode template = generateTemplate(schemaNode, contentType);

        // Ensure output directory exists
        if (outputFile.getParentFile() != null) {
            Files.createDirectories(outputFile.getParentFile().toPath());
        }

        // Write to file with pretty printing
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, template);

        System.out.println("✓ Created template: " + outputFile.getAbsolutePath());
        System.out.println();
        System.out.println("Template for " + contentType + " has been created.");
        System.out.println("Fill in the required fields and validate with: andara-content validate " + outputFile.getPath());

        return 0;
    }

    private JsonNode loadSchema(ContentType contentType) throws Exception {
        // Try to load from classpath first
        String schemaPath = SCHEMA_BASE_PATH + contentType.getSchemaPath();
        InputStream schemaStream = getClass().getResourceAsStream(schemaPath);

        if (schemaStream == null) {
            // Try loading from filesystem (for development)
            Path workspacePath = Paths.get("docs/content-schemas/" + contentType.getSchemaPath());
            if (Files.exists(workspacePath)) {
                return objectMapper.readTree(workspacePath.toFile());
            } else {
                // Try from classloader as fallback
                schemaStream = getClass().getClassLoader().getResourceAsStream(schemaPath);
                if (schemaStream != null) {
                    return objectMapper.readTree(schemaStream);
                }
            }
        } else {
            return objectMapper.readTree(schemaStream);
        }

        return null;
    }

    private ObjectNode generateTemplate(JsonNode schema, ContentType contentType) {
        ObjectNode template = objectMapper.createObjectNode();

        // Get properties from schema
        JsonNode properties = schema.get("properties");
        JsonNode required = schema.get("required");

        if (properties == null) {
            return template;
        }

        // Iterate through properties
        Iterator<String> fieldNames = properties.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode propertySchema = properties.get(fieldName);

            boolean isRequired = isFieldRequired(fieldName, required);

            if (isRequired) {
                // Add with default value
                template.set(fieldName, generateDefaultValue(fieldName, propertySchema, contentType));
            } else if (withComments) {
                // Add commented out or with null/placeholder
                template.set(fieldName, generatePlaceholderValue(fieldName, propertySchema));
            }
        }

        return template;
    }

    private boolean isFieldRequired(String fieldName, JsonNode required) {
        if (required == null || !required.isArray()) {
            return false;
        }

        for (JsonNode req : required) {
            if (req.asText().equals(fieldName)) {
                return true;
            }
        }

        return false;
    }

    private JsonNode generateDefaultValue(String fieldName, JsonNode propertySchema, ContentType contentType) {
        String type = propertySchema.has("type") ? propertySchema.get("type").asText() : "string";

        // Handle special fields
        if (fieldName.endsWith("Id") && type.equals("string")) {
            if (propertySchema.has("format") && propertySchema.get("format").asText().equals("uuid")) {
                return new TextNode(UUID.randomUUID().toString());
            }
            return new TextNode("TODO_" + fieldName);
        }

        if (fieldName.equals("name")) {
            return new TextNode("New " + contentType.name().replace("_", " "));
        }

        if (fieldName.equals("description")) {
            return new TextNode("Description for this " + contentType.name().toLowerCase());
        }

        // Handle by type
        return switch (type) {
            case "string" -> {
                if (propertySchema.has("enum")) {
                    // Use first enum value
                    JsonNode enumValues = propertySchema.get("enum");
                    if (enumValues.isArray() && enumValues.size() > 0) {
                        yield new TextNode(enumValues.get(0).asText());
                    }
                }
                yield new TextNode("");
            }
            case "integer", "number" -> {
                if (propertySchema.has("minimum")) {
                    yield new IntNode(propertySchema.get("minimum").asInt());
                }
                if (propertySchema.has("default")) {
                    yield new IntNode(propertySchema.get("default").asInt());
                }
                yield new IntNode(0);
            }
            case "boolean" -> {
                if (propertySchema.has("default")) {
                    yield BooleanNode.valueOf(propertySchema.get("default").asBoolean());
                }
                yield BooleanNode.FALSE;
            }
            case "array" -> objectMapper.createArrayNode();
            case "object" -> {
                if (propertySchema.has("properties")) {
                    yield generateTemplate(propertySchema, contentType);
                }
                yield objectMapper.createObjectNode();
            }
            default -> NullNode.getInstance();
        };
    }

    private JsonNode generatePlaceholderValue(String fieldName, JsonNode propertySchema) {
        String type = propertySchema.has("type") ? propertySchema.get("type").asText() : "string";

        // For optional fields, use null or empty based on type
        return switch (type) {
            case "string" -> NullNode.getInstance();
            case "integer", "number" -> NullNode.getInstance();
            case "boolean" -> NullNode.getInstance();
            case "array" -> objectMapper.createArrayNode();
            case "object" -> objectMapper.createObjectNode();
            default -> NullNode.getInstance();
        };
    }
}
