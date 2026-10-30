package com.notasapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notasapp.repository.UsuarioRepository;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TwoFactorLifecycleIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void protectsSetupConsumesRecoveryCodesAndAllowsDisable() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"totp_lifecycle","email":"totp_lifecycle@example.com","password":"Password123"}
                                """))
                .andExpect(status().isCreated());
        TestUsers.verify(usuarioRepository, "totp_lifecycle");
        String access = login("""
                {"username":"totp_lifecycle","password":"Password123"}
                """);

        mvc.perform(get("/api/v1/auth/2fa/status")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        JsonNode setup = json(mvc.perform(post("/api/v1/auth/2fa/setup")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andReturn().getResponse());
        String secret = setup.get("secret").asText();
        String code = new DefaultCodeGenerator().generate(secret, System.currentTimeMillis() / 1000 / 30);

        JsonNode verify = json(mvc.perform(post("/api/v1/auth/2fa/verify")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s"}
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andReturn().getResponse());
        String recovery = verify.get("recoveryCodes").get(0).asText();

        mvc.perform(get("/api/v1/auth/2fa/status")
                        .header("Authorization", "Bearer " + access))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.recoveryCodesRemaining").value(8));

        mvc.perform(post("/api/v1/auth/2fa/setup")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TWO_FACTOR_ALREADY_ENABLED"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"totp_lifecycle","password":"Password123","totp":"%s"}
                                """.formatted(recovery)))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"totp_lifecycle","password":"Password123","totp":"%s"}
                                """.formatted(recovery)))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/auth/2fa/status")
                        .header("Authorization", "Bearer " + access))
                .andExpect(jsonPath("$.recoveryCodesRemaining").value(7));

        mvc.perform(post("/api/v1/auth/2fa/disable")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"Incorrecta123"}
                                """))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/auth/2fa/disable")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"Password123"}
                                """))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"totp_lifecycle","password":"Password123"}
                                """))
                .andExpect(status().isOk());
    }

    private String login(String body) throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        return json(response).get("accessToken").asText();
    }

    private JsonNode json(MockHttpServletResponse response) throws Exception {
        return objectMapper.readTree(response.getContentAsString());
    }
}
