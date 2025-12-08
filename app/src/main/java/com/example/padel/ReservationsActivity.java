package com.example.padel;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReservationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ReservationsAdapter adapter;
    List<Reservation> reservationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);

        recyclerView = findViewById(R.id.rvReservations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample data
        reservationList = new ArrayList<>();
        reservationList.add(new Reservation("Court A", "2025-12-10", "18:00", "4"));
        reservationList.add(new Reservation("Court B", "2025-12-11", "16:00", "2"));

        adapter = new ReservationsAdapter(reservationList);
        recyclerView.setAdapter(adapter);
    }
}
