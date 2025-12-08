package com.example.padel;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ReservationLogin extends AppCompatActivity {

    EditText Email, Password;
    Button loginbtn;
    TextView forgotSignUpText;

    String court = "", date = "", time = "", players = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_login); // votre layout XML

        // Récupérer les données passées depuis MainActivity
        court = getIntent().getStringExtra("court");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        players = getIntent().getStringExtra("players");

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

            // Connexion locale réussie (pas de Firebase)
            Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show();

            // Aller à la page récapitulative de réservation
            Intent intent = new Intent(ReservationLogin.this, ReservationsActivity.class);
            intent.putExtra("court", court);
            intent.putExtra("date", date);
            intent.putExtra("time", time);
            intent.putExtra("players", players);
            intent.putExtra("email", email);
            startActivity(intent);
        });

        // Gestion du texte "Mot de passe oublié ? S'inscrire"
        String text = forgotSignUpText.getText().toString();
        SpannableString spannable = new SpannableString(text);

        // Partie "Mot de passe oublié ?" cliquable
        ClickableSpan forgotClickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Ici tu peux ouvrir un écran de réinitialisation de mot de passe
                Toast.makeText(ReservationLogin.this, "Mot de passe oublié cliqué", Toast.LENGTH_SHORT).show();
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
                // Ouvrir Register.java
                Intent intent = new Intent(ReservationLogin.this, Register.class);
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
}
