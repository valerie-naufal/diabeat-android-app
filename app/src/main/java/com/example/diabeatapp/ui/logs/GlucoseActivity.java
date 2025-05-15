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
import com.example.diabeatapp.databinding.ActivityGlucoseBinding;
import com.example.diabeatapp.databinding.RecyclerviewGlucoseTitleBinding;
import com.example.diabeatapp.models.GlucoseItem;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GlucoseActivity extends AppCompatActivity {

    private ActivityGlucoseBinding activityBinding;
    private RecyclerviewGlucoseTitleBinding titleBinding;
    private RecyclerView recyclerView;
    private List<FoodItem> glucoseLogs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        activityBinding = ActivityGlucoseBinding.inflate(getLayoutInflater());
        titleBinding = RecyclerviewGlucoseTitleBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        recyclerView = activityBinding.foodLogs;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchGlucoseLogs();
    }

    private void fetchGlucoseLogs() {
        String username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", null);
        if (username == null) {
            Toast.makeText(this, "No username found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("glucose logs")
                .whereEqualTo("username", username)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<GlucoseItem> glucoseList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        String glucose = doc.getString("glucose");
                        double glucoseAsDouble = Double.parseDouble(glucose);
                        String formattedGlucose = String.format("%.2f", glucoseAsDouble) + " mg/dL";

                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        Date date = timestamp.toDate();
                        String formattedDate = new SimpleDateFormat("h:mm a MMM d, yyyy", Locale.getDefault()).format(date);

                        glucoseList.add(new GlucoseItem(formattedGlucose, formattedDate));
                    }
                    recyclerView.setAdapter(new GlucoseRecyclerViewAdapter(glucoseList));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch glucose logs", Toast.LENGTH_SHORT).show();
                });
    }
}