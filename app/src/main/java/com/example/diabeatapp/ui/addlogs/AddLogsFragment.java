package com.example.diabeatapp.ui.addlogs;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.diabeatapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddLogsFragment extends Fragment {

    private Spinner spinnerMealType;
    private EditText etMealName, etCarbs, etCalories;
    private Button btnAddMeal;
    private FirebaseFirestore db;

    public AddLogsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_logs, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get UI components
        spinnerMealType = view.findViewById(R.id.spinner_meal_type);
        etMealName = view.findViewById(R.id.et_meal_name);
        etCarbs = view.findViewById(R.id.et_carbs);
        etCalories = view.findViewById(R.id.et_calories);
        btnAddMeal = view.findViewById(R.id.btn_add_meal);

        // Set click listener
        btnAddMeal.setOnClickListener(v -> addMealLog());

        return view;
    }

    private void addMealLog() {
        String mealType = spinnerMealType.getSelectedItem().toString().trim();
        String mealName = etMealName.getText().toString().trim();
        String carbs = etCarbs.getText().toString().trim();
        String calories = etCalories.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(mealName) || TextUtils.isEmpty(carbs) || TextUtils.isEmpty(calories)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get username from SharedPreferences
        String username = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", null);

        // Create log object
        Map<String, Object> mealLog = new HashMap<>();
        mealLog.put("meal type", mealType);
        mealLog.put("name", mealName);
        mealLog.put("carbs", carbs);
        mealLog.put("calories", calories);
        mealLog.put("timestamp", Timestamp.now());
        mealLog.put("username", username);

        // Add to Firestore
        db.collection("food logs")
                .add(mealLog)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getContext(), "Meal logged successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to log meal", Toast.LENGTH_SHORT).show());
    }
}