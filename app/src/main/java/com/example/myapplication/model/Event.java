package com.example.myapplication.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Serializable {
    private String name;
    private String category; // Company or Private
    private String type;     // Seminar, Party, etc.
    private String date;
    private List<String> options;

    public Event(String name, String category, String type, String date) {
        this.name = name;
        this.category = category;
        this.type = type;
        this.date = date;
        this.options = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}