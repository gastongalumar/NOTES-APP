package com.notasapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteFlowIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void notesCategoriesTagsAndOwnership() throws Exception {
        String alice = registerAndLogin("alice_flow", "alice_flow@example.com");
        String bob = registerAndLogin("bob_flow", "bob_flow@example.com");

        JsonNode category = json(mvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Trabajo","color":"#a3122a","descripcion":"Laburo"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse());
        JsonNode tag = json(mvc.perform(post("/api/v1/tags")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"urgente"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse());

        JsonNode created = json(mvc.perform(post("/api/v1/notes")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo":"Primera nota",
                                  "contenido":"Contenido de prueba",
                                  "categoriaIds":[%d],
                                  "tagIds":[%d],
                                  "prioridad":1,
                                  "estado":"PENDIENTE"
                                }
                                """.formatted(category.get("id").asLong(), tag.get("id").asLong())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Primera nota"))
                .andReturn().getResponse());

        long noteId = created.get("id").asLong();
        long version = created.get("version").asLong();

        mvc.perform(get("/api/v1/notes").header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Primera nota"));

        mvc.perform(get("/api/v1/notes/" + noteId).header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias[0].nombre").value("Trabajo"));

        mvc.perform(put("/api/v1/notes/" + noteId)
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo":"Nota editada",
                                  "contenido":"Otro contenido",
                                  "categoriaIds":[%d],
                                  "tagIds":[%d],
                                  "prioridad":2,
                                  "estado":"EN_CURSO",
                                  "version":%d
                                }
                                """.formatted(category.get("id").asLong(), tag.get("id").asLong(), version)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Nota editada"));

        mvc.perform(get("/api/v1/notes/" + noteId).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/v1/notes/" + noteId).header("Authorization", "Bearer " + alice))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/notes/" + noteId).header("Authorization", "Bearer " + alice))
                .andExpect(status().isNotFound());
    }

    private String registerAndLogin(String username, String email) throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","password":"Password123","nombre":"Test"}
                                """.formatted(username, email)))
                .andExpect(status().isCreated());
        MockHttpServletResponse login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Password123"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        return json(login).get("accessToken").asText();
    }

    private JsonNode json(MockHttpServletResponse response) throws Exception {
        assertThat(response.getContentAsString()).isNotBlank();
        return objectMapper.readTree(response.getContentAsString());
    }
}
