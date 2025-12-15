package com.example.padel;

import android.app.Application;
import android.util.Log;

import com.example.padel.firebase.FirebaseHelper;

/**
 * Application class for initializing Firebase and checking database on first app open
 */
public class PadelApplication extends Application {
    private static final String TAG = "PadelApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase and check if database needs initialization
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
