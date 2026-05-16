package com.example.myapplication.network;

import com.example.myapplication.network.dto.AuthResponse;
import com.example.myapplication.network.dto.CreateEventRequest;
import com.example.myapplication.network.dto.EventDashboardResponse;
import com.example.myapplication.network.dto.EventParticipantResponse;
import com.example.myapplication.network.dto.EventResponse;
import com.example.myapplication.network.dto.GuestAuthResponse;
import com.example.myapplication.network.dto.LoginRequest;
import com.example.myapplication.network.dto.NamedItemResponse;
import com.example.myapplication.network.dto.OrganizerSubscriptionResponse;
import com.example.myapplication.network.dto.RegisterRequest;
import com.example.myapplication.network.dto.SaveEventVendorRequest;
import com.example.myapplication.network.dto.ShortCodeResponse;
import com.example.myapplication.network.dto.SubscriptionPreferenceRequest;
import com.example.myapplication.network.dto.UpdateEventRequest;
import com.example.myapplication.network.dto.VendorResponse;
import com.example.myapplication.network.dto.VenueResponse;
import com.example.myapplication.network.dto.WeatherData;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ── Auth ───────────────────────────────────────────────────────────────────

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/guest")
    Call<GuestAuthResponse> loginAsGuest();

    @DELETE("api/users/me")
    Call<Void> deleteCurrentUser();

    // ── Events ─────────────────────────────────────────────────────────────────

    @POST("api/events")
    Call<EventResponse> createEvent(@Body CreateEventRequest request);

    @GET("api/events")
    Call<List<EventResponse>> getAllEvents();

    @GET("api/events/{id}")
    Call<EventResponse> getEventById(@Path("id") Long id);

    @GET("api/events/search")
    Call<List<EventResponse>> searchEvents(@Query("keyword") String keyword);

    @GET("api/events/filter")
    Call<List<EventResponse>> filterEvents(
            @Query("keyword") String keyword,
            @Query("city") String city,
            @Query("eventTypeId") Long eventTypeId,
            @Query("organiserId") Long organiserId,
            @Query("from") String from,
            @Query("to") String to);

    @GET("api/events/organiser/{organiserId}")
    Call<List<EventResponse>> getMyEvents(@Path("organiserId") Long organiserId);

    @GET("api/events/organiser/{organiserId}/dashboard")
    Call<EventDashboardResponse> getOrganiserDashboard(@Path("organiserId") Long organiserId);

    @GET("api/events/organiser/{organiserId}/calendar.ics")
    Call<ResponseBody> exportOrganiserCalendar(@Path("organiserId") Long organiserId);

    @GET("api/events/my")
    Call<List<EventResponse>> getCreatedEvents();

    @DELETE("api/events/{id}")
    Call<Void> deleteEvent(@Path("id") Long id);

    @PATCH("api/events/{id}")
    Call<EventResponse> updateEvent(@Path("id") Long id, @Body UpdateEventRequest request);

    @POST("api/events/{id}/join")
    Call<Void> joinEvent(@Path("id") Long id);

    @DELETE("api/events/{id}/leave")
    Call<Void> leaveEvent(@Path("id") Long id);

    @GET("api/events/{id}/participants")
    Call<List<EventParticipantResponse>> getParticipants(@Path("id") Long id);

    @GET("api/events/{id}/participants.csv")
    Call<ResponseBody> exportParticipantsCsv(@Path("id") Long id);

    @GET("api/events/{id}/calendar.ics")
    Call<ResponseBody> exportEventCalendar(@Path("id") Long id);

    @POST("api/events/{id}/vendors")
    Call<VendorResponse> addVendorToEvent(@Path("id") Long id, @Body SaveEventVendorRequest request);

    @DELETE("api/events/{id}/vendors/{osmId}")
    Call<Void> removeVendorFromEvent(@Path("id") Long id, @Path("osmId") Long osmId);

    // ── Invite link ────────────────────────────────────────────────────────────

    /** Public preview — no auth required */
    @GET("api/events/invite/{token}")
    Call<EventResponse> previewByToken(@Path("token") String token);

    /** Join an event via its invite token */
    @POST("api/events/invite/{token}/join")
    Call<Void> joinByToken(@Path("token") String token);

    /** Get the raw invite token (organiser/admin only) */
    @GET("api/events/{id}/invite-link")
    Call<String> getInviteLink(@Path("id") Long id);

    /** Get a short code for the invite token (organiser/admin only) — WAF-safe sharing */
    @GET("api/events/{id}/invite-link/short")
    Call<ShortCodeResponse> getShortInviteCode(@Path("id") Long id);

    /** Grant admin rights to a user by username */
    @POST("api/events/{id}/admins/{username}")
    Call<Void> addAdmin(@Path("id") Long id, @Path("username") String username);

    /** Revoke admin rights from a user */
    @DELETE("api/events/{id}/admins/{adminUserId}")
    Call<Void> removeAdmin(@Path("id") Long id, @Path("adminUserId") Long adminUserId);

    /** All events the current user has joined as a participant */
    @GET("api/events/joined")
    Call<List<EventResponse>> getJoinedEvents();

    @GET("api/events/joined/calendar.ics")
    Call<ResponseBody> exportJoinedCalendar();

    @GET("api/events/subscribed")
    Call<List<EventResponse>> getSubscribedEvents();

    @GET("api/events/subscriptions")
    Call<List<OrganizerSubscriptionResponse>> getSubscriptions();

    @POST("api/events/subscriptions/organisers/{organiserId}")
    Call<OrganizerSubscriptionResponse> subscribeToOrganiser(
            @Path("organiserId") Long organiserId,
            @Body SubscriptionPreferenceRequest request);

    @DELETE("api/events/subscriptions/organisers/{organiserId}")
    Call<Void> unsubscribeFromOrganiser(@Path("organiserId") Long organiserId);

    // ── Venues ──────────────────────────────────────────────────────────────────

    @GET("api/venues")
    Call<List<VenueResponse>> getVenues(
            @Query("city") String city,
            @Query("radiusMeters") int radiusMeters,
            @Query("locationType") String locationType,
            @Query("eventType") String eventType);

    // ── Reference data (public, no auth needed) ────────────────────────────────

    @GET("api/events/types")
    Call<List<NamedItemResponse>> getEventTypes();

    @GET("api/events/options")
    Call<List<NamedItemResponse>> getEventOptions();

    // ── Weather (proxied through backend) ──────────────────────────────────────

    /** Single-day forecast. */
    @GET("api/weather")
    Call<WeatherData> getWeather(@Query("city") String city, @Query("date") String date);

    /** Multi-day forecast — one entry per day between startDate and endDate. */
    @GET("api/weather/range")
    Call<List<WeatherData>> getWeatherRange(
            @Query("city") String city,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);

    // ── Vendors ────────────────────────────────────────────────────────────────

    @GET("api/vendors")
    Call<List<VendorResponse>> getVendors(
            @Query("city") String city,
            @Query("radiusMeters") int radiusMeters,
            @Query("optionName") List<String> optionNames);
}
