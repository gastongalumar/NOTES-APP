package com.notasapp.dto;

public class LoginResponseDTO {
    private String message;
    private String token;
    private String username;
    private String role;

    // Constructor
    public LoginResponseDTO(String message, String token, String username, String role) {
        this.message = message;
        this.token = token;
        this.username = username;
        this.role = role;
    }

    // Getters
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}