package com.notasapp;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmailVerificationRequiredIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void blocksLoginUntilEmailIsVerified() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"unverified_user","email":"unverified_user@example.com","password":"Password123"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"unverified_user","password":"Password123"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"))
                .andExpect(jsonPath("$.email").value("unverified_user@example.com"));

        mvc.perform(post("/api/v1/auth/email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"unverified_user@example.com"}
                                """))
                .andExpect(status().isAccepted());

        mvc.perform(post("/api/v1/auth/email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nadie@example.com"}
                                """))
                .andExpect(status().isAccepted());
    }

    @Test
    void changingEmailRequiresVerificationAgain() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"email_change_user","email":"email_change_user@example.com","password":"Password123"}
                                """))
                .andExpect(status().isCreated());
        TestUsers.verify(usuarioRepository, "email_change_user");

        MockHttpServletResponse login = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"email_change_user","password":"Password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        String access = objectMapper.readTree(login.getContentAsString()).get("accessToken").asText();

        mvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"email_change_user_new@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email_change_user_new@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(false));
    }
}
