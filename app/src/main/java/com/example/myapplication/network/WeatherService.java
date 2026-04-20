package com.example.myapplication.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("data/2.5/forecast")
    Call<WeatherResponse> getForecast(
        @Query("q") String cityName,
        @Query("units") String units,
        @Query("appid") String apiKey
    );
}