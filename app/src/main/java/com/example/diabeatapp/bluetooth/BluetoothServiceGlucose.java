package com.example.diabeatapp.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothServiceGlucose {

    private static BluetoothServiceGlucose instance;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread readThread;
    private boolean isConnected = false;
    private static final String TAG = "BluetoothServiceGlucose";

    public interface ConnectionCallback {
        void onConnected();
        void onDisconnected();
        void onError(Exception e);
    }

    public interface DataCallback {
        void onDataReceived(String data);
    }

    private BluetoothServiceGlucose() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothServiceGlucose getInstance() {
        if (instance == null) {
            instance = new BluetoothServiceGlucose();
        }
        return instance;
    }

    public void connectToDevice(BluetoothDevice device, UUID uuid, Context context, ConnectionCallback connectionCallback) {
        new Thread(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Bluetooth permission not granted.");
                    connectionCallback.onError(new SecurityException("BLUETOOTH_CONNECT permission not granted"));
                    return;
                }

                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                //bluetoothAdapter.cancelDiscovery();

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

                //bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
                isConnected = true;
                connectionCallback.onConnected();
                Log.d(TAG, "Connected to device: " + device.getName());
            } catch (IOException e) {
                isConnected = false;
                connectionCallback.onError(e);
                Log.e(TAG, "Connection failed", e);
            }
        }).start();
    }

    public void listenForData(DataCallback dataCallback) {
        if (inputStream == null) {
            Log.e(TAG, "Input stream is null. Cannot listen for data.");
            return;
        }

        readThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (isConnected && !Thread.currentThread().isInterrupted()) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        String incomingMessage = new String(buffer, 0, bytes);
                        //Log.d(TAG, "Received: " + incomingMessage);
                        dataCallback.onDataReceived(incomingMessage.trim());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading data", e);
                    disconnect();
                    break;
                }
            }
        });
        readThread.start();
    }

    public void sendData(String data) {
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
                Log.d(TAG, "Sent data: " + data);
            } catch (IOException e) {
                Log.e(TAG, "Failed to send data", e);
            }
        }
    }

    public void disconnect() {
        try {
            isConnected = false;
            if (readThread != null) readThread.interrupt();
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
            Log.d(TAG, "Disconnected from device");
        } catch (IOException e) {
            Log.e(TAG, "Error during disconnect", e);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}