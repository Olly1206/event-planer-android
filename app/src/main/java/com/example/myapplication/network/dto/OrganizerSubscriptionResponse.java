package com.example.myapplication.network.dto;

public class OrganizerSubscriptionResponse {
    public Long id;
    public Long organiserId;
    public String organiserUsername;
    public Boolean notificationsEnabled;
    public Boolean emailFallbackEnabled;
    public Integer remindBeforeMinutes;
    public String createdAt;
}
