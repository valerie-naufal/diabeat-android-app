package com.example.diabeatapp.models;

import com.google.firebase.Timestamp;

public class GlucoseItem {
    public String glucoseLevel;
    public String timestamp;

    public GlucoseItem(String glucoseLevel, String timestamp) {
        this.glucoseLevel = glucoseLevel;
        this.timestamp = timestamp;
    }
}
