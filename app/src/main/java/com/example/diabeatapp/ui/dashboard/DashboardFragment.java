package com.example.diabeatapp.ui.dashboard;

import android.content.SharedPreferences;
import android.os.Bundle;
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

        final TextView textView = binding.tvUpdates;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}