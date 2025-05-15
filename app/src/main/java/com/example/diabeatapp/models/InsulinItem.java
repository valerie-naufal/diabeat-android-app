package com.example.diabeatapp.models;

import com.google.firebase.Timestamp;

public class InsulinItem {
    public String insulinDose;
    public String insulinType;
    public String timestamp;

    public InsulinItem(String insulinDose, String insulinType, String timestamp) {
        this.insulinDose = insulinDose;
        this.insulinType = insulinType;
        this.timestamp = timestamp;
    }
}
