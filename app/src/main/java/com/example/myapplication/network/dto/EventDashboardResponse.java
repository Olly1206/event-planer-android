package com.example.myapplication.network.dto;

import java.util.List;

public class EventDashboardResponse {
    public long followerCount;
    public int totalCreatedEvents;
    public int upcomingEvents;
    public int draftEvents;
    public int totalParticipantCount;
    public List<EventDashboardItemResponse> events;
}
