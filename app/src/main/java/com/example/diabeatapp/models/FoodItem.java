package com.example.diabeatapp.models;

import com.google.firebase.Timestamp;

public class FoodItem {
    public String calories;
    public String carbs;
    public String mealType;
    public String mealName;
    public Timestamp timestamp;

    public FoodItem(String calories, String carbs, String mealType, String mealName, Timestamp timestamp) {
        this.calories = calories;
        this.carbs = carbs;
        this.mealType = mealType;
        this.mealName = mealName;
        this.timestamp = timestamp;
    }
}
