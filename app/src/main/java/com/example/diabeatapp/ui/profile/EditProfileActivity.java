package com.example.diabeatapp.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.example.diabeatapp.databinding.ActivityEditProfileBinding;
import com.example.diabeatapp.databinding.ActivitySignUpBinding;
import com.example.diabeatapp.ui.authentication.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private EditText firstNameEditText, lastNameEditText, usernameEditText, phoneEditText, emergencyContactEditText, passwordEditText, insulinSensitivityEditText, heightEditText, weightEditText;
    private Spinner genderSpinner, diabetesTypeSpinner, bloodTypeSpinner, insulinTypeSpinner;
    private Button updateProfileButton;
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

        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firstNameEditText = binding.etFirstName;
        lastNameEditText = binding.etLastName;
        usernameEditText = binding.etUsername;
        phoneEditText = binding.etPhoneNumber;
        emergencyContactEditText = binding.etEmergencyContact;
        genderSpinner = binding.spinnerGender;
        diabetesTypeSpinner = binding.spinnerDiabetesType;
        bloodTypeSpinner = binding.spinnerBloodType;
        insulinTypeSpinner = binding.spinnerInsulinType;
        insulinSensitivityEditText = binding.etInsulinSensitivity;
        heightEditText = binding.etHeight;
        weightEditText = binding.etWeight;
        updateProfileButton = binding.btnUpdateProfile;
        progressBar = binding.progressBar;
        databaseInstance = FirebaseFirestore.getInstance();

        String username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", null);
        if (username == null) {
            Log.e("MainActivity", "Username not found in SharedPreferences");
            return;
        }

        databaseInstance.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(userDocs -> {
                            if (userDocs.isEmpty()) return;

                            DocumentSnapshot userDoc = userDocs.getDocuments().get(0);
                            String firstName = userDoc.getString("first name");
                            String lastName = userDoc.getString("last name");
                            String phone = userDoc.getString("phone");
                            String emergencyContact = userDoc.getString("emergency contact");
                            String gender = userDoc.getString("gender");
                            String diabetesType = userDoc.getString("diabetes type");
                            String bloodType = userDoc.getString("blood type");
                            String insulinType = userDoc.getString("insulin type");
                            String insulinSensitivity = userDoc.getString("insulin sensitivity");
                            String height = userDoc.getString("height");
                            String weight = userDoc.getString("weight");

                            firstNameEditText.setText(firstName);
                            lastNameEditText.setText(lastName);
                            usernameEditText.setText(username);
                            phoneEditText.setText(phone);
                            emergencyContactEditText.setText(emergencyContact);
                            genderSpinner.setSelection(((ArrayAdapter<String>)genderSpinner.getAdapter()).getPosition(gender));
                            diabetesTypeSpinner.setSelection(((ArrayAdapter<String>)diabetesTypeSpinner.getAdapter()).getPosition(diabetesType));
                            bloodTypeSpinner.setSelection(((ArrayAdapter<String>)bloodTypeSpinner.getAdapter()).getPosition(bloodType));
                            insulinTypeSpinner.setSelection(((ArrayAdapter<String>)insulinTypeSpinner.getAdapter()).getPosition(insulinType));
                            insulinSensitivityEditText.setText(insulinSensitivity);
                            heightEditText.setText(height);
                            weightEditText.setText(weight);
                });

        // Animate the click of the button
        updateProfileButton.setOnTouchListener((v, event) -> {
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
        updateProfileButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            updateProfileButton.setEnabled(false);

            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String updatedUsername = usernameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String emergencyContact = emergencyContactEditText.getText().toString().trim();
            String gender = genderSpinner.getSelectedItem().toString();
            String diabetesType = diabetesTypeSpinner.getSelectedItem().toString();
            String bloodType = bloodTypeSpinner.getSelectedItem().toString();
            String insulinType = insulinTypeSpinner.getSelectedItem().toString();
            String insulinSensitivity = insulinSensitivityEditText.getText().toString().trim();
            String height = heightEditText.getText().toString().trim();
            String weight = weightEditText.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || updatedUsername.isEmpty() || phone.isEmpty() || emergencyContact.isEmpty() || gender.isEmpty() || diabetesType.isEmpty() || bloodType.isEmpty() || insulinType.isEmpty() || insulinSensitivity.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(EditProfileActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                updateProfileButton.setEnabled(true);
                return;
            }

            checkUsernameAndUpdate(firstName, lastName, updatedUsername, phone, emergencyContact, gender, diabetesType, bloodType, insulinType, insulinSensitivity, height, weight);
        });
    }

    private void checkUsernameAndUpdate(String firstName, String lastName, String username, String phone, String emergencyContact, String gender, String diabetesType, String bloodType, String insulinType, String insulinSensitivity, String height, String weight) {
        String oldUsername = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", null);

        databaseInstance.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty() && !username.equals(oldUsername)) {
                            Toast.makeText(EditProfileActivity.this, "Username already exists. Choose another one.", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            updateProfileButton.setEnabled(true);
                        } else {
                            updateData(firstName, lastName, username, phone, emergencyContact, gender, diabetesType, bloodType, insulinType, insulinSensitivity, height, weight);
                        }
                    } else {
                        Log.w("FIREBASE_TAG", "Error checking username", task.getException());
                        Toast.makeText(EditProfileActivity.this, "Error checking username. Try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateData(String firstName, String lastName, String username, String phone, String emergencyContact, String gender, String diabetesType, String bloodType, String insulinType, String insulinSensitivity, String height, String weight) {
        Map<String, Object> user = new HashMap<>();
        user.put("first name", firstName);
        user.put("last name", lastName);
        user.put("username", username);
        user.put("phone", phone);
        user.put("emergency contact", emergencyContact);
        user.put("gender", gender);
        user.put("diabetes type", diabetesType);
        user.put("blood type", bloodType);
        user.put("insulin type", insulinType);
        user.put("insulin sensitivity", insulinSensitivity);
        user.put("height", height);
        user.put("weight", weight);

        // Find the document by username and update it
        databaseInstance.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        databaseInstance.collection("users")
                                .document(docId)
                                .update(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("username", username);
                                    editor.putString("insulin type", insulinType);
                                    editor.apply();

                                    startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FIREBASE_TAG", "Update failed", e);
                                    Toast.makeText(EditProfileActivity.this, "Failed to update profile. Try again.", Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Toast.makeText(EditProfileActivity.this, "User not found.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_TAG", "Query failed", e);
                    Toast.makeText(EditProfileActivity.this, "Something went wrong. Try again.", Toast.LENGTH_LONG).show();
                });
    }
}