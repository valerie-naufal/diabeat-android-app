package com.example.diabeatapp.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import android.bluetooth.BluetoothDevice;
import androidx.core.app.ActivityCompat;

import com.example.diabeatapp.bluetooth.BluetoothServiceGlucose;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class BluetoothServiceInsulin {

    private static BluetoothServiceInsulin instance;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private static final String TAG = "BluetoothServiceInsulin";
    private boolean isConnected = false;
    private final LinkedBlockingQueue<String> sendQueue = new LinkedBlockingQueue<>();
    private Thread senderThread;

    private BluetoothServiceInsulin() {}

    public static BluetoothServiceInsulin getInstance() {
        if (instance == null) instance = new BluetoothServiceInsulin();
        return instance;
    }

    public void connectToDevice(BluetoothDevice device, UUID uuid, Context context, BluetoothServiceGlucose.ConnectionCallback callback) {
        new Thread(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    callback.onError(new SecurityException("BLUETOOTH_CONNECT permission not granted"));
                    return;
                }

                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                //BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                //bluetoothSocket.connect();

                boolean success;
                try {
                    this.bluetoothSocket.connect();
                    success =true;
                } catch (IOException e) {
                    Log.e("", e.getMessage());
                    try {
                        System.out.println("BT Socket"+ bluetoothSocket);
                        this.bluetoothSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{Integer.TYPE}).invoke(device, new Object[]{1});
                        this.bluetoothSocket.connect();
                        success = true;
                    } catch (Exception e2) {
                        Log.e("", "Couldn't establish Bluetooth connection!");
                        try {
                            this.bluetoothSocket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

                outputStream = bluetoothSocket.getOutputStream();
                isConnected = true;
                startSenderThread();
                callback.onConnected();
                Log.d(TAG, "Connected to device: " + device.getName());
            } catch (IOException e) {
                isConnected = false;
                callback.onError(e);
            }
        }).start();
    }

    private void startSenderThread() {
        senderThread = new Thread(() -> {
            while (isConnected) {
                try {
                    String duration = sendQueue.take(); // waits if empty
                    if (outputStream != null) {
                        outputStream.write(duration.getBytes());
                        outputStream.flush();
                        Log.d(TAG, "Dosage sent: " + duration);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to send dosage", e);
                }
            }
        });
        senderThread.start();
    }

    // Old sendPumpDuration
    /*public void sendPumpDuration(String durationMs) {
        if (outputStream != null && isConnected) {
            try {
                outputStream.write(durationMs.getBytes());
                outputStream.flush(); // Optional: ensure data is sent immediately
                Log.d("BluetoothServiceInsulin", "Dosage sent: " + durationMs);
            } catch (IOException e) {
                Log.e("BluetoothServiceInsulin", "Failed to send dosage", e);
            }
        } else {
            Log.e("BluetoothServiceInsulin", "Not connected or outputStream is null");
        }
    }*/

    // New sendPumpDuration
    public void sendPumpDuration(String durationMs) {
        if (isConnected) {
            sendQueue.offer(durationMs);
        } else {
            Log.e(TAG, "Not connected to insulin device");
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void disconnect() {
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
            isConnected = false;
        } catch (IOException e) {
            Log.e("BluetoothServiceInsulin", "Error closing connection", e);
        }
    }
}