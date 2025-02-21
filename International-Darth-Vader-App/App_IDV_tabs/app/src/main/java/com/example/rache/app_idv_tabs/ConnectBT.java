package com.example.rache.app_idv_tabs;

/**
 * This thread handles connecting to the Darth Vader via Bluetooth.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectBT extends Thread {
    private static final String LOG_TAG = "HEY";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice device;
    private UUID uuidBT;
    private String rfcommString;
    private String deviceAddress;
    private boolean cxnStatus;

    public ConnectBT(String deviceAddr, BluetoothAdapter btAdapter){
        deviceAddress = deviceAddr;
        mBluetoothAdapter = btAdapter;
    }

    public void run(){
        uuidBT = UUID.fromString("4c1199da-5622-476c-a5e0-e9abe9262754");
        rfcommString = "idvPlayAudio";

        // Register Pi using retrieved MAC address
        device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        BluetoothSocket temp = null;

        // Get a BluetoothSocket for a connection with the given BluetoothDevice
        try {
            temp = device.createRfcommSocketToServiceRecord(uuidBT);
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            temp = (BluetoothSocket) m.invoke(device, 1); // Port 1
        } catch (Exception e) {
            Log.e(LOG_TAG, "BluetoothSocket create() failed: " + e);
        }
        mBluetoothSocket = temp;

        try {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            btConnected(true);
        } catch (IOException ioe) {
            try {
                mBluetoothSocket.close();
            } catch (IOException ioe2) {
                Log.e(LOG_TAG, "BluetoothSocket close() failed: " + ioe2);
            }
            Log.e(LOG_TAG, "Establish BluetoothSocket Connection failed: " + ioe);
        }
    }

    public boolean btConnected(boolean status){
        cxnStatus = status;
        return status;
    }

    public BluetoothSocket getSocket(){
        return mBluetoothSocket;
    }
}

