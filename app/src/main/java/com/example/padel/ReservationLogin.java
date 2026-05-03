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
        

        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_reservation_login);
        

        View mainView = findViewById(R.id.loginLayout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }


        firebaseHelper = FirebaseHelper.getInstance(this);


        court = getIntent().getStringExtra("court");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        players = getIntent().getStringExtra("players");


        if (firebaseHelper.isUserLoggedIn()) {
            navigateToPayment();
            return;
        }


        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        loginbtn = findViewById(R.id.loginBtn);
        forgotSignUpText = findViewById(R.id.forgotSignUpText);


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


            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Connexion en cours...");
            progressDialog.setCancelable(false);
            progressDialog.show();


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


        String text = forgotSignUpText.getText().toString();
        SpannableString spannable = new SpannableString(text);


        ClickableSpan forgotClickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {

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


        ClickableSpan signUpClickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {

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


        forgotSignUpText.setText(spannable);
        forgotSignUpText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void navigateToPayment() {
        // tchouf idha fama reservation detzail mn mainactivitionymchy pzyment act sinon  ymchy l reservation act

        Intent intent;
        if (date != null && !date.isEmpty() && time != null && !time.isEmpty()) {

            intent = new Intent(ReservationLogin.this, PaymentActivity.class);
            intent.putExtra("court", court);
            intent.putExtra("date", date);
            intent.putExtra("time", time);
            intent.putExtra("players", players);
            if (firebaseHelper.getCurrentUser() != null) {
                intent.putExtra("email", firebaseHelper.getCurrentUser().getEmail());
            }
        } else {

            intent = new Intent(ReservationLogin.this, ReservationsActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
