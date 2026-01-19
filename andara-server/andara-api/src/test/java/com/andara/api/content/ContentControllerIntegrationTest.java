package com.andara.api.content;

import com.andara.content.ContentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration test for ContentController CRUD operations.
 * Tests the full content management workflow: Create → List → Get → Update → Delete.
 * 
 * NOTE: Disabled until full infrastructure (EventStore, Kafka, Flyway migrations) is set up.
 * TODO: Re-enable once all dependencies are properly configured for integration testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Disabled("Requires full infrastructure setup (EventStore, Kafka, migrations)")
class ContentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String testItemId;
    private Map<String, Object> testItemData;

    @BeforeEach
    void setUp() {
        // Generate test item data
        testItemId = "item_test_sword_" + UUID.randomUUID().toString().substring(0, 8);
        testItemData = new HashMap<>();
        testItemData.put("templateId", testItemId);
        testItemData.put("name", "Test Sword");
        testItemData.put("description", "A test sword for integration testing");
        testItemData.put("category", "weapon");
        testItemData.put("baseValue", 100);
        testItemData.put("weight", 5.0);
    }

    @Test
    void testCompleteContentWorkflow() throws Exception {
        // 1. CREATE: Create new item
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("content", testItemData);
        createRequest.put("importedBy", "test-user");
        createRequest.put("changeSummary", "Created for testing");

        String createdItemId = mockMvc.perform(post("/api/admin/content/ITEM_TEMPLATE")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.contentId").value(testItemId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 2. LIST: Verify item appears in list
        mockMvc.perform(get("/api/admin/content/ITEM_TEMPLATE")
                .param("page", "0")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalCount", greaterThan(0)));

        // 3. GET: Retrieve the specific item
        mockMvc.perform(get("/api/admin/content/ITEM_TEMPLATE/" + testItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.templateId").value(testItemId))
                .andExpect(jsonPath("$.content.name").value("Test Sword"))
                .andExpect(jsonPath("$.metadata.contentType").value("ITEM_TEMPLATE"))
                .andExpect(jsonPath("$.metadata.versionNumber").value(1));

        // 4. UPDATE: Modify the item
        Map<String, Object> updatedData = new HashMap<>(testItemData);
        updatedData.put("name", "Updated Test Sword");
        updatedData.put("baseValue", 150);

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("content", updatedData);
        updateRequest.put("importedBy", "test-user");
        updateRequest.put("changeSummary", "Updated for testing");

        mockMvc.perform(put("/api/admin/content/ITEM_TEMPLATE/" + testItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify update
        mockMvc.perform(get("/api/admin/content/ITEM_TEMPLATE/" + testItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.name").value("Updated Test Sword"))
                .andExpect(jsonPath("$.content.baseValue").value(150))
                .andExpect(jsonPath("$.metadata.versionNumber").value(2));

        // 5. VERSION HISTORY: Check version history
        mockMvc.perform(get("/api/admin/content/ITEM_TEMPLATE/" + testItemId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versions", hasSize(2)))
                .andExpect(jsonPath("$.versions[0].versionNumber").value(2))
                .andExpect(jsonPath("$.versions[1].versionNumber").value(1));

        // 6. DELETE: Remove the item
        mockMvc.perform(delete("/api/admin/content/ITEM_TEMPLATE/" + testItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify deletion (should return 404)
        mockMvc.perform(get("/api/admin/content/ITEM_TEMPLATE/" + testItemId))
                .andExpect(status().isNotFound());

        // Version history should still exist
        mockMvc.perform(get("/api/admin/content/ITEM_TEMPLATE/" + testItemId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versions", hasSize(2)));
    }

    @Test
    void testListWithSearchAndPagination() throws Exception {
        // Create multiple items
        for (int i = 0; i < 5; i++) {
            Map<String, Object> itemData = new HashMap<>(testItemData);
            itemData.put("templateId", "item_test_" + i);
            itemData.put("name", "Test Item " + i);

            Map<String, Object> request = new HashMap<>();
            request.put("content", itemData);
            request.put("importedBy", "test-user");

            mockMvc.perform(post("/api/admin/content/ITEM_TEMPLATE")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Test search
        mockMvc.perform(get("/api/admin/content/ITEM_TEMPLATE")
                .param("search", "Test Item 2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThan(0))));

        // Test pagination
        mockMvc.perform(get("/api/admin/content/ITEM_TEMPLATE")
                .param("page", "0")
                .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void testCreateWithInvalidData() throws Exception {
        // Missing required fields
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("templateId", testItemId);
        // Missing: name, category, baseValue, weight

        Map<String, Object> request = new HashMap<>();
        request.put("content", invalidData);

        mockMvc.perform(post("/api/admin/content/ITEM_TEMPLATE")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }

    @Test
    void testUpdateNonExistentItem() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("content", testItemData);

        mockMvc.perform(put("/api/admin/content/ITEM_TEMPLATE/non_existent_id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteNonExistentItem() throws Exception {
        mockMvc.perform(delete("/api/admin/content/ITEM_TEMPLATE/non_existent_id"))
                .andExpect(status().isNotFound());
    }
}
