package com.example.mimos.simpleweatherapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;

public class PermissionManager extends ActivityCompat {

    private static final int PERMISSION_REQUEST_CODE = 200;  //static permission code

    private Activity activity;

    //permission list
    String[] PERMISSIONS = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};

    public PermissionManager(Activity context) {
        activity = context;
    }

    /**
     * check for permission.
     */
    public boolean hasPermission (String permission) {
        Log.d("SimpleWeatherAPP", "Check " + permission);
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * request for permission.
     */
    public void reqPermissions () {
        Log.d("SimpleWeatherAPP", "Req permissions");
        ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    public int getPermissionRequestCode() {
        return PERMISSION_REQUEST_CODE;
    }
}
