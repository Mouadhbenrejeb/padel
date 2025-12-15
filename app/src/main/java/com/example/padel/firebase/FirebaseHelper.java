package com.example.padel.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.padel.Reservation;
import com.example.padel.models.Court;
import com.example.padel.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for Firebase operations including Authentication, Realtime Database
 */
public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static final String PREFS_NAME = "PadelPrefs";
    private static final String KEY_DB_INITIALIZED = "db_initialized";
    
    // Retry configuration for database operations
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 1000; // 1 second
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private static FirebaseHelper instance;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Context context;

    // Database paths
    private static final String PATH_COURTS = "courts";
    private static final String PATH_USERS = "users";
    private static final String PATH_RESERVATIONS = "reservations";

    private FirebaseHelper(Context context) {
        this.context = context.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseHelper(context);
        }
        return instance;
    }

    // ==================== INITIALIZATION ====================

    /**
     * Check if database is initialized and initialize if needed
     */
    public void checkAndInitializeDatabase(OnDatabaseInitializedListener listener) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isInitialized = prefs.getBoolean(KEY_DB_INITIALIZED, false);

        if (isInitialized) {
            Log.d(TAG, "Database already initialized");
            if (listener != null) {
                listener.onInitialized(true);
            }
            return;
        }

        // Check if courts exist in Firebase
        mDatabase.child(PATH_COURTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    // Initialize courts
                    initializeDefaultCourts(listener);
                } else {
                    // Mark as initialized
                    markDatabaseInitialized();
                    if (listener != null) {
                        listener.onInitialized(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database check failed: " + error.getMessage());
                if (listener != null) {
                    listener.onInitialized(false);
                }
            }
        });
    }

    private void initializeDefaultCourts(OnDatabaseInitializedListener listener) {
        Log.d(TAG, "Initializing default courts...");

        // Create default courts
        Court court1 = new Court("court_1", "Court 1", "Standard padel court", true);
        Court court2 = new Court("court_2", "Court 2", "Standard padel court", true);
        Court court3 = new Court("court_3", "Court 3", "Premium padel court", true);

        DatabaseReference courtsRef = mDatabase.child(PATH_COURTS);
        courtsRef.child(court1.getId()).setValue(court1);
        courtsRef.child(court2.getId()).setValue(court2);
        courtsRef.child(court3.getId()).setValue(court3)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Default courts initialized successfully");
                        markDatabaseInitialized();
                        if (listener != null) {
                            listener.onInitialized(true);
                        }
                    } else {
                        Log.e(TAG, "Failed to initialize courts");
                        if (listener != null) {
                            listener.onInitialized(false);
                        }
                    }
                });
    }

    private void markDatabaseInitialized() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DB_INITIALIZED, true).apply();
    }

    // ==================== AUTHENTICATION ====================

    /**
     * Register a new user with email and password.
     * Implements retry mechanism with exponential backoff for database save failures.
     */
    public void registerUser(String email, String password, String firstName, String lastName,
                             OnAuthCompleteListener listener) {
        Log.d(TAG, "registerUser: Starting registration for email: " + email);
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "registerUser: Authentication successful for email: " + email);
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(TAG, "registerUser: FirebaseUser obtained, UID: " + firebaseUser.getUid());
                            // Save user data to database with retry mechanism
                            User user = new User(firebaseUser.getUid(), firstName, lastName, email);
                            saveUserDataWithRetry(firebaseUser, user, 0, listener);
                        } else {
                            // This should not happen, but handle it gracefully
                            Log.e(TAG, "registerUser: Authentication succeeded but FirebaseUser is null");
                            listener.onFailure("Registration succeeded but user is null");
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";
                        Log.e(TAG, "registerUser: Authentication failed - " + errorMessage);
                        listener.onFailure(errorMessage);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any unexpected failures
                    Log.e(TAG, "registerUser: Unexpected authentication failure - " + e.getMessage(), e);
                    listener.onFailure(e.getMessage() != null ? e.getMessage() : "Registration failed");
                });
    }

    /**
     * Saves user data to the database with retry mechanism using exponential backoff.
     * 
     * @param firebaseUser The authenticated Firebase user
     * @param user The user data to save
     * @param attemptNumber Current retry attempt (0-based)
     * @param listener Callback listener for completion
     */
    private void saveUserDataWithRetry(FirebaseUser firebaseUser, User user, int attemptNumber,
                                       OnAuthCompleteListener listener) {
        int currentAttempt = attemptNumber + 1;
        Log.d(TAG, "saveUserDataWithRetry: Attempt " + currentAttempt + "/" + MAX_RETRY_ATTEMPTS + 
              " for user: " + firebaseUser.getUid());
        
        mDatabase.child(PATH_USERS).child(firebaseUser.getUid()).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "saveUserDataWithRetry: User data saved successfully on attempt " + 
                          currentAttempt + " for UID: " + firebaseUser.getUid());
                    listener.onSuccess(firebaseUser);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "saveUserDataWithRetry: Failed to save user data on attempt " + 
                          currentAttempt + "/" + MAX_RETRY_ATTEMPTS + " - Error: " + e.getMessage(), e);
                    
                    if (attemptNumber < MAX_RETRY_ATTEMPTS - 1) {
                        // Calculate exponential backoff delay
                        long delayMs = (long) (INITIAL_BACKOFF_MS * Math.pow(BACKOFF_MULTIPLIER, attemptNumber));
                        Log.d(TAG, "saveUserDataWithRetry: Scheduling retry in " + delayMs + "ms");
                        
                        // Schedule retry with exponential backoff
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            saveUserDataWithRetry(firebaseUser, user, attemptNumber + 1, listener);
                        }, delayMs);
                    } else {
                        // All retry attempts exhausted
                        Log.e(TAG, "saveUserDataWithRetry: All " + MAX_RETRY_ATTEMPTS + 
                              " retry attempts exhausted for user: " + firebaseUser.getUid() + 
                              ". Authentication succeeded but user data could not be saved.");
                        // Return success since authentication succeeded - user can still use the app
                        // The user data can be saved later when they update their profile
                        listener.onSuccess(firebaseUser);
                    }
                });
    }

    /**
     * Login user with email and password
     */
    public void loginUser(String email, String password, OnAuthCompleteListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        listener.onSuccess(user);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Login failed";
                        listener.onFailure(errorMessage);
                    }
                });
    }

    /**
     * Logout current user
     */
    public void logout() {
        mAuth.signOut();
    }

    /**
     * Get current logged in user
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, OnOperationCompleteListener listener) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Failed to send reset email";
                        listener.onFailure(errorMessage);
                    }
                });
    }

    // ==================== COURTS ====================

    /**
     * Get all courts from database
     */
    public void getCourts(OnCourtsLoadedListener listener) {
        mDatabase.child(PATH_COURTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Court> courts = new ArrayList<>();
                for (DataSnapshot courtSnapshot : snapshot.getChildren()) {
                    Court court = courtSnapshot.getValue(Court.class);
                    if (court != null) {
                        courts.add(court);
                    }
                }
                listener.onCourtsLoaded(courts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load courts: " + error.getMessage());
                listener.onCourtsLoaded(new ArrayList<>());
            }
        });
    }

    // ==================== RESERVATIONS ====================

    /**
     * Create a new reservation
     */
    public void createReservation(Reservation reservation, OnOperationCompleteListener listener) {
        String odId = getCurrentUser() != null ? getCurrentUser().getUid() : "anonymous";
        reservation.setUserId(odId);

        String reservationId = mDatabase.child(PATH_RESERVATIONS).push().getKey();
        if (reservationId != null) {
            reservation.setId(reservationId);
            mDatabase.child(PATH_RESERVATIONS).child(reservationId).setValue(reservation)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onSuccess();
                        } else {
                            listener.onFailure("Failed to create reservation");
                        }
                    });
        } else {
            listener.onFailure("Failed to generate reservation ID");
        }
    }

    /**
     * Get reservations for current user
     */
    public void getUserReservations(OnReservationsLoadedListener listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            listener.onReservationsLoaded(new ArrayList<>());
            return;
        }

        mDatabase.child(PATH_RESERVATIONS)
                .orderByChild("userId")
                .equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Reservation> reservations = new ArrayList<>();
                        for (DataSnapshot resSnapshot : snapshot.getChildren()) {
                            Reservation reservation = resSnapshot.getValue(Reservation.class);
                            if (reservation != null) {
                                reservations.add(reservation);
                            }
                        }
                        listener.onReservationsLoaded(reservations);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load reservations: " + error.getMessage());
                        listener.onReservationsLoaded(new ArrayList<>());
                    }
                });
    }

    /**
     * Get all reservations (for admin or checking availability)
     */
    public void getAllReservations(OnReservationsLoadedListener listener) {
        mDatabase.child(PATH_RESERVATIONS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Reservation> reservations = new ArrayList<>();
                for (DataSnapshot resSnapshot : snapshot.getChildren()) {
                    Reservation reservation = resSnapshot.getValue(Reservation.class);
                    if (reservation != null) {
                        reservations.add(reservation);
                    }
                }
                listener.onReservationsLoaded(reservations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load reservations: " + error.getMessage());
                listener.onReservationsLoaded(new ArrayList<>());
            }
        });
    }

    /**
     * Get all booked time slots for a specific court and date
     */
    public void getBookedTimeSlots(String court, String date, OnBookedTimeSlotsListener listener) {
        mDatabase.child(PATH_RESERVATIONS)
                .orderByChild("court")
                .equalTo(court)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> bookedTimes = new ArrayList<>();
                        for (DataSnapshot resSnapshot : snapshot.getChildren()) {
                            Reservation reservation = resSnapshot.getValue(Reservation.class);
                            if (reservation != null &&
                                    date.equals(reservation.getDate()) &&
                                    !"cancelled".equals(reservation.getStatus())) {
                                bookedTimes.add(reservation.getTime());
                            }
                        }
                        listener.onBookedTimeSlotsLoaded(bookedTimes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to get booked time slots: " + error.getMessage());
                        listener.onBookedTimeSlotsLoaded(new ArrayList<>());
                    }
                });
    }

    /**
     * Check if a time slot is available for a specific court and date
     */
    public void checkTimeSlotAvailability(String court, String date, String time,
                                          OnAvailabilityCheckListener listener) {
        mDatabase.child(PATH_RESERVATIONS)
                .orderByChild("court")
                .equalTo(court)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isAvailable = true;
                        for (DataSnapshot resSnapshot : snapshot.getChildren()) {
                            Reservation reservation = resSnapshot.getValue(Reservation.class);
                            if (reservation != null &&
                                    date.equals(reservation.getDate()) &&
                                    time.equals(reservation.getTime())) {
                                isAvailable = false;
                                break;
                            }
                        }
                        listener.onAvailabilityChecked(isAvailable);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to check availability: " + error.getMessage());
                        listener.onAvailabilityChecked(false);
                    }
                });
    }

    /**
     * Cancel a reservation
     */
    public void cancelReservation(String reservationId, OnOperationCompleteListener listener) {
        mDatabase.child(PATH_RESERVATIONS).child(reservationId).child("status").setValue("cancelled")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure("Failed to cancel reservation");
                    }
                });
    }

    // ==================== USER DATA ====================

    /**
     * Get current user data from database
     */
    public void getCurrentUserData(OnUserDataLoadedListener listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            listener.onUserDataLoaded(null);
            return;
        }

        mDatabase.child(PATH_USERS).child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        listener.onUserDataLoaded(user);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load user data: " + error.getMessage());
                        listener.onUserDataLoaded(null);
                    }
                });
    }

    /**
     * Update user credits
     */
    public void updateUserCredits(double newCredits, OnOperationCompleteListener listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            listener.onFailure("User not logged in");
            return;
        }

        mDatabase.child(PATH_USERS).child(currentUser.getUid()).child("credits").setValue(newCredits)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure("Failed to update credits");
                    }
                });
    }

    // ==================== LISTENERS ====================

    public interface OnDatabaseInitializedListener {
        void onInitialized(boolean success);
    }

    public interface OnAuthCompleteListener {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    public interface OnOperationCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface OnCourtsLoadedListener {
        void onCourtsLoaded(List<Court> courts);
    }

    public interface OnReservationsLoadedListener {
        void onReservationsLoaded(List<Reservation> reservations);
    }

    public interface OnAvailabilityCheckListener {
        void onAvailabilityChecked(boolean isAvailable);
    }

    public interface OnUserDataLoadedListener {
        void onUserDataLoaded(User user);
    }

    public interface OnBookedTimeSlotsListener {
        void onBookedTimeSlotsLoaded(List<String> bookedTimes);
    }
}
