package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Single result returned by the Nominatim geocoding search API.
 * Only the fields we actually use are declared; Gson ignores the rest.
 */
public class NominatimResult {

    /** Human-readable display name, e.g. "Paris, Île-de-France, France" */
    @SerializedName("display_name")
    public String displayName;

    /** The city/town/village name portion, e.g. "Paris" */
    public String name;

    /** ISO 3166-1 alpha-2 country code, e.g. "fr" */
    @SerializedName("country_code")
    public String countryCode;

    /** Returns a short label: "Paris, France" style. */
    @Override
    public String toString() {
        if (displayName == null) return "";
        // Nominatim display_name is comma-delimited — take first and last parts
        String[] parts = displayName.split(",");
        if (parts.length >= 2) {
            return parts[0].trim() + ", " + parts[parts.length - 1].trim();
        }
        return displayName;
    }
}
