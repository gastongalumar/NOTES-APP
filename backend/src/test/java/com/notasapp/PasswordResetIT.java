package com.notasapp;

import com.notasapp.repository.UsuarioRepository;
import com.notasapp.service.MailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordResetIT {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MailService mailService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void resetsPasswordUsingEmailedToken() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"reset_user","email":"reset_user@example.com","password":"Password123"}
                                """))
                .andExpect(status().isCreated());
        TestUsers.verify(usuarioRepository, "reset_user");

        mvc.perform(post("/api/v1/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"reset_user@example.com"}
                                """))
                .andExpect(status().isAccepted());

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(mailService, org.mockito.Mockito.atLeastOnce())
                .send(org.mockito.ArgumentMatchers.eq("reset_user@example.com"),
                        org.mockito.ArgumentMatchers.anyString(), body.capture());
        String text = body.getAllValues().stream()
                .filter(value -> value.contains("/reset-password?token="))
                .findFirst()
                .orElseThrow();
        String token = text.substring(text.indexOf("token=") + 6).trim();
        assertThat(token).isNotBlank();

        mvc.perform(post("/api/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"NewPassword123"}
                                """.formatted(token)))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"reset_user","password":"NewPassword123"}
                                """))
                .andExpect(status().isOk());
    }
}
