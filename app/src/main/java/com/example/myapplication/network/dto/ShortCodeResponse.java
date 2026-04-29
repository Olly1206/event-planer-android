package com.example.myapplication.network.dto;

public class ShortCodeResponse {
    private String shortCode;

    public ShortCodeResponse() {
    }

    public ShortCodeResponse(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
}
