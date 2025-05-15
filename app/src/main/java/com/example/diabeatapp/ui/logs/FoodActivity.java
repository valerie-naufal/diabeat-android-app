package com.example.diabeatapp.ui.logs;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diabeatapp.R;
import com.example.diabeatapp.models.FoodItem;
import com.example.diabeatapp.databinding.ActivityFoodBinding;
import com.example.diabeatapp.databinding.RecyclerviewFoodTitleBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FoodActivity extends AppCompatActivity {

    private ActivityFoodBinding activityBinding;
    private RecyclerviewFoodTitleBinding titleBinding;
    private RecyclerView recyclerView;
    private List<FoodItem> foodLogs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        activityBinding = ActivityFoodBinding.inflate(getLayoutInflater());
        titleBinding = RecyclerviewFoodTitleBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        recyclerView = activityBinding.foodLogs;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchFoodLogs();
    }

    private void fetchFoodLogs() {
        String username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", null);
        if (username == null) {
            Toast.makeText(this, "No username found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("food logs")
                .whereEqualTo("username", username)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<FoodItem> foodList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        String mealName = doc.getString("name");
                        String calories = doc.getString("calories");
                        String carbs = doc.getString("carbs");
                        String mealType = doc.getString("meal type");
                        Timestamp timestamp = doc.getTimestamp("timestamp");

                        foodList.add(new FoodItem(calories, carbs, mealType, mealName, timestamp));
                    }
                    recyclerView.setAdapter(new FoodRecyclerViewAdapter(foodList));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch food logs", Toast.LENGTH_SHORT).show();
                });
    }
}