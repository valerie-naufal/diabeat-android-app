package com.example.diabeatapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
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

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final UUID WRIST_PI_UUID = UUID.fromString("0000110a-0000-1000-8000-00805F9B34FB");
    private static final UUID BELT_PI_UUID = UUID.fromString("0000110a-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice raspberryPiDevice;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 100;
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
                R.id.navigation_dashboard, R.id.navigation_logs, R.id.navigation_add_logs, R.id.navigation_consumption, R.id.navigation_profile)
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
                    int sensorValue = 0;
                    if(!data.contains(".") && !data.isEmpty()){
                        sensorValue = Integer.parseInt(data);
                    }

                    if(sensorValue > 499){
                        // Get from firebase
                        double weight = 90.5;
                        double tdd = weight * 0.3;

                        // Get from firebase
                        double carbs = 5.0;

                        // Send to firebase
                        double glucose = (-2.65) * Math.pow(10, -5) * sensorValue * sensorValue + 0.0814 * sensorValue + 58.02;

                        // Send to firebase
                        double insulinDose = (carbs/(500/tdd)) + ((glucose - 100)/(1800/tdd));

                        // Pump duration in milliseconds
                        int pumpDurationMs = (int)((insulinDose/10)) * 1000;
                        Log.d("MainActivity", "Calculated glucose/blood sugar level: " + data);
                    }
                    Log.d("MainActivity", "Received sensor value: " + sensorValue);
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
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Log.d("MainActivity", "Connected to PicoW-Belt");
                    // The dosage is hard-coded for the time being until we configure the GlucoseML model
                    BluetoothServiceInsulin.getInstance().sendPumpDuration("1000");
                }, 5000); // 5-second delay
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