package com.andara.application.content;

import com.andara.content.ContentType;
import com.andara.content.model.ContentVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for querying content with pagination and filtering.
 */
@Service
public class ContentQueryService {
    private static final Logger log = LoggerFactory.getLogger(ContentQueryService.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ContentQueryService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * List all active content of a given type with pagination and optional search.
     */
    public ContentListResult listActiveContent(
        ContentType contentType,
        int page,
        int pageSize,
        String searchTerm
    ) {
        int offset = page * pageSize;
        
        String sql;
        Object[] params;
        
        if (searchTerm != null && !searchTerm.isBlank()) {
            // Search in content_id and content_data
            sql = """
                SELECT cv.version_id, cv.content_type, cv.content_id, cv.version_number,
                       cv.content_data, cv.imported_at, cv.imported_by, cv.change_summary
                FROM content_versions cv
                JOIN active_content ac ON cv.version_id = ac.version_id
                WHERE ac.content_type = ?
                  AND (cv.content_id ILIKE ? OR cv.content_data::text ILIKE ?)
                ORDER BY cv.content_id
                LIMIT ? OFFSET ?
                """;
            String searchPattern = "%" + searchTerm + "%";
            params = new Object[]{contentType.name(), searchPattern, searchPattern, pageSize, offset};
        } else {
            sql = """
                SELECT cv.version_id, cv.content_type, cv.content_id, cv.version_number,
                       cv.content_data, cv.imported_at, cv.imported_by, cv.change_summary
                FROM content_versions cv
                JOIN active_content ac ON cv.version_id = ac.version_id
                WHERE ac.content_type = ?
                ORDER BY cv.content_id
                LIMIT ? OFFSET ?
                """;
            params = new Object[]{contentType.name(), pageSize, offset};
        }
        
        List<ContentVersion> items = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                try {
                    JsonNode contentData = objectMapper.readTree(rs.getString("content_data"));
                    return new ContentVersion(
                        (UUID) rs.getObject("version_id"),
                        ContentType.valueOf(rs.getString("content_type")),
                        rs.getString("content_id"),
                        rs.getInt("version_number"),
                        objectMapper.treeToValue(contentData, Object.class),
                        null,
                        rs.getTimestamp("imported_at").toInstant(),
                        rs.getString("imported_by"),
                        rs.getString("change_summary")
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize content", e);
                }
            },
            params
        );
        
        // Get total count
        int totalCount;
        if (searchTerm != null && !searchTerm.isBlank()) {
            String searchPattern = "%" + searchTerm + "%";
            totalCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM active_content ac
                JOIN content_versions cv ON ac.version_id = cv.version_id
                WHERE ac.content_type = ?
                  AND (cv.content_id ILIKE ? OR cv.content_data::text ILIKE ?)
                """,
                Integer.class,
                contentType.name(),
                searchPattern,
                searchPattern
            );
        } else {
            totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM active_content WHERE content_type = ?",
                Integer.class,
                contentType.name()
            );
        }
        
        return new ContentListResult(items, totalCount, page, pageSize);
    }

    /**
     * Get a specific content item by type and ID.
     */
    public Optional<ContentVersion> getContent(ContentType contentType, String contentId) {
        return jdbcTemplate.query(
            """
            SELECT cv.version_id, cv.content_type, cv.content_id, cv.version_number,
                   cv.content_data, cv.imported_at, cv.imported_by, cv.change_summary
            FROM content_versions cv
            JOIN active_content ac ON cv.version_id = ac.version_id
            WHERE ac.content_type = ? AND ac.content_id = ?
            """,
            (rs, rowNum) -> {
                try {
                    JsonNode contentData = objectMapper.readTree(rs.getString("content_data"));
                    return new ContentVersion(
                        (UUID) rs.getObject("version_id"),
                        ContentType.valueOf(rs.getString("content_type")),
                        rs.getString("content_id"),
                        rs.getInt("version_number"),
                        objectMapper.treeToValue(contentData, Object.class),
                        null,
                        rs.getTimestamp("imported_at").toInstant(),
                        rs.getString("imported_by"),
                        rs.getString("change_summary")
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize content", e);
                }
            },
            contentType.name(),
            contentId
        ).stream().findFirst();
    }

    /**
     * Get version history for a content item.
     */
    public List<ContentVersion> getVersionHistory(ContentType contentType, String contentId) {
        return jdbcTemplate.query(
            """
            SELECT version_id, content_type, content_id, version_number,
                   content_data, imported_at, imported_by, change_summary
            FROM content_versions
            WHERE content_type = ? AND content_id = ?
            ORDER BY version_number DESC
            """,
            (rs, rowNum) -> {
                try {
                    JsonNode contentData = objectMapper.readTree(rs.getString("content_data"));
                    return new ContentVersion(
                        (UUID) rs.getObject("version_id"),
                        ContentType.valueOf(rs.getString("content_type")),
                        rs.getString("content_id"),
                        rs.getInt("version_number"),
                        objectMapper.treeToValue(contentData, Object.class),
                        null,
                        rs.getTimestamp("imported_at").toInstant(),
                        rs.getString("imported_by"),
                        rs.getString("change_summary")
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize content", e);
                }
            },
            contentType.name(),
            contentId
        );
    }

    /**
     * Result wrapper for paginated content list.
     */
    public record ContentListResult(
        List<ContentVersion> items,
        int totalCount,
        int page,
        int pageSize
    ) {
        public int totalPages() {
            return (int) Math.ceil((double) totalCount / pageSize);
        }
        
        public boolean hasNext() {
            return page < totalPages() - 1;
        }
    }
}
