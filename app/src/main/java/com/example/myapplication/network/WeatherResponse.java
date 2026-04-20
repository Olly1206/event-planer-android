package com.example.myapplication.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("list")
    public List<ForecastItem> forecastList;

    public static class ForecastItem {
        @SerializedName("dt_txt")
        public String dateTime;

        @SerializedName("main")
        public MainData main;

        @SerializedName("weather")
        public List<WeatherDescription> weather;
    }

    public static class MainData {
        @SerializedName("temp")
        public double temp;
        @SerializedName("humidity")
        public int humidity;
    }

    public static class WeatherDescription {
        @SerializedName("description")
        public String description;
        @SerializedName("icon")
        public String icon;
    }
}