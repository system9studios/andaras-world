package com.andara.content.cli.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Exports content from database to files via REST API.
 */
@CommandLine.Command(
    name = "export",
    description = "Export content from database to files"
)
public class ExportCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Content type")
    private String contentTypeStr;

    @CommandLine.Option(names = "--output", description = "Output directory", defaultValue = "./content-export")
    private String outputDir;

    @CommandLine.Option(names = "--env", description = "Environment", defaultValue = "dev")
    private String environment;

    @CommandLine.Option(names = "--server", description = "Server URL", defaultValue = "http://localhost:8080")
    private String serverUrl;

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

        System.out.println("Exporting " + contentTypeStr.toUpperCase() + " from " + serverUrl);
        System.out.println("Output directory: " + outputDir);
        System.out.println();

        // Build request URL
        String url = String.format("%s/api/admin/content/export?type=%s&outputDir=%s&exportedBy=%s&environment=%s",
            serverUrl,
            URLEncoder.encode(contentTypeStr.toUpperCase(), StandardCharsets.UTF_8),
            URLEncoder.encode(outputDir, StandardCharsets.UTF_8),
            URLEncoder.encode("cli-" + System.getProperty("user.name"), StandardCharsets.UTF_8),
            URLEncoder.encode(environment, StandardCharsets.UTF_8)
        );

        // Build request
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET();

        // Add admin token if available
        if (token != null && !token.isBlank()) {
            requestBuilder.header("X-Admin-Token", token);
        }

        HttpRequest request = requestBuilder.build();

        try {
            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
                
                System.out.println("✓ Export successful!");
                System.out.println();
                
                if (result.containsKey("exportedCount")) {
                    System.out.println("Exported items: " + result.get("exportedCount"));
                }
                
                if (result.containsKey("outputDirectory")) {
                    System.out.println("Output directory: " + result.get("outputDirectory"));
                }
                
                if (result.containsKey("manifestPath")) {
                    System.out.println("Manifest file: " + result.get("manifestPath"));
                }
                
                if (result.containsKey("errors")) {
                    List<String> errors = (List<String>) result.get("errors");
                    if (!errors.isEmpty()) {
                        System.out.println("\nErrors:");
                        for (String error : errors) {
                            System.out.println("  ✗ " + error);
                        }
                    }
                }
                
                return 0;
                
            } else if (response.statusCode() == 401) {
                System.err.println("✗ Authentication failed!");
                System.err.println("Please provide a valid admin token via --token or ANDARA_ADMIN_TOKEN env var.");
                return 1;
                
            } else {
                System.err.println("✗ Export failed!");
                System.err.println("Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return 1;
            }
            
        } catch (Exception e) {
            System.err.println("✗ Failed to connect to server: " + e.getMessage());
            System.err.println("Make sure the server is running at " + serverUrl);
            return 1;
        }
    }
}
