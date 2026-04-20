package com.example.myapplication.network.dto;

public class RegisterRequest {
    public String username;
    public String email;
    public String password;
    public String role; // "PRIVATE" or "COMPANY"

    public RegisterRequest(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
