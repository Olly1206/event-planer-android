package com.example.myapplication.network.dto;

import java.util.List;
import java.util.Set;

public class EventResponse {
    public Long id;
    public String title;
    public String description;
    public String eventDate;         // ISO-8601 string from backend
    public String eventEndDate;      // ISO-8601 string — null if open-ended
    public String locationName;
    public String locationType;      // "INDOOR", "OUTDOOR", "BOTH"
    public String status;            // "DRAFT", "PLANNED", "ONGOING", "COMPLETED", "CANCELLED"
    public Integer maxParticipants;
    public int currentParticipantCount;
    public Long organiserId;
    public String organiserUsername;
    public String eventTypeName;
    public Set<String> selectedOptions;
    public List<VendorResponse> selectedVendors;
    public String visibility;       // "PUBLIC" or "PRIVATE"
    public String createdAt;
    public String inviteToken;       // only present for organiser/admins
    public Boolean isAdmin;         // true if current user is an admin of this event
}
