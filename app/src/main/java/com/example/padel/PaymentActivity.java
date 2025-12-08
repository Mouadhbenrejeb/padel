package com.example.padel;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    TextView tvPhone, tvParts;
    Spinner spinnerParts;
    Switch switchExtra;
    Button btnModify, btnPay;

    String court, date, time, players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment); // ton nouveau XML

        // Récupérer les données de la réservation depuis l'activité précédente
        court = getIntent().getStringExtra("court");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        players = getIntent().getStringExtra("players");

        // Lier les vues
        tvPhone = findViewById(R.id.tvPhone);
        spinnerParts = findViewById(R.id.spinnerParts);
        switchExtra = findViewById(R.id.switchExtra);
        btnModify = findViewById(R.id.btnModify);
        btnPay = findViewById(R.id.btnPay);

        // Afficher le numéro de téléphone par défaut
        tvPhone.setText("95182340");

        // Modifier le téléphone
        btnModify.setOnClickListener(v -> {
            // Ici, tu peux ouvrir un dialog pour changer le numéro
            Toast.makeText(PaymentActivity.this, "Modifier numéro cliqué", Toast.LENGTH_SHORT).show();
        });

        // Cliquer sur Payer
        btnPay.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        // Obtenir le nombre de parts et si extras sont commandés
        int parts = spinnerParts.getSelectedItemPosition() + 1; // par exemple
        boolean extras = switchExtra.isChecked();

        // Calculer prix total (exemple simple)
        int basePrice = 80; // prix de base
        int totalPrice = basePrice * parts;
        if (extras) totalPrice += 20; // exemple frais extras

        // Simuler paiement réussi
        Toast.makeText(this, "Paiement réussi! Total: " + totalPrice + " DT", Toast.LENGTH_SHORT).show();

        // Générer contenu pour QR ou résumé
        String qrContent = "Reservation: " + court + "\nDate: " + date + "\nTime: " + time +
                "\nPlayers: " + players + "\nParts: " + parts + "\nExtras: " + (extras ? "Oui" : "Non");

        // Ouvrir ReservationsActivity et envoyer les données
        Intent intent = new Intent(PaymentActivity.this, ReservationsActivity.class);
        intent.putExtra("court", court);
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        intent.putExtra("players", players);
        intent.putExtra("qrContent", qrContent);
        startActivity(intent);
        finish();
    }
}
