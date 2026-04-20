package com.example.myapplication.network.dto;

/**
 * PATCH request body — all fields optional. Backend ignores nulls.
 */
public class UpdateEventRequest {
    public String title;
    public String description;
    public String eventDate;
    public String eventEndDate;
    public String locationName;
    public String locationType;
    public String visibility;
    public String status;
    public Integer maxParticipants;
}
