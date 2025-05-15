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
import com.example.diabeatapp.databinding.ActivityInsulinBinding;
import com.example.diabeatapp.databinding.RecyclerviewInsulinTitleBinding;
import com.example.diabeatapp.models.InsulinItem;
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

public class InsulinActivity extends AppCompatActivity {

    private ActivityInsulinBinding activityBinding;
    private RecyclerviewInsulinTitleBinding titleBinding;
    private RecyclerView recyclerView;
    private List<InsulinItem> insulinLogs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        activityBinding = ActivityInsulinBinding.inflate(getLayoutInflater());
        titleBinding = RecyclerviewInsulinTitleBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        recyclerView = activityBinding.foodLogs;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchInsulinLogs();
    }

    private void fetchInsulinLogs() {
        String username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", null);
        if (username == null) {
            Toast.makeText(this, "No username found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("insulin logs")
                .whereEqualTo("username", username)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<InsulinItem> insulinList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        String insulin = doc.getString("insulinDose");
                        double insulinAsDouble = Double.parseDouble(insulin);
                        String formattedInsulin = String.format("%.2f", insulinAsDouble) + " units";

                        String insulinType = doc.getString("type");

                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        Date date = timestamp.toDate();
                        String formattedDate = new SimpleDateFormat("h:mm a MMM d, yyyy", Locale.getDefault()).format(date);

                        insulinList.add(new InsulinItem(formattedInsulin, insulinType, formattedDate));
                    }
                    recyclerView.setAdapter(new InsulinRecyclerViewAdapter(insulinList));
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to fetch insulin logs", Toast.LENGTH_SHORT).show();
                });
    }
}