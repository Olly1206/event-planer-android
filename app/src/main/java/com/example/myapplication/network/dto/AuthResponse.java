package com.example.myapplication.network.dto;

public class AuthResponse {
    public String token;
    public Long userId;
    public String username;
    public String email;
    public String role; // "PRIVATE", "COMPANY", or "ADMIN"
}
