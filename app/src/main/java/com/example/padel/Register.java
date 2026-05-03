package com.example.padel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.padel.firebase.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    EditText firstName, lastName, email, password;
    CheckBox terms;
    Button registerBtn;
    TextView alreadySignIn;

    private FirebaseHelper firebaseHelper;
    
    //  data mta3 reservation  teatada men login screen
    private String court = "";
    private String date = "";
    private String time = "";
    private String players = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_register);
        

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }


        firebaseHelper = FirebaseHelper.getInstance(this);
        
        // tjib data taa reserva ken taadat mn   login
        court = getIntent().getStringExtra("court");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        players = getIntent().getStringExtra("players");
        if (court == null) court = "";
        if (date == null) date = "";
        if (time == null) time = "";
        if (players == null) players = "";


        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        terms = findViewById(R.id.terms);
        registerBtn = findViewById(R.id.registerBtn);
        alreadySignIn = findViewById(R.id.alreadySignInText);


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

            if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                Toast.makeText(Register.this, "Format d'adresse e-mail invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            String passwordError = validatePasswordStrength(pass);
            if (passwordError != null) {
                Toast.makeText(Register.this, passwordError, Toast.LENGTH_LONG).show();
                return;
            }

            if (!isChecked) {
                Toast.makeText(Register.this, "Veuillez accepter les conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            //  loading dialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Inscription en cours...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // tasna3 flag tchouf register kmlt wlee

            final boolean[] isCompleted = {false};

            // hedhy timer  (12 sec)
            Handler timeoutHandler = new Handler(Looper.getMainLooper());
            Runnable timeoutRunnable = () -> {
                if (!isCompleted[0]) {
                    isCompleted[0] = true;
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(Register.this, "Délai d'attente dépassé. Veuillez réessayer.", Toast.LENGTH_LONG).show();
                }
            };
            timeoutHandler.postDelayed(timeoutRunnable, 12000);


            firebaseHelper.registerUser(mail, pass, fName, lName, new FirebaseHelper.OnAuthCompleteListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Log.d("REGISTER", "User registered successfully");
                    if (isCompleted[0]) return;
                    isCompleted[0] = true;
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    progressDialog.dismiss();
                    Toast.makeText(Register.this, "Inscription réussie", Toast.LENGTH_SHORT).show();

                    //  ken fam data taa reserv temchy ll payment screen sinon temchy reservationactiv wtchouf reservation mteek

                    Intent intent;
                    if (date != null && !date.isEmpty() && time != null && !time.isEmpty()) {
                        intent = new Intent(Register.this, PaymentActivity.class);
                        intent.putExtra("court", court);
                        intent.putExtra("date", date);
                        intent.putExtra("time", time);
                        intent.putExtra("players", players);
                    } else {
                        intent = new Intent(Register.this, ReservationsActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    if (isCompleted[0]) return;
                    isCompleted[0] = true;
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    progressDialog.dismiss();
                    Log.d("TAG", errorMessage);
                    Toast.makeText(Register.this, "Erreur: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });


        alreadySignIn.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, ReservationLogin.class);
            startActivity(intent);
            finish();
        });
    }


    private String validatePasswordStrength(String password) {
        StringBuilder errorMessage = new StringBuilder();
        

        if (password.length() < 6) {
            errorMessage.append("• Au moins 6 caractères\n");
        }
        

        if (!password.matches(".*[A-Z].*")) {
            errorMessage.append("• Au moins une lettre majuscule\n");
        }
        

        if (!password.matches(".*[a-z].*")) {
            errorMessage.append("• Au moins une lettre minuscule\n");
        }
        

        if (!password.matches(".*[0-9].*")) {
            errorMessage.append("• Au moins un chiffre\n");
        }
        

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errorMessage.append("• Au moins un caractère spécial (!@#$%^&*...)\n");
        }
        
        if (errorMessage.length() > 0) {
            return "Le mot de passe doit contenir :\n" + errorMessage.toString().trim();
        }
        
        return null;
    }
}
