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

        // Inflate layout with binding
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Button click
        binding.startbtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intro.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
