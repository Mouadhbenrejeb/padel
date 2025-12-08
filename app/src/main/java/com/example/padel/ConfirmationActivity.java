package com.example.padel;



import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.WriterException;

public class ConfirmationActivity extends AppCompatActivity {

    TextView tvCourt, tvDate, tvTime, tvPlayers;
    ImageView ivQrCode;
    MaterialButton btnViewReservations, btnHome;

    String court, date, time, players, qrContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        // Initialize views
        tvCourt = findViewById(R.id.tvCourt);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvPlayers = findViewById(R.id.tvPlayers);
        ivQrCode = findViewById(R.id.ivQrCode);
        btnViewReservations = findViewById(R.id.btnViewReservations);
        btnHome = findViewById(R.id.btnHome);

        // Get data from PaymentActivity
        Intent intent = getIntent();
        court = intent.getStringExtra("court");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");
        players = intent.getStringExtra("players");
        qrContent = intent.getStringExtra("qrContent");

        // Set reservation details
        tvCourt.setText("Court: " + court);
        tvDate.setText("Date: " + date);
        tvTime.setText("Time: " + time);
        tvPlayers.setText("Players: " + (players != null ? players : "N/A"));

        // Generate QR code
        generateQrCode(qrContent);

        // Button click listeners
        btnViewReservations.setOnClickListener(v -> {
            Intent i = new Intent(ConfirmationActivity.this, ReservationsActivity.class);
            startActivity(i);
        });

        btnHome.setOnClickListener(v -> {
            Intent i = new Intent(ConfirmationActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }

    private void generateQrCode(String content) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 400, 400);
            Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565);
            for (int x = 0; x < 400; x++) {
                for (int y = 0; y < 400; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ivQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
