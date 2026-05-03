package com.example.padel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.padel.firebase.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ReservationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ReservationsAdapter adapter;
    List<Reservation> reservationList;
    TextView tvEmpty, tvUserCredits;
    ProgressBar progressBar;
    Button btnLogout, btnBackMenu, btnAddCredit;

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_reservations);
        

        View mainView = findViewById(R.id.reservationsLayout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }


        firebaseHelper = FirebaseHelper.getInstance(this);

        recyclerView = findViewById(R.id.rvReservations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);
        tvUserCredits = findViewById(R.id.tvUserCredits);


        loadUserCredits();


        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            firebaseHelper.logout();
            Intent intent = new Intent(ReservationsActivity.this, ReservationLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });


        btnBackMenu = findViewById(R.id.btnBackMenu);
        btnBackMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ReservationsActivity.this, MainActivity.class);
            startActivity(intent);
        });


        btnAddCredit = findViewById(R.id.btnAddCredit);
        btnAddCredit.setOnClickListener(v -> {
            Intent intent = new Intent(ReservationsActivity.this, AddCreditActivity.class);
            startActivity(intent);
        });


        reservationList = new ArrayList<>();
        adapter = new ReservationsAdapter(reservationList);
        recyclerView.setAdapter(adapter);


        loadReservationsFromFirebase();
    }

    private void loadReservationsFromFirebase() {

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.GONE);
        }
        
        firebaseHelper.getUserReservations(reservations -> {
            runOnUiThread(() -> {

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                reservationList.clear();
                reservationList.addAll(reservations);
                adapter.notifyDataSetChanged();


                if (reservationList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(View.GONE);
                    }
                }
            });
        });
    }

    private void loadUserCredits() {
        firebaseHelper.getCurrentUserData(user -> {
            runOnUiThread(() -> {
                if (user != null) {
                    tvUserCredits.setText(String.format("Votre crédit: %.2f DT", user.getCredits()));
                } else {
                    tvUserCredits.setText("Votre crédit: 0.00 DT");
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadReservationsFromFirebase();

        loadUserCredits();
    }
}
