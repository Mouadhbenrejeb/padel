package com.example.padel;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.padel.firebase.FirebaseHelper;
import com.example.padel.models.Court;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements DateAdapter.OnDateClickListener, TimeAdapter.OnTimeClickListener {

    Spinner spinnerCourts;
    EditText etPlayers;
    Button btnReserve;

    RecyclerView rvDates, rvTimes;

    String selectedDate = "";
    String selectedTime = "";

    private FirebaseHelper firebaseHelper;
    private List<Court> courtsList = new ArrayList<>();
    private TimeAdapter timeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_main);
        
        // Apply window insets for edge-to-edge
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        // Initialize Firebase Helper
        firebaseHelper = FirebaseHelper.getInstance(this);

        spinnerCourts = findViewById(R.id.spinnerCourts);
        etPlayers = findViewById(R.id.etPlayers);
        btnReserve = findViewById(R.id.btnReserve);
        rvDates = findViewById(R.id.rvDate);
        rvTimes = findViewById(R.id.rvTime);

        // Set up InputFilter for players EditText to restrict input to numbers 1-4
        setupPlayersInputFilter();

        // Load courts from Firebase
        loadCourtsFromFirebase();

        // Dates RecyclerView
        rvDates.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        ArrayList<String[]> dates = generateDates();
        DateAdapter dateAdapter = new DateAdapter(dates, this);
        rvDates.setAdapter(dateAdapter);
        
        // Set default selected date to today (first item in the list)
        if (!dates.isEmpty()) {
            selectedDate = dates.get(0)[1]; // The date string (e.g., "15/12")
        }

        // Times RecyclerView
        rvTimes.setLayoutManager(new GridLayoutManager(this, 3));
        timeAdapter = new TimeAdapter(generateTimes(), this);
        rvTimes.setAdapter(timeAdapter);

        btnReserve.setOnClickListener(v -> reserve());
    }

    private void setupPlayersInputFilter() {
        // InputFilter to restrict input to numbers 1-4 only
        InputFilter playersFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                // Allow empty input (for deletion)
                if (source.length() == 0) {
                    return null;
                }

                // Build the resulting string
                StringBuilder builder = new StringBuilder(dest);
                builder.replace(dstart, dend, source.subSequence(start, end).toString());
                String result = builder.toString();

                // Check if result is empty (allow it for typing)
                if (result.isEmpty()) {
                    return null;
                }

                // Only allow single digit numbers 1-4
                try {
                    int value = Integer.parseInt(result);
                    if (value >= 1 && value <= 4) {
                        return null; // Accept the input
                    }
                } catch (NumberFormatException e) {
                    // Not a valid number
                }

                // Reject the input
                return "";
            }
        };

        // Apply the filter along with max length of 1
        etPlayers.setFilters(new InputFilter[]{playersFilter, new InputFilter.LengthFilter(1)});
    }

    private void loadCourtsFromFirebase() {
        firebaseHelper.getCourts(courts -> {
            courtsList = courts;
            List<String> courtNames = new ArrayList<>();
            
            if (courts.isEmpty()) {
                // Fallback to default courts if Firebase is empty
                courtNames.add("Court 1");
                courtNames.add("Court 2");
                courtNames.add("Court 3");
            } else {
                for (Court court : courts) {
                    courtNames.add(court.getName());
                }
            }
            
            runOnUiThread(() -> {
                spinnerCourts.setAdapter(new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        courtNames
                ));
                
                // Add listener to refresh time slots when court changes
                spinnerCourts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // Refresh time slot availability for the selected date
                        if (!selectedDate.isEmpty()) {
                            String selectedCourt = parent.getItemAtPosition(position).toString();
                            selectedTime = ""; // Reset selected time
                            if (timeAdapter != null) {
                                timeAdapter.resetSelection();
                            }
                            firebaseHelper.getBookedTimeSlots(selectedCourt, selectedDate, bookedTimes -> {
                                runOnUiThread(() -> {
                                    if (timeAdapter != null) {
                                        timeAdapter.setDisabledTimes(bookedTimes);
                                    }
                                });
                            });
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                    }
                });
            });
        });
    }

    private void reserve() {
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected court from spinner
        String selectedCourt = spinnerCourts.getSelectedItem() != null ? 
                spinnerCourts.getSelectedItem().toString() : "Court 1";
        
        // Get players count (optional)
        String players = etPlayers.getText().toString().trim();
        
        // Only validate players if a value is provided
        if (!players.isEmpty()) {
            try {
                int playersCount = Integer.parseInt(players);
                if (playersCount < 1 || playersCount > 4) {
                    etPlayers.setError("Number of players must be between 1 and 4");
                    etPlayers.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etPlayers.setError("Please enter a valid number (1-4)");
                etPlayers.requestFocus();
                return;
            }
        }

        Intent intent = new Intent(this, ReservationLogin.class);
        intent.putExtra("court", selectedCourt);
        intent.putExtra("date", selectedDate);
        intent.putExtra("time", selectedTime);
        intent.putExtra("players", players);
        startActivity(intent);
    }

    // CALLBACKS
    @Override
    public void onDateSelected(String date) {
        selectedDate = date;
        selectedTime = ""; // Reset selected time when date changes
        
        // Reset time adapter selection
        if (timeAdapter != null) {
            timeAdapter.resetSelection();
        }
        
        // Get selected court
        String selectedCourt = spinnerCourts.getSelectedItem() != null ? 
                spinnerCourts.getSelectedItem().toString() : "Court 1";
        
        // Fetch booked time slots from Firebase
        firebaseHelper.getBookedTimeSlots(selectedCourt, date, bookedTimes -> {
            runOnUiThread(() -> {
                if (timeAdapter != null) {
                    timeAdapter.setDisabledTimes(bookedTimes);
                }
            });
        });
    }

    @Override
    public void onTimeSelected(String time) {
        selectedTime = time;
    }

    // DATA
    private ArrayList<String[]> generateDates() {
        ArrayList<String[]> dates = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (int i = 0; i < 7; i++) {
            String dayName = dayNames[c.get(Calendar.DAY_OF_WEEK) - 1];
            String dateStr = c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1);
            dates.add(new String[]{dayName, dateStr});
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