package com.andara.content.cli.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * Imports content files into the database via REST API.
 */
@CommandLine.Command(
    name = "import",
    description = "Import content files into database"
)
public class ImportCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Content type")
    private String contentTypeStr;

    @CommandLine.Option(names = "--source", description = "Source directory or file", required = true)
    private String source;

    @CommandLine.Option(names = "--env", description = "Environment (dev, staging, prod)", defaultValue = "dev")
    private String environment;

    @CommandLine.Option(names = "--server", description = "Server URL", defaultValue = "http://localhost:8080")
    private String serverUrl;

    @CommandLine.Option(names = "--dry-run", description = "Validate only, don't import", defaultValue = "false")
    private boolean dryRun;

    @CommandLine.Option(names = "--token", description = "Admin API token (or set ANDARA_ADMIN_TOKEN env var)")
    private String adminToken;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public Integer call() throws Exception {
        // Get admin token from option or environment variable
        String token = adminToken != null ? adminToken : System.getenv("ANDARA_ADMIN_TOKEN");
        
        if (token == null || token.isBlank()) {
            System.err.println("Warning: No admin token provided. Request may fail if authentication is enabled.");
            System.err.println("Provide token via --token option or ANDARA_ADMIN_TOKEN environment variable.");
        }

        File sourceFile = new File(source);
        if (!sourceFile.exists()) {
            System.err.println("Error: Source not found: " + source);
            return 1;
        }

        // Collect content items
        List<Object> contentItems = new ArrayList<>();
        
        if (sourceFile.isDirectory()) {
            try (Stream<Path> paths = Files.walk(sourceFile.toPath())) {
                paths.filter(Files::isRegularFile)
                     .filter(p -> p.toString().endsWith(".json"))
                     .forEach(path -> {
                         try {
                             String content = Files.readString(path);
                             Object item = objectMapper.readValue(content, Object.class);
                             contentItems.add(item);
                             System.out.println("Loaded: " + path.getFileName());
                         } catch (Exception e) {
                             System.err.println("Warning: Failed to load " + path.getFileName() + " - " + e.getMessage());
                         }
                     });
            }
        } else {
            // Single file
            String content = Files.readString(sourceFile.toPath());
            Object item = objectMapper.readValue(content, Object.class);
            contentItems.add(item);
            System.out.println("Loaded: " + sourceFile.getName());
        }

        if (contentItems.isEmpty()) {
            System.err.println("Error: No content items found to import");
            return 1;
        }

        System.out.println("\nImporting " + contentItems.size() + " item(s) to " + serverUrl);
        if (dryRun) {
            System.out.println("DRY RUN - Validation only, no data will be imported");
        }
        System.out.println();

        // Prepare request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("items", contentItems);
        requestBody.put("importedBy", "cli-" + System.getProperty("user.name"));
        requestBody.put("changeSummary", "Imported via CLI from " + source);

        String requestJson = objectMapper.writeValueAsString(requestBody);

        // Build request
        String url = serverUrl + "/api/admin/content/import?type=" + contentTypeStr.toUpperCase() + "&dryRun=" + dryRun;
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestJson));

        // Add admin token if available
        if (token != null && !token.isBlank()) {
            requestBuilder.header("X-Admin-Token", token);
        }

        HttpRequest request = requestBuilder.build();

        try {
            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
                
                System.out.println("✓ Import successful!");
                System.out.println();
                
                if (result.containsKey("importedCount")) {
                    System.out.println("Imported: " + result.get("importedCount"));
                }
                
                if (result.containsKey("importedIds")) {
                    List<String> ids = (List<String>) result.get("importedIds");
                    System.out.println("Content IDs:");
                    for (String id : ids) {
                        System.out.println("  - " + id);
                    }
                }
                
                if (result.containsKey("warnings")) {
                    List<String> warnings = (List<String>) result.get("warnings");
                    if (!warnings.isEmpty()) {
                        System.out.println("\nWarnings:");
                        for (String warning : warnings) {
                            System.out.println("  ⚠ " + warning);
                        }
                    }
                }
                
                if (dryRun) {
                    System.out.println("\nDry run completed. No data was imported.");
                }
                
                return 0;
                
            } else if (response.statusCode() == 401) {
                System.err.println("✗ Authentication failed!");
                System.err.println("Please provide a valid admin token via --token or ANDARA_ADMIN_TOKEN env var.");
                return 1;
                
            } else {
                System.err.println("✗ Import failed!");
                System.err.println("Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                
                try {
                    Map<String, Object> errorResult = objectMapper.readValue(response.body(), Map.class);
                    if (errorResult.containsKey("errors")) {
                        List<String> errors = (List<String>) errorResult.get("errors");
                        System.err.println("\nErrors:");
                        for (String error : errors) {
                            System.err.println("  ✗ " + error);
                        }
                    }
                } catch (Exception e) {
                    // Response body might not be JSON
                }
                
                return 1;
            }
            
        } catch (Exception e) {
            System.err.println("✗ Failed to connect to server: " + e.getMessage());
            System.err.println("Make sure the server is running at " + serverUrl);
            return 1;
        }
    }
}
