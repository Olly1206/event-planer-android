package com.example.myapplication.network.dto;

import java.util.Set;

/**
 * Request body for POST /api/events.
 * Mirrors the backend's CreateEventRequest exactly.
 */
public class CreateEventRequest {
    public String title;
    public String description;
    /** ISO-8601 LocalDateTime string, e.g. "2026-04-15T09:00:00" */
    public String eventDate;
    /** ISO-8601 end datetime, e.g. "2026-04-15T17:00:00" */
    public String eventEndDate;
    public String locationName;
    /** "INDOOR", "OUTDOOR", or "BOTH" */
    public String locationType;
    public Long venueOsmId;
    public String venueName;
    public String venueAddress;
    public Double venueLat;
    public Double venueLon;
    public String venueCategory;
    public String venueWebsite;
    public String venuePhone;
    public String venueOpeningHours;
    public Integer maxParticipants;
    /** Database ID of the EventType (e.g. Seminar = 1) */
    public Long eventTypeId;
    /** Database IDs of selected EventOptions (e.g. Catering, Music) */
    public Set<Long> optionIds;
}
