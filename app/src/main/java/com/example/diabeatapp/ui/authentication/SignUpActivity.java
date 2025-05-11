package com.example.diabeatapp.ui.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.diabeatapp.MainActivity;
import com.example.diabeatapp.R;
import com.example.diabeatapp.databinding.ActivitySignUpBinding;
import com.example.diabeatapp.ui.authentication.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private EditText firstNameEditText, lastNameEditText, usernameEditText, phoneEditText, emergencyContactEditText, passwordEditText, insulinSensitivityEditText, heightEditText, weightEditText;
    private Spinner genderSpinner, diabetesTypeSpinner, bloodTypeSpinner;
    private TextView loginLinkText;
    private Button signUpButton;
    private ProgressBar progressBar;
    private FirebaseFirestore databaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        EdgeToEdge.enable(this);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firstNameEditText = binding.etFirstName;
        lastNameEditText = binding.etLastName;
        usernameEditText = binding.etUsername;
        phoneEditText = binding.etPhoneNumber;
        emergencyContactEditText = binding.etEmergencyContact;
        passwordEditText = binding.etPassword;
        genderSpinner = binding.spinnerGender;
        diabetesTypeSpinner = binding.spinnerDiabetesType;
        bloodTypeSpinner = binding.spinnerBloodType;
        insulinSensitivityEditText = binding.etInsulinSensitivity;
        heightEditText = binding.etHeight;
        weightEditText = binding.etWeight;
        loginLinkText = binding.tvLoginLink;
        signUpButton = binding.btnSignUp;
        progressBar = binding.progressBar;
        databaseInstance = FirebaseFirestore.getInstance();

        // Animate the click of the button
        signUpButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false; // Let the click still happen
        });


        // Specify what happens when the sign up button is clicked
        signUpButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            signUpButton.setEnabled(false);

            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String emergencyContact = emergencyContactEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String gender = genderSpinner.getSelectedItem().toString();
            String diabetesType = diabetesTypeSpinner.getSelectedItem().toString();
            String bloodType = bloodTypeSpinner.getSelectedItem().toString();
            String insulinSensitivity = insulinSensitivityEditText.getText().toString().trim();
            String height = heightEditText.getText().toString().trim();
            String weight = weightEditText.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || phone.isEmpty() || emergencyContact.isEmpty() || password.isEmpty() || gender.isEmpty() || diabetesType.isEmpty() || bloodType.isEmpty() || insulinSensitivity.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                signUpButton.setEnabled(true);
                return;
            }

            checkUsernameAndRegister(firstName, lastName, username, phone, emergencyContact, password, gender, diabetesType, bloodType, insulinSensitivity, height, weight);
        });

        // ClickListener for login text
        loginLinkText.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void checkUsernameAndRegister(String firstName, String lastName, String username, String phone, String emergencyContact, String password, String gender, String diabetesType, String bloodType, String insulinSensitivity, String height, String weight) {
        databaseInstance.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(SignUpActivity.this, "Username already exists. Choose another one.", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            signUpButton.setEnabled(true);
                        } else {
                            // Hash password and store user
                            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
                            putData(firstName, lastName, username, phone, emergencyContact, hashedPassword, gender, diabetesType, bloodType, insulinSensitivity, height, weight);
                        }
                    } else {
                        Log.w("FIREBASE_TAG", "Error checking username", task.getException());
                        Toast.makeText(SignUpActivity.this, "Error checking username. Try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void putData(String firstName, String lastName, String username, String phone, String emergencyContact, String hashedPassword, String gender, String diabetesType, String bloodType, String insulinSensitivity, String height, String weight) {
        Map<String, Object> user = new HashMap<>();
        // Create an empty list of rides
        ArrayList<Map<String, Object>> rides = new ArrayList<>();

        user.put("first name", firstName);
        user.put("last name", lastName);
        user.put("username", username);
        user.put("phone", phone);
        user.put("emergency contact", emergencyContact);
        user.put("password", hashedPassword);
        user.put("gender", gender);
        user.put("diabetes type", diabetesType);
        user.put("blood type", bloodType);
        user.put("insulin sensitivity", insulinSensitivity);
        user.put("height", height);
        user.put("weight", weight);

        databaseInstance.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FIREBASE_TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                    Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username); // Save unique identifier
                    editor.apply();

                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("FIREBASE_TAG", "Error adding document", e);
                    Toast.makeText(SignUpActivity.this, "Sign-up failed. Try again.", Toast.LENGTH_LONG).show();
                });
    }
}