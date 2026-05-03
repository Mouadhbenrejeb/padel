package com.example.padel;

import android.app.Application;
import android.util.Log;

import com.example.padel.firebase.FirebaseHelper;


public class PadelApplication extends Application {
    private static final String TAG = "PadelApplication";

    @Override
    public void onCreate() {
        super.onCreate();


        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance(this);
        firebaseHelper.checkAndInitializeDatabase(success -> {
            if (success) {
                Log.d(TAG, "Database initialization check completed successfully");
            } else {
                Log.e(TAG, "Database initialization check failed");
            }
        });
    }
}
