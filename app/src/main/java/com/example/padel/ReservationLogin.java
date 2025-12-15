package com.example.padel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.padel.firebase.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;

public class ReservationLogin extends AppCompatActivity {

    EditText Email, Password;
    Button loginbtn;
    TextView forgotSignUpText;

    String court = "", date = "", time = "", players = "";

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_reservation_login);
        
        // Apply window insets for edge-to-edge
        View mainView = findViewById(R.id.loginLayout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        // Initialize Firebase Helper
        firebaseHelper = FirebaseHelper.getInstance(this);

        // Récupérer les données passées depuis MainActivity
        court = getIntent().getStringExtra("court");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        players = getIntent().getStringExtra("players");

        // Check if user is already logged in
        if (firebaseHelper.isUserLoggedIn()) {
            navigateToPayment();
            return;
        }

        // Lier les vues XML
        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        loginbtn = findViewById(R.id.loginBtn);
        forgotSignUpText = findViewById(R.id.forgotSignUpText);

        // Gestion du bouton login
        loginbtn.setOnClickListener(v -> {
            String email = Email.getText().toString().trim();
            String pass = Password.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer vos identifiants", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format d'adresse e-mail invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading dialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Connexion en cours...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Firebase Authentication
            firebaseHelper.loginUser(email, pass, new FirebaseHelper.OnAuthCompleteListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    progressDialog.dismiss();
                    Toast.makeText(ReservationLogin.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                    navigateToPayment();
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressDialog.dismiss();
                    Toast.makeText(ReservationLogin.this, "Erreur: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Gestion du texte "Mot de passe oublié ? S'inscrire"
        String text = forgotSignUpText.getText().toString();
        SpannableString spannable = new SpannableString(text);

        // Partie "Mot de passe oublié ?" cliquable
        ClickableSpan forgotClickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Send password reset email using Firebase
                String email = Email.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(ReservationLogin.this, "Veuillez entrer votre email", Toast.LENGTH_SHORT).show();
                    return;
                }
                firebaseHelper.sendPasswordResetEmail(email, new FirebaseHelper.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ReservationLogin.this, "Email de réinitialisation envoyé", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(ReservationLogin.this, "Erreur: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
                ds.setUnderlineText(false);
            }
        };
        spannable.setSpan(forgotClickable, 0, "Mot de passe oublié ?".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Partie "S'inscrire" cliquable
        ClickableSpan signUpClickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Ouvrir Register.java with reservation data
                Intent intent = new Intent(ReservationLogin.this, Register.class);
                intent.putExtra("court", court);
                intent.putExtra("date", date);
                intent.putExtra("time", time);
                intent.putExtra("players", players);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
                ds.setUnderlineText(false);
            }
        };
        spannable.setSpan(signUpClickable, text.indexOf("S'inscrire"), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Appliquer le SpannableString au TextView
        forgotSignUpText.setText(spannable);
        forgotSignUpText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void navigateToPayment() {
        // Check if we have reservation details (coming from MainActivity)
        // If not (coming from Intro), go to ReservationsActivity instead
        Intent intent;
        if (date != null && !date.isEmpty() && time != null && !time.isEmpty()) {
            // We have reservation details, go to PaymentActivity to complete reservation
            intent = new Intent(ReservationLogin.this, PaymentActivity.class);
            intent.putExtra("court", court);
            intent.putExtra("date", date);
            intent.putExtra("time", time);
            intent.putExtra("players", players);
            if (firebaseHelper.getCurrentUser() != null) {
                intent.putExtra("email", firebaseHelper.getCurrentUser().getEmail());
            }
        } else {
            // No reservation details, go to ReservationsActivity to view existing reservations
            intent = new Intent(ReservationLogin.this, ReservationsActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
