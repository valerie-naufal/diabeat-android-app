package com.example.diabeatapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.diabeatapp.bluetooth.BluetoothServiceGlucose;
import com.example.diabeatapp.bluetooth.BluetoothServiceInsulin;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.diabeatapp.databinding.ActivityMainBinding;

import java.util.Set;
import java.util.UUID;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final UUID WRIST_PI_UUID = UUID.fromString("0000110a-0000-1000-8000-00805F9B34FB");
    private static final UUID BELT_PI_UUID = UUID.fromString("0000110a-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice raspberryPiDevice;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 100;
    private boolean canSendDosage = true;
    private String glucoseLevel;
    private String bloodSugarLevel;
    private String pumpDurationMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Request Bluetooth permission if needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            return; // wait for user response
        }

        initializeBluetoothConnection();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setItemIconTintList(null); // preserve original icon colors
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_logs, R.id.navigation_add_logs, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void initializeBluetoothConnection() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is not enabled.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth permission not granted. Please enable it in settings.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Wrist band sending glucose value (from Pi to Android device)
        Set<BluetoothDevice> pairedWristDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedWristDevice) {

            if (device.getName().equals("PicoW Sensor")) {
                raspberryPiDevice = device;
                break;
            }
        }

        if (raspberryPiDevice == null) {
            Toast.makeText(this, "Wrist Pi not paired. Pair it first.", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothServiceGlucose.getInstance().connectToDevice(raspberryPiDevice, WRIST_PI_UUID, this, new BluetoothServiceGlucose.ConnectionCallback() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected to Wrist Pi", Toast.LENGTH_SHORT).show());

                BluetoothServiceGlucose.getInstance().listenForData(data -> {
                    int sensorValue = (!data.contains(".") && !data.isEmpty()) ? Integer.parseInt(data) : 0;
                    Log.d("MainActivity", "Received sensor value: " + sensorValue);

                    String username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", null);
                    if (username == null) {
                        Log.e("MainActivity", "Username not found in SharedPreferences");
                        return;
                    }

                    String insulinType = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("insulin type", null);
                    if (insulinType == null) {
                        Log.e("MainActivity", "Insulin type not found in SharedPreferences");
                        return;
                    }

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnSuccessListener(userDocs -> {
                                if (userDocs.isEmpty()) return;

                                DocumentSnapshot userDoc = userDocs.getDocuments().get(0);
                                double weight = Double.parseDouble(userDoc.getString("weight"));
                                double tdd = weight * 0.3;

                                db.collection("food logs")
                                        .whereEqualTo("username", username)
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener(foodDocs -> {
                                            if (foodDocs.isEmpty()) return;

                                            DocumentSnapshot foodDoc = foodDocs.getDocuments().get(0);
                                            double carbs = Double.parseDouble(foodDoc.getString("carbs"));

                                            // Calculate glucose
                                            double glucose = Math.abs((-2.65) * Math.pow(10, -5) * sensorValue * sensorValue + 0.0814 * sensorValue + 58.02);
                                            Log.d("MainActivity", "Glucose = " + glucose);

                                            // Always log glucose
                                            Map<String, Object> glucoseLog = new HashMap<>();
                                            glucoseLog.put("username", username);
                                            glucoseLog.put("glucose", String.valueOf(glucose));
                                            glucoseLog.put("timestamp", new Date());
                                            db.collection("glucose logs").add(glucoseLog);

                                            if (!canSendDosage) {
                                                // Only glucose is logged
                                                return;
                                            }

                                            // Now handle insulin + pump
                                            double insulinDose = (carbs / (500 / tdd)) + ((glucose - 100) / (1800 / tdd));
                                            int pumpDurationMs = (int) (insulinDose * 100);

                                            Log.d("MainActivity", "Insulin Dose = " + insulinDose + ", Duration = " + pumpDurationMs);

                                            // Log insulin dose
                                            Map<String, Object> insulinLog = new HashMap<>();
                                            insulinLog.put("username", username);
                                            insulinLog.put("insulinDose", String.valueOf(insulinDose));
                                            insulinLog.put("type", "bolus");
                                            insulinLog.put("timestamp", new Date());
                                            db.collection("insulin logs").add(insulinLog);

                                            if (pumpDurationMs > 300 && BluetoothServiceInsulin.getInstance().isConnected()) {
                                                canSendDosage = false;
                                                BluetoothServiceInsulin.getInstance().sendPumpDuration(String.valueOf(pumpDurationMs));
                                                Log.d("MainActivity", "Sending dosage to pump: " + pumpDurationMs);

                                                long delay = 43200000;
                                                if (insulinType.equalsIgnoreCase("Short acting")) {
                                                    delay = 14400000;
                                                } else if (insulinType.equalsIgnoreCase("Intermediate acting")) {
                                                    delay = 43200000;
                                                } else if (insulinType.equalsIgnoreCase("Long acting")) {
                                                    delay = 86400000;
                                                }

                                                Log.d("MainActivity", "Insulin type:" + insulinType + " ,delay = " + delay);

                                                // Delay before allowing another dose
                                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                    canSendDosage = true;
                                                    Log.d("MainActivity", "Ready to send next dosage");
                                                    // We set a timer/delay of 43,200,000 milliseconds = 12 hours so that
                                                    // the user does not get injected with insulin too soon and risk
                                                    // insulin stacking which could lead to hypoglycemia. The timer/delay
                                                    // we set is based off of the type of insulin being used and in this
                                                    // case it is intermediate-acting insulin. For short-acting insulin
                                                    // the timer/delay would be 14,400,000 milliseconds = 4 hours, and
                                                    // for long-acting insulin the timer/delay would be 86,400,000 = 24
                                                    // hours and in some cases can be 42 hours.
                                                }, delay); // 12 hours
                                            } else {
                                                canSendDosage = true; // allow retry next time
                                            }
                                        });
                            });
                });
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected from Wrist Pi", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Wrist connection error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        // Insulin pump receiving dosage value (from Android device to Pi)
        Set<BluetoothDevice> pairedBeltDevice = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        BluetoothDevice insulinDevice = null;
        for (BluetoothDevice device : pairedBeltDevice) {
            if (device.getName().equals("PicoW-Belt")) {
                insulinDevice = device;
                break;
            }
        }

        BluetoothServiceInsulin.getInstance().connectToDevice(insulinDevice, BELT_PI_UUID, this, new BluetoothServiceGlucose.ConnectionCallback() {
            @Override
            public void onConnected() {
                //new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d("MainActivity", "Connected to PicoW-Belt");
                // The dosage is hard-coded for the time being until we configure the GlucoseML model
                //BluetoothServiceInsulin.getInstance().sendPumpDuration("1000");
                //}, 5000); // 5-second delay
                //Log.d("MainActivity", "Connected to PicoW-Belt");
                // No sending here. We wait for calculated values from glucose.
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected from Belt Pi", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Belt connection error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    // Optional: handle permission result (only needed if you want to retry auto)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeBluetoothConnection();
            } else {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}