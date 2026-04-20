package com.example.myapplication.network;

import com.example.myapplication.network.dto.NominatimResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Nominatim geocoding API (OpenStreetMap).
 * Free, no API key required.
 * Terms: https://operations.osmfoundation.org/policies/nominatim/
 * — must send a descriptive User-Agent header (handled by the client builder).
 */
public interface NominatimService {

    @Headers("Accept-Language: en")
    @GET("search")
    Call<List<NominatimResult>> searchCities(
            @Query("q")              String query,
            @Query("featuretype")    String featureType,   // "city" filters to cities/towns
            @Query("format")         String format,        // must be "json"
            @Query("addressdetails") int    addressDetails,
            @Query("limit")          int    limit
    );
}
