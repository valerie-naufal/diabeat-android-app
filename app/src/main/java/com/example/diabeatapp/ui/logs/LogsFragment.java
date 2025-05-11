package com.example.diabeatapp.ui.logs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.diabeatapp.R;
import com.example.diabeatapp.databinding.FragmentDashboardBinding;
import com.example.diabeatapp.databinding.FragmentLogsBinding;
import com.example.diabeatapp.ui.authentication.LoginActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LogsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentLogsBinding binding;
    private String username;
    private CardView foodCard, glucoseCard, insulinCard;

    public LogsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogsFragment newInstance(String param1, String param2) {
        LogsFragment fragment = new LogsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);

        foodCard = binding.cvFood;
        glucoseCard = binding.cvGlucose;
        insulinCard = binding.cvInsulin;

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

        // ClickListener for food card
        foodCard.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FoodActivity.class);
            startActivity(intent);
        });

        // ClickListener for glucose card
        glucoseCard.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), GlucoseActivity.class);
            startActivity(intent);
        });

        // ClickListener for insulin card
        insulinCard.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), InsulinActivity.class);
            startActivity(intent);
        });

        return root;
    }
}