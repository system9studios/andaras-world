package com.andara.api.content;

import com.andara.application.content.ContentImportService;
import com.andara.application.content.ContentQueryService;
import com.andara.application.content.ContentRepositoryService;
import com.andara.content.ContentType;
import com.andara.content.model.ContentVersion;
import com.andara.content.model.ImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for content CRUD operations.
 * TODO: Add @PreAuthorize("hasRole('ADMIN')") when authentication is implemented
 */
@RestController
@RequestMapping("/api/admin/content")
public class ContentController {
    private static final Logger log = LoggerFactory.getLogger(ContentController.class);
    
    private final ContentQueryService queryService;
    private final ContentRepositoryService repositoryService;
    private final ContentImportService importService;

    public ContentController(
        ContentQueryService queryService,
        ContentRepositoryService repositoryService,
        ContentImportService importService
    ) {
        this.queryService = queryService;
        this.repositoryService = repositoryService;
        this.importService = importService;
    }

    /**
     * List all content of a given type with pagination and search.
     * 
     * @param contentTypeStr Content type
     * @param page Page number (default: 0)
     * @param pageSize Page size (default: 20)
     * @param search Optional search term
     * @return Paginated list of content items
     */
    @GetMapping("/{type}")
    // TODO: @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listContent(
        @PathVariable("type") String contentTypeStr,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
        @RequestParam(value = "search", required = false) String search
    ) {
        try {
            ContentType contentType = ContentType.valueOf(contentTypeStr.toUpperCase());
            
            ContentQueryService.ContentListResult result = queryService.listActiveContent(
                contentType,
                page,
                pageSize,
                search
            );
            
            return ResponseEntity.ok(Map.of(
                "items", result.items(),
                "totalCount", result.totalCount(),
                "page", result.page(),
                "pageSize", result.pageSize(),
                "totalPages", result.totalPages(),
                "hasNext", result.hasNext()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid content type: " + contentTypeStr));
        } catch (Exception e) {
            log.error("Failed to list content", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to list content: " + e.getMessage()));
        }
    }

    /**
     * Get a specific content item by type and ID.
     * 
     * @param contentTypeStr Content type
     * @param contentId Content ID
     * @return Content item with metadata
     */
    @GetMapping("/{type}/{id}")
    // TODO: @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getContent(
        @PathVariable("type") String contentTypeStr,
        @PathVariable("id") String contentId
    ) {
        try {
            ContentType contentType = ContentType.valueOf(contentTypeStr.toUpperCase());
            
            return queryService.getContent(contentType, contentId)
                .map(content -> (ResponseEntity<?>) ResponseEntity.ok(Map.of(
                    "content", content.getContentData(),
                    "metadata", Map.of(
                        "contentId", content.getContentId(),
                        "contentType", content.getContentType(),
                        "versionNumber", content.getVersionNumber(),
                        "importedAt", content.getImportedAt(),
                        "importedBy", content.getImportedBy(),
                        "changeSummary", content.getChangeSummary() != null ? content.getChangeSummary() : ""
                    )
                )))
                .orElse(ResponseEntity.notFound().build());
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid content type: " + contentTypeStr));
        } catch (Exception e) {
            log.error("Failed to get content", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get content: " + e.getMessage()));
        }
    }

    /**
     * Create new content item.
     * 
     * @param contentTypeStr Content type
     * @param requestBody Request containing content data
     * @return Created content metadata
     */
    @PostMapping("/{type}")
    // TODO: @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createContent(
        @PathVariable("type") String contentTypeStr,
        @RequestBody Map<String, Object> requestBody
    ) {
        try {
            ContentType contentType = ContentType.valueOf(contentTypeStr.toUpperCase());
            
            Object contentData = requestBody.get("content");
            if (contentData == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No content data provided"));
            }
            
            String importedBy = (String) requestBody.getOrDefault("importedBy", "admin");
            String changeSummary = (String) requestBody.getOrDefault("changeSummary", "Created via API");
            
            // Use import service to validate and save
            ImportResult result = importService.importContent(
                contentType,
                List.of(contentData),
                importedBy,
                changeSummary,
                false
            );
            
            if (result.isSuccess()) {
                String contentId = result.getImportedIds().get(0);
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "success", true,
                        "contentId", contentId,
                        "message", "Content created successfully"
                    ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "errors", result.getErrors()
                    ));
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid content type: " + contentTypeStr));
        } catch (Exception e) {
            log.error("Failed to create content", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create content: " + e.getMessage()));
        }
    }

    /**
     * Update existing content item.
     * 
     * @param contentTypeStr Content type
     * @param contentId Content ID
     * @param requestBody Request containing updated content data
     * @return Update result
     */
    @PutMapping("/{type}/{id}")
    // TODO: @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateContent(
        @PathVariable("type") String contentTypeStr,
        @PathVariable("id") String contentId,
        @RequestBody Map<String, Object> requestBody
    ) {
        try {
            ContentType contentType = ContentType.valueOf(contentTypeStr.toUpperCase());
            
            // Check if content exists
            if (!repositoryService.exists(contentType, contentId)) {
                return ResponseEntity.notFound().build();
            }
            
            Object contentData = requestBody.get("content");
            if (contentData == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No content data provided"));
            }
            
            String importedBy = (String) requestBody.getOrDefault("importedBy", "admin");
            String changeSummary = (String) requestBody.getOrDefault("changeSummary", "Updated via API");
            
            // Validate and save new version
            ImportResult result = importService.importContent(
                contentType,
                List.of(contentData),
                importedBy,
                changeSummary,
                false
            );
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "contentId", contentId,
                    "message", "Content updated successfully"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "errors", result.getErrors()
                    ));
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid content type: " + contentTypeStr));
        } catch (Exception e) {
            log.error("Failed to update content", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update content: " + e.getMessage()));
        }
    }

    /**
     * Delete content item (deactivates it, preserves version history).
     * 
     * @param contentTypeStr Content type
     * @param contentId Content ID
     * @return Deletion result
     */
    @DeleteMapping("/{type}/{id}")
    // TODO: @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteContent(
        @PathVariable("type") String contentTypeStr,
        @PathVariable("id") String contentId
    ) {
        try {
            ContentType contentType = ContentType.valueOf(contentTypeStr.toUpperCase());
            
            boolean deleted = repositoryService.deleteContent(contentType, contentId);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Content deleted successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid content type: " + contentTypeStr));
        } catch (Exception e) {
            log.error("Failed to delete content", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete content: " + e.getMessage()));
        }
    }

    /**
     * Get version history for a content item.
     * 
     * @param contentTypeStr Content type
     * @param contentId Content ID
     * @return List of all versions
     */
    @GetMapping("/{type}/{id}/history")
    // TODO: @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getVersionHistory(
        @PathVariable("type") String contentTypeStr,
        @PathVariable("id") String contentId
    ) {
        try {
            ContentType contentType = ContentType.valueOf(contentTypeStr.toUpperCase());
            
            List<ContentVersion> history = queryService.getVersionHistory(contentType, contentId);
            
            if (history.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "contentId", contentId,
                "contentType", contentType,
                "versions", history
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid content type: " + contentTypeStr));
        } catch (Exception e) {
            log.error("Failed to get version history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get version history: " + e.getMessage()));
        }
    }
}
