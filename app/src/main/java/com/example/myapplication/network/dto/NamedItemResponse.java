package com.example.myapplication.network.dto;

/**
 * Returned by GET /api/events/types and GET /api/events/options.
 * Used to resolve a display name (e.g. "Seminar") to its database ID
 * before submitting a CreateEventRequest.
 */
public class NamedItemResponse {
    public Long id;
    public String name;
}
