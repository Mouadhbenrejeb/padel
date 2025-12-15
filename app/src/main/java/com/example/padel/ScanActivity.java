package com.example.padel;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.padel.databinding.ActivityScanBinding;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ScanActivity extends AppCompatActivity {

    private ActivityScanBinding binding;

    // QR Scanner launcher
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    parseAndDisplayResult(result.getContents());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Scan button click - launch QR scanner
        binding.scanButton.setOnClickListener(v -> startQrScan());

        // Back button click - finish activity
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void startQrScan() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan reservation QR code");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(false);
        options.setOrientationLocked(true);

        barcodeLauncher.launch(options);
    }

    private void parseAndDisplayResult(String qrContent) {
        // QR format: "court | date | time | players" or "court | date | time"
        String[] parts = qrContent.split("\\s*\\|\\s*");

        if (parts.length >= 3) {
            String court = parts[0].trim();
            String date = parts[1].trim();
            String time = parts[2].trim();

            binding.courtText.setText(court);
            binding.dateText.setText(date);
            binding.timeText.setText(time);

            // Check if players info is available
            if (parts.length >= 4) {
                String players = parts[3].trim();
                binding.playersText.setText(players);
                binding.playersLayout.setVisibility(View.VISIBLE);
            } else {
                binding.playersLayout.setVisibility(View.GONE);
            }

            // Show the result card
            binding.resultCard.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
        }
    }
}
