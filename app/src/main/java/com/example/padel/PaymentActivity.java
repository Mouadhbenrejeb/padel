package com.example.padel;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.padel.firebase.FirebaseHelper;
import com.example.padel.models.User;

public class PaymentActivity extends AppCompatActivity {

    TextView tvPhone, tvParts, tvCredits;
    Spinner spinnerParts;
    Switch switchExtra;
    Button btnModify, btnPay;
    FrameLayout loadingOverlay;

    String court, date, time, players;

    private FirebaseHelper firebaseHelper;
    private double userCredits = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize Firebase Helper
        firebaseHelper = FirebaseHelper.getInstance(this);

        // Récupérer les données de la réservation depuis l'activité précédente
        court = getIntent().getStringExtra("court");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        players = getIntent().getStringExtra("players");

        // Lier les vues
        tvPhone = findViewById(R.id.tvPhone);
        tvCredits = findViewById(R.id.tvCredits);
        spinnerParts = findViewById(R.id.spinnerParts);
        switchExtra = findViewById(R.id.switchExtra);
        btnModify = findViewById(R.id.btnModify);
        btnPay = findViewById(R.id.btnPay);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Afficher le numéro de téléphone par défaut
        tvPhone.setText("95182340");

        // Load user credits
        loadUserCredits();

        // Modifier le téléphone
        btnModify.setOnClickListener(v -> {
            // Ici, tu peux ouvrir un dialog pour changer le numéro
            Toast.makeText(PaymentActivity.this, "Modifier numéro cliqué", Toast.LENGTH_SHORT).show();
        });

        // Cliquer sur Payer
        btnPay.setOnClickListener(v -> processPayment());
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPay.setEnabled(!show);
    }

    private void loadUserCredits() {
        firebaseHelper.getCurrentUserData(user -> {
            runOnUiThread(() -> {
                if (user != null) {
                    userCredits = user.getCredits();
                    tvCredits.setText(String.format("Votre crédit: %.2f DT", userCredits));
                } else {
                    tvCredits.setText("Votre crédit: 0.00 DT");
                }
            });
        });
    }

    private void processPayment() {
        // Obtenir le nombre de parts et si extras sont commandés
        final int parts = spinnerParts.getSelectedItemPosition() + 1; // par exemple
        final boolean extras = switchExtra.isChecked();

        // Calculer prix total (exemple simple)
        int basePrice = 80; // prix de base
        int price = basePrice;
        if (extras) price += 20; // exemple frais extras
        final int totalPrice = price;

        // Check if user has enough credits
        if (userCredits < totalPrice) {
            Toast.makeText(this, "Crédit insuffisant! Vous avez " + String.format("%.2f", userCredits) + " DT", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        showLoading(true);

        // Create reservation in Firebase
        Reservation newReservation = new Reservation(court, date, time, players != null ? players : "4");

        firebaseHelper.createReservation(newReservation, new FirebaseHelper.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                // Deduct credits after successful reservation
                final double newCredits = userCredits - totalPrice;
                firebaseHelper.updateUserCredits(newCredits, new FirebaseHelper.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            showLoading(false);
                            userCredits = newCredits;
                            
                            // Paiement réussi
                            Toast.makeText(PaymentActivity.this, "Paiement réussi! Total: " + totalPrice + " DT", Toast.LENGTH_SHORT).show();

                            // Générer contenu pour QR ou résumé
                            String qrContent = "Reservation: " + court + "\nDate: " + date + "\nTime: " + time;
                            if (players != null && !players.isEmpty()) {
                                qrContent += "\nPlayers: " + players;
                            }
                            qrContent += "\nParts: " + parts + "\nExtras: " + (extras ? "Oui" : "Non");

                            // Ouvrir ReservationsActivity (reservation already created)
                            Intent intent = new Intent(PaymentActivity.this, ReservationsActivity.class);
                            intent.putExtra("qrContent", qrContent);
                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            // Reservation created but credits not deducted - still navigate
                            Toast.makeText(PaymentActivity.this, "Réservation créée mais erreur de crédit: " + errorMessage, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PaymentActivity.this, ReservationsActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(PaymentActivity.this, "Erreur: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
