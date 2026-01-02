package com.andara.api.party;

import com.andara.application.party.CharacterService;
import com.andara.common.Result;
import com.andara.domain.DomainEvent;
import com.andara.domain.party.*;
import com.andara.domain.party.events.CharacterCreated;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CharacterController.class)
@Import(com.andara.api.ApiTestConfiguration.class)
@ActiveProfiles("test")
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CharacterService characterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCharacter_validRequest_shouldReturnCreated() throws Exception {
        UUID characterId = UUID.randomUUID();
        UUID instanceId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();

        CharacterCreated event = CharacterCreated.create(
            CharacterId.from(characterId),
            CharacterName.of("Test Character"),
            Origin.VAULT_DWELLER,
            Attributes.create(8, 8, 8, 8, 8, 8),
            Map.of("mechanics", 20),
            Appearance.defaultAppearance(),
            true,
            instanceId,
            agentId
        );

        when(characterService.createCharacter(any())).thenReturn(Result.success(List.of(event)));

        CreateCharacterRequestDto request = new CreateCharacterRequestDto(
            "Test Character",
            Origin.VAULT_DWELLER,
            new AttributesDto(8, 8, 8, 8, 8, 8),
            List.of("mechanics", "electronics"),
            new AppearanceDto(
                com.andara.domain.party.Appearance.Gender.NON_BINARY,
                com.andara.domain.party.Appearance.BodyType.AVERAGE
            ),
            true,
            instanceId,
            agentId
        );

        mockMvc.perform(post("/api/v1/characters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.characterId").exists());
    }

    @Test
    void getOrigins_shouldReturnAllOrigins() throws Exception {
        mockMvc.perform(get("/api/v1/characters/origins"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].displayName").exists());
    }

    @Test
    void getSkills_shouldReturnSkillsList() throws Exception {
        mockMvc.perform(get("/api/v1/characters/skills"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].name").exists());
    }
}

