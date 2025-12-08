package com.example.padel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements DateAdapter.OnDateClickListener, TimeAdapter.OnTimeClickListener {

    Spinner spinnerCourts;
    EditText etPlayers;
    Button btnReserve;

    RecyclerView rvDates, rvTimes;

    String selectedDate = "";
    String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerCourts = findViewById(R.id.spinnerCourts);
        etPlayers = findViewById(R.id.etPlayers);
        btnReserve = findViewById(R.id.btnReserve);
        rvDates = findViewById(R.id.rvDate);
        rvTimes = findViewById(R.id.rvTime);

        // Spinner
        String[] courts = {"Court 1", "Court 2", "Court 3"};
        spinnerCourts.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                courts
        ));

        // Dates RecyclerView
        rvDates.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        DateAdapter dateAdapter = new DateAdapter(generateDates(), this);
        rvDates.setAdapter(dateAdapter);

        // Times RecyclerView
        rvTimes.setLayoutManager(new GridLayoutManager(this, 3));
        TimeAdapter timeAdapter = new TimeAdapter(generateTimes(), this);
        rvTimes.setAdapter(timeAdapter);

        btnReserve.setOnClickListener(v -> reserve());
    }

    private void reserve() {
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ReservationLogin.class);
        intent.putExtra("date", selectedDate);
        intent.putExtra("time", selectedTime);
        startActivity(intent);
    }

    // CALLBACKS
    @Override
    public void onDateSelected(String date) {
        selectedDate = date;
    }

    @Override
    public void onTimeSelected(String time) {
        selectedTime = time;
    }

    // DATA
    private ArrayList<String> generateDates() {
        ArrayList<String> dates = new ArrayList<>();
        Calendar c = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            dates.add(
                    c.get(Calendar.DAY_OF_MONTH) + "/" +
                            (c.get(Calendar.MONTH) + 1)
            );
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }

    private ArrayList<String> generateTimes() {
        ArrayList<String> times = new ArrayList<>();
        times.add("06:30");
        times.add("08:00");
        times.add("09:30");
        times.add("11:00");
        times.add("12:30");
        times.add("14:00");
        times.add("15:30");
        times.add("17:00");
        times.add("20:00");
        times.add("21:30");
        times.add("23:00");
        return times;
}
}