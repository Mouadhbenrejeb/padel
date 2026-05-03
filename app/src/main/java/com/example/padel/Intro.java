package com.example.padel;




import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.padel.databinding.ActivityIntroBinding;

public class Intro extends AppCompatActivity {

    private ActivityIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.startbtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intro.this, MainActivity.class);
            startActivity(intent);
        });


        binding.reservationsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intro.this, ReservationLogin.class);
            startActivity(intent);
        });


        binding.scanBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intro.this, ScanActivity.class);
            startActivity(intent);
        });
        binding.reclamationBtn.setOnClickListener(v -> {
            startActivity(new Intent(Intro.this, ReclamationCameraActivity.class));
        });
    }
}
