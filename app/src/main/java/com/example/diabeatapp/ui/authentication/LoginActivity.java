package com.example.diabeatapp.ui.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.diabeatapp.MainActivity;
import com.example.diabeatapp.R;
import com.example.diabeatapp.databinding.ActivityLoginBinding;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.Transaction;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import at.favre.lib.crypto.bcrypt.BCrypt;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView signUpLinkText;
    private Button loginButton;
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

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*getSupportActionBar().hide();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);*/

        usernameEditText = binding.etUsername;
        passwordEditText = binding.etPassword;
        signUpLinkText = binding.tvSignUpLink;
        loginButton = binding.btnLogin;
        progressBar = binding.progressBar;
        databaseInstance = FirebaseFirestore.getInstance();

        // Animate the click of the button
        loginButton.setOnTouchListener((v, event) -> {
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


        // Specify what happens when the login button is clicked
        loginButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);

            String enteredUsername = usernameEditText.getText().toString().trim();
            String enteredPassword = passwordEditText.getText().toString().trim();

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                return;
            }

            // Check Firestore for matching username
            databaseInstance.collection("users")
                    .whereEqualTo("username", enteredUsername)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Username not found.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                        } else {
                            // Since username is unique, get the first document
                            String storedHashedPassword = queryDocumentSnapshots.getDocuments().get(0).getString("password");

                            BCrypt.Result result = BCrypt.verifyer().verify(enteredPassword.toCharArray(), storedHashedPassword);

                            if (result.verified) {
                                databaseInstance.collection("users")
                                        .whereEqualTo("username", enteredUsername)
                                        .get()
                                        .addOnSuccessListener(userDocs -> {
                                            if (userDocs.isEmpty()) return;

                                            DocumentSnapshot userDoc = userDocs.getDocuments().get(0);
                                            String insulinType = userDoc.getString("insulin type");

                                            // Login successful
                                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("username", enteredUsername); // Save unique identifier
                                            editor.putString("insulin type", insulinType);
                                            editor.apply();
                                });

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                // Password incorrect
                                Toast.makeText(LoginActivity.this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                loginButton.setEnabled(true);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIREBASE_LOGIN", "Error logging in: ", e);
                        Toast.makeText(LoginActivity.this, "Login failed. Try again.", Toast.LENGTH_SHORT).show();
                    });
        });

        // ClickListener for login text
        signUpLinkText.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            // Finish the LoginActivity
            //finish();
        });
    }

    public void goBack(View view) {
        finish(); // Closes the activity when the back button is clicked
    }

    /*@NonNull
    @Override
    public OnBackInvokedDispatcher getOnBackInvokedDispatcher() {
        return super.getOnBackInvokedDispatcher();
    }*/
}