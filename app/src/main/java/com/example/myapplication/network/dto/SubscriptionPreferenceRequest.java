package com.example.myapplication.network.dto;

public class SubscriptionPreferenceRequest {
    public Boolean notificationsEnabled;
    public Boolean emailFallbackEnabled;
    public Integer remindBeforeMinutes;

    public SubscriptionPreferenceRequest(Boolean notificationsEnabled,
                                         Boolean emailFallbackEnabled,
                                         Integer remindBeforeMinutes) {
        this.notificationsEnabled = notificationsEnabled;
        this.emailFallbackEnabled = emailFallbackEnabled;
        this.remindBeforeMinutes = remindBeforeMinutes;
    }
}
