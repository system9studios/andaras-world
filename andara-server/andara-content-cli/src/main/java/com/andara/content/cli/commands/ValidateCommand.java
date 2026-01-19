package com.andara.content.cli.commands;

import com.andara.content.ContentType;
import com.andara.content.validation.SchemaValidator;
import com.andara.content.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * Validates content files against schemas.
 */
@CommandLine.Command(
    name = "validate",
    description = "Validate content files against schemas"
)
public class ValidateCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Content file or directory to validate")
    private File target;

    @CommandLine.Option(names = "--type", description = "Content type (item-template, skill-definition, etc.)")
    private String contentTypeStr;

    @CommandLine.Option(names = "--all", description = "Validate all files in directory recursively")
    private boolean validateAll = false;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SchemaValidator schemaValidator = new SchemaValidator(objectMapper);

    @Override
    public Integer call() throws Exception {
        if (!target.exists()) {
            System.err.println("Error: File or directory not found: " + target);
            return 1;
        }

        List<ValidationEntry> entries = collectValidationEntries();
        
        if (entries.isEmpty()) {
            System.err.println("Error: No content files found to validate");
            return 1;
        }

        System.out.println("Validating " + entries.size() + " file(s)...\n");

        int errorCount = 0;
        int warningCount = 0;

        for (ValidationEntry entry : entries) {
            System.out.println("Validating: " + entry.file.getName() + " (" + entry.contentType + ")");
            
            try {
                String contentJson = Files.readString(entry.file.toPath());
                Object content = objectMapper.readValue(contentJson, Object.class);
                
                ValidationResult result = schemaValidator.validate(entry.contentType, content);
                
                if (result.isValid()) {
                    System.out.println("  ✓ Valid");
                    
                    if (!result.getWarnings().isEmpty()) {
                        System.out.println("  Warnings:");
                        for (String warning : result.getWarnings()) {
                            System.out.println("    ⚠ " + warning);
                            warningCount++;
                        }
                    }
                    
                    if (!result.getSuggestions().isEmpty()) {
                        System.out.println("  Suggestions:");
                        for (String suggestion : result.getSuggestions()) {
                            System.out.println("    💡 " + suggestion);
                        }
                    }
                } else {
                    System.out.println("  ✗ Invalid");
                    System.out.println("  Errors:");
                    for (String error : result.getErrors()) {
                        System.out.println("    ✗ " + error);
                        errorCount++;
                    }
                    
                    if (!result.getWarnings().isEmpty()) {
                        System.out.println("  Warnings:");
                        for (String warning : result.getWarnings()) {
                            System.out.println("    ⚠ " + warning);
                            warningCount++;
                        }
                    }
                }
                
            } catch (Exception e) {
                System.err.println("  ✗ Error: " + e.getMessage());
                errorCount++;
            }
            
            System.out.println();
        }

        // Summary
        System.out.println("═════════════════════════════════════");
        System.out.println("Validation Summary");
        System.out.println("═════════════════════════════════════");
        System.out.println("Files validated: " + entries.size());
        System.out.println("Errors: " + errorCount);
        System.out.println("Warnings: " + warningCount);
        System.out.println("═════════════════════════════════════");

        return errorCount > 0 ? 1 : 0;
    }

    private List<ValidationEntry> collectValidationEntries() throws Exception {
        List<ValidationEntry> entries = new ArrayList<>();

        if (target.isDirectory()) {
            if (validateAll) {
                // Recursively find all JSON files
                try (Stream<Path> paths = Files.walk(target.toPath())) {
                    paths.filter(Files::isRegularFile)
                         .filter(p -> p.toString().endsWith(".json"))
                         .forEach(path -> {
                             File file = path.toFile();
                             try {
                                 ContentType type = inferContentType(file);
                                 entries.add(new ValidationEntry(file, type));
                             } catch (Exception e) {
                                 System.err.println("Warning: Skipping " + file.getName() + " - " + e.getMessage());
                             }
                         });
                }
            } else {
                // Only files in immediate directory
                File[] files = target.listFiles((dir, name) -> name.endsWith(".json"));
                if (files != null) {
                    for (File file : files) {
                        try {
                            ContentType type = inferContentType(file);
                            entries.add(new ValidationEntry(file, type));
                        } catch (Exception e) {
                            System.err.println("Warning: Skipping " + file.getName() + " - " + e.getMessage());
                        }
                    }
                }
            }
        } else {
            // Single file
            ContentType type = determineContentType();
            entries.add(new ValidationEntry(target, type));
        }

        return entries;
    }

    private ContentType determineContentType() {
        if (contentTypeStr != null) {
            try {
                return ContentType.valueOf(contentTypeStr.toUpperCase().replace("-", "_"));
            } catch (IllegalArgumentException e) {
                throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Invalid content type: " + contentTypeStr
                );
            }
        }
        
        return inferContentType(target);
    }

    private ContentType inferContentType(File file) {
        String fileName = file.getName();
        
        // Try to infer from filename patterns
        if (fileName.contains("item") || fileName.contains("weapon") || fileName.contains("armor")) {
            return ContentType.ITEM_TEMPLATE;
        } else if (fileName.contains("skill")) {
            return ContentType.SKILL_DEFINITION;
        } else if (fileName.contains("ability")) {
            return ContentType.ABILITY_DEFINITION;
        } else if (fileName.contains("recipe")) {
            return ContentType.RECIPE;
        } else if (fileName.contains("region")) {
            return ContentType.REGION_DEFINITION;
        } else if (fileName.contains("zone")) {
            return ContentType.ZONE_TEMPLATE;
        } else if (fileName.contains("poi")) {
            return ContentType.POI_TEMPLATE;
        } else if (fileName.contains("npc")) {
            return ContentType.NPC_TEMPLATE;
        } else if (fileName.contains("faction")) {
            return ContentType.FACTION_DEFINITION;
        } else if (fileName.contains("encounter")) {
            return ContentType.ENCOUNTER_TEMPLATE;
        } else if (fileName.contains("dialogue")) {
            return ContentType.DIALOGUE_TREE;
        }
        
        throw new IllegalArgumentException(
            "Could not determine content type for: " + fileName + ". Please specify --type"
        );
    }

    private static class ValidationEntry {
        final File file;
        final ContentType contentType;

        ValidationEntry(File file, ContentType contentType) {
            this.file = file;
            this.contentType = contentType;
        }
    }
}
