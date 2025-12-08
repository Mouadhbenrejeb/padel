package com.example.padel;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Register extends AppCompatActivity {

    EditText firstName, lastName, email, password;
    CheckBox terms;
    Button registerBtn;
    TextView alreadySignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Lier les vues
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        terms = findViewById(R.id.terms);
        registerBtn = findViewById(R.id.registerBtn);
        alreadySignIn = findViewById(R.id.alreadySignInText); // il faut ajouter id dans XML

        // Clic sur ENREGISTREMENT
        registerBtn.setOnClickListener(v -> {
            String fName = firstName.getText().toString().trim();
            String lName = lastName.getText().toString().trim();
            String mail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            boolean isChecked = terms.isChecked();

            if (fName.isEmpty() || lName.isEmpty() || mail.isEmpty() || pass.isEmpty()) {
                Toast.makeText(Register.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isChecked) {
                Toast.makeText(Register.this, "Veuillez accepter les conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ici, tu peux enregistrer les infos dans ta DB ou Firebase
            Toast.makeText(Register.this, "Inscription réussie", Toast.LENGTH_SHORT).show();

            // Aller à MainActivity
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
            finish(); // optionnel, pour ne pas revenir sur le register en arrière
        });

        // Clic sur "Déjà inscrit? S'identifier"
        alreadySignIn.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, ReservationLogin.class);
            startActivity(intent);
            finish(); // optionnel
        });
    }
}
