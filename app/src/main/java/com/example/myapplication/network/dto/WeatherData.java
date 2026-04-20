package com.example.myapplication.network.dto;

/**
 * Returned by GET /api/weather?city=X&date=yyyy-MM-dd.
 * The backend proxies OpenWeatherMap — the API key is never on the device.
 */
public class WeatherData {
    public String city;
    public String date;
    public double temperature;
    public int humidity;
    public String description;
    public String icon;
}
