package com.notasapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notasapp.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttachmentFlowIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void uploadsAndDownloadsLocalAttachment() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"file_user","email":"file_user@example.com","password":"Password123"}
                                """))
                .andExpect(status().isCreated());
        String token = login("file_user");
        JsonNode note = json(mvc.perform(post("/api/v1/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Con archivo","contenido":"x","prioridad":2,"estado":"PENDIENTE"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse());

        byte[] png = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", png);
        mvc.perform(multipart("/api/v1/notes/" + note.get("id").asLong() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/notes/" + note.get("id").asLong() + "/attachments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(auditLogRepository.countByAccion("ATTACHMENT_UPLOAD")).isGreaterThanOrEqualTo(1);
    }

    private String login(String username) throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Password123"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        return json(response).get("accessToken").asText();
    }

    private JsonNode json(MockHttpServletResponse response) throws Exception {
        return objectMapper.readTree(response.getContentAsString());
    }
}
