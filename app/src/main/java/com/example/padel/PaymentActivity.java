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


        firebaseHelper = FirebaseHelper.getInstance(this);


        court = getIntent().getStringExtra("court");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        players = getIntent().getStringExtra("players");


        tvPhone = findViewById(R.id.tvPhone);
        tvCredits = findViewById(R.id.tvCredits);
        spinnerParts = findViewById(R.id.spinnerParts);
        switchExtra = findViewById(R.id.switchExtra);
        btnModify = findViewById(R.id.btnModify);
        btnPay = findViewById(R.id.btnPay);
        loadingOverlay = findViewById(R.id.loadingOverlay);


        tvPhone.setText("95182340");


        loadUserCredits();


        btnModify.setOnClickListener(v -> {

            Toast.makeText(PaymentActivity.this, "Modifier numéro cliqué", Toast.LENGTH_SHORT).show();
        });


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

        final int parts = spinnerParts.getSelectedItemPosition() + 1;
        final boolean extras = switchExtra.isChecked();


        int basePrice = 80;
        int price = basePrice;
        if (extras) price += 20;
        final int totalPrice = price;


        if (userCredits < totalPrice) {
            Toast.makeText(this, "Crédit insuffisant! Vous avez " + String.format("%.2f", userCredits) + " DT", Toast.LENGTH_SHORT).show();
            return;
        }


        showLoading(true);


        Reservation newReservation = new Reservation(court, date, time, players != null ? players : "4");

        firebaseHelper.createReservation(newReservation, new FirebaseHelper.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {

                final double newCredits = userCredits - totalPrice;
                firebaseHelper.updateUserCredits(newCredits, new FirebaseHelper.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            showLoading(false);
                            userCredits = newCredits;
                            

                            Toast.makeText(PaymentActivity.this, "Paiement réussi! Total: " + totalPrice + " DT", Toast.LENGTH_SHORT).show();


                            String qrContent = "Reservation: " + court + "\nDate: " + date + "\nTime: " + time;
                            if (players != null && !players.isEmpty()) {
                                qrContent += "\nPlayers: " + players;
                            }
                            qrContent += "\nParts: " + parts + "\nExtras: " + (extras ? "Oui" : "Non");


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
