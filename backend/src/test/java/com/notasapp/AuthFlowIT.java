package com.notasapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notasapp.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void registerLoginRefreshAndLogout() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"tester","email":"tester@example.com","password":"Password123","nombre":"Test"}
                                """))
                .andExpect(status().isCreated());
        TestUsers.verify(usuarioRepository, "tester");

        MockHttpServletResponse login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"tester","password":"Password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        JsonNode loginBody = objectMapper.readTree(login.getContentAsString());
        String accessToken = loginBody.get("accessToken").asText();
        var refreshCookie = login.getCookie("notasapp_rt");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.isHttpOnly()).isTrue();

        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        MockHttpServletResponse refreshed = mvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertThat(refreshed.getCookie("notasapp_rt")).isNotNull();

        mvc.perform(post("/api/v1/auth/logout").cookie(refreshCookie))
                .andExpect(status().isNoContent());
    }
}
