package com.example.diabeatapp.ui.dashboard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.diabeatapp.databinding.FragmentDashboardBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private String username;
    private long lastUpdateTime  = 0; //in milliseconds

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel homeViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);

        if (username != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        if (!querySnapshots.isEmpty()) {
                            DocumentSnapshot document = querySnapshots.getDocuments().get(0);

                            String firstName = document.getString("first name");
                            String lastName = document.getString("last name");

                            binding.tvName.setText("Hi, " + firstName + " " + lastName);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to load profile info.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        TextView glucoseTextView = binding.tvGlucoseLevel;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("glucose logs")
                .whereEqualTo("username", username)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Failed to load glucose level.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        long now = System.currentTimeMillis();
                        if (now - lastUpdateTime < 1000) return; // throttle to 0.5 seconds
                        lastUpdateTime = now;

                        String glucoseValue = snapshots.getDocuments().get(0).getString("glucose");
                        double glucoseValueAsDouble = Double.parseDouble(glucoseValue);
                        if (glucoseValue != null && glucoseValueAsDouble > 60) {
                            try {
                                double glucoseDouble = Double.parseDouble(glucoseValue);
                                String formattedGlucose = String.format("%.2f", glucoseDouble);
                                glucoseTextView.setText(formattedGlucose);
                            } catch (NumberFormatException ex) {
                                glucoseTextView.setText(glucoseValue);
                            }
                            Log.d("DashboardFragment", "Loaded glucose level: " + glucoseValue);
                        }
                    }
                });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}