package com.example.padel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.padel.firebase.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    EditText firstName, lastName, email, password;
    CheckBox terms;
    Button registerBtn;
    TextView alreadySignIn;

    private FirebaseHelper firebaseHelper;
    
    // Reservation data passed from login screen
    private String court = "";
    private String date = "";
    private String time = "";
    private String players = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_register);
        
        // Apply window insets for edge-to-edge
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        // Initialize Firebase Helper
        firebaseHelper = FirebaseHelper.getInstance(this);
        
        // Get reservation data if passed from login screen
        court = getIntent().getStringExtra("court");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        players = getIntent().getStringExtra("players");
        if (court == null) court = "";
        if (date == null) date = "";
        if (time == null) time = "";
        if (players == null) players = "";

        // Lier les vues
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        terms = findViewById(R.id.terms);
        registerBtn = findViewById(R.id.registerBtn);
        alreadySignIn = findViewById(R.id.alreadySignInText);

        // Clic sur ENREGISTREMENT
        registerBtn.setOnClickListener(v -> {
            String fName = firstName.getText().toString().trim();
            String lName = lastName.getText().toString().trim();
            String mail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            boolean isChecked = terms.isChecked();

            if (fName.isEmpty() || lName.isEmpty() || mail.isEmpty() || pass.isEmpty()) {
                Toast.makeText(Register.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                Toast.makeText(Register.this, "Format d'adresse e-mail invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            String passwordError = validatePasswordStrength(pass);
            if (passwordError != null) {
                Toast.makeText(Register.this, passwordError, Toast.LENGTH_LONG).show();
                return;
            }

            if (!isChecked) {
                Toast.makeText(Register.this, "Veuillez accepter les conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading dialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Inscription en cours...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Create a flag to track if registration completed
            final boolean[] isCompleted = {false};

            // Set up timeout handler (12 seconds)
            Handler timeoutHandler = new Handler(Looper.getMainLooper());
            Runnable timeoutRunnable = () -> {
                if (!isCompleted[0]) {
                    isCompleted[0] = true;
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(Register.this, "Délai d'attente dépassé. Veuillez réessayer.", Toast.LENGTH_LONG).show();
                }
            };
            timeoutHandler.postDelayed(timeoutRunnable, 12000);

            // Register user with Firebase
            firebaseHelper.registerUser(mail, pass, fName, lName, new FirebaseHelper.OnAuthCompleteListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Log.d("REGISTER", "User registered successfully");
                    if (isCompleted[0]) return; // Already timed out
                    isCompleted[0] = true;
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    progressDialog.dismiss();
                    Toast.makeText(Register.this, "Inscription réussie", Toast.LENGTH_SHORT).show();

                    // If we have reservation data, go to PaymentActivity to complete the reservation
                    // Otherwise, go to ReservationsActivity to view existing reservations
                    Intent intent;
                    if (date != null && !date.isEmpty() && time != null && !time.isEmpty()) {
                        intent = new Intent(Register.this, PaymentActivity.class);
                        intent.putExtra("court", court);
                        intent.putExtra("date", date);
                        intent.putExtra("time", time);
                        intent.putExtra("players", players);
                    } else {
                        intent = new Intent(Register.this, ReservationsActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    if (isCompleted[0]) return; // Already timed out
                    isCompleted[0] = true;
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    progressDialog.dismiss();
                    Log.d("TAG", errorMessage);
                    Toast.makeText(Register.this, "Erreur: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Clic sur "Déjà inscrit? S'identifier"
        alreadySignIn.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, ReservationLogin.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Validates password strength and returns an error message in French if requirements are not met.
     * Password must contain at least:
     * - 6 characters
     * - One uppercase letter
     * - One lowercase letter
     * - One number
     * - One special character
     *
     * @param password The password to validate
     * @return Error message in French if validation fails, null if password is valid
     */
    private String validatePasswordStrength(String password) {
        StringBuilder errorMessage = new StringBuilder();
        
        // Check minimum length
        if (password.length() < 6) {
            errorMessage.append("• Au moins 6 caractères\n");
        }
        
        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            errorMessage.append("• Au moins une lettre majuscule\n");
        }
        
        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            errorMessage.append("• Au moins une lettre minuscule\n");
        }
        
        // Check for at least one number
        if (!password.matches(".*[0-9].*")) {
            errorMessage.append("• Au moins un chiffre\n");
        }
        
        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errorMessage.append("• Au moins un caractère spécial (!@#$%^&*...)\n");
        }
        
        if (errorMessage.length() > 0) {
            return "Le mot de passe doit contenir :\n" + errorMessage.toString().trim();
        }
        
        return null;
    }
}
