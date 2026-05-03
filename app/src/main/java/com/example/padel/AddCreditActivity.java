package com.example.padel;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.padel.firebase.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity for adding credits to user account with fake payment processing
 */
public class AddCreditActivity extends AppCompatActivity {

    // UI Elements
    private ImageButton btnBack;
    private TextView tvCurrentBalance;
    private TextInputEditText etCardNumber, etExpiry, etCvv, etCardholderName;
    private RadioGroup rgCreditOptions;
    private TextView tvCreditsToAdd, tvTotalPrice;
    private MaterialButton btnPay;
    private FrameLayout loadingOverlay, successOverlay;
    private TextView tvLoadingMessage, tvSuccessMessage;
    private MaterialButton btnDone;


    private FirebaseHelper firebaseHelper;
    private double currentCredits = 0;
    private int selectedCredits = 0;
    private double selectedPrice = 0;


    private static final int CREDITS_50 = 50;
    private static final double PRICE_50 = 25.00;
    private static final int CREDITS_100 = 100;
    private static final double PRICE_100 = 45.00;
    private static final int CREDITS_200 = 200;
    private static final double PRICE_200 = 80.00;
    private static final int CREDITS_500 = 500;
    private static final double PRICE_500 = 175.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_add_credit);


        View mainView = findViewById(R.id.addCreditLayout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }


        firebaseHelper = FirebaseHelper.getInstance(this);


        initViews();


        setupListeners();


        loadCurrentBalance();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);

        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCvv = findViewById(R.id.etCvv);
        etCardholderName = findViewById(R.id.etCardholderName);

        rgCreditOptions = findViewById(R.id.rgCreditOptions);
        tvCreditsToAdd = findViewById(R.id.tvCreditsToAdd);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        btnPay = findViewById(R.id.btnPay);

        loadingOverlay = findViewById(R.id.loadingOverlay);
        successOverlay = findViewById(R.id.successOverlay);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        btnDone = findViewById(R.id.btnDone);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());


        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;


                String text = s.toString().replace(" ", "");
                StringBuilder formatted = new StringBuilder();


                for (int i = 0; i < text.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(text.charAt(i));
                }

                etCardNumber.setText(formatted.toString());
                etCardNumber.setSelection(formatted.length());
                isFormatting = false;
            }
        });


        etExpiry.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String text = s.toString().replace("/", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < text.length() && i < 4; i++) {
                    if (i == 2) {
                        formatted.append("/");
                    }
                    formatted.append(text.charAt(i));
                }

                etExpiry.setText(formatted.toString());
                etExpiry.setSelection(formatted.length());
                isFormatting = false;
            }
        });


        rgCreditOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb50Credits) {
                selectedCredits = CREDITS_50;
                selectedPrice = PRICE_50;
            } else if (checkedId == R.id.rb100Credits) {
                selectedCredits = CREDITS_100;
                selectedPrice = PRICE_100;
            } else if (checkedId == R.id.rb200Credits) {
                selectedCredits = CREDITS_200;
                selectedPrice = PRICE_200;
            } else if (checkedId == R.id.rb500Credits) {
                selectedCredits = CREDITS_500;
                selectedPrice = PRICE_500;
            }
            updateSummary();
        });


        btnPay.setOnClickListener(v -> processPayment());


        btnDone.setOnClickListener(v -> finish());
    }

    private void loadCurrentBalance() {
        firebaseHelper.getCurrentUserData(user -> {
            runOnUiThread(() -> {
                if (user != null) {
                    currentCredits = user.getCredits();
                    tvCurrentBalance.setText(String.format("Current Balance: %.2f DT", currentCredits));
                } else {
                    tvCurrentBalance.setText("Current Balance: 0.00 DT");
                }
            });
        });
    }

    private void updateSummary() {
        tvCreditsToAdd.setText(String.valueOf(selectedCredits));
        tvTotalPrice.setText(String.format("%.2f DT", selectedPrice));
    }

    private void processPayment() {

        if (!validateInputs()) {
            return;
        }


        showLoading("Processing payment...");


        Handler handler = new Handler(Looper.getMainLooper());


        handler.postDelayed(() -> {
            updateLoadingMessage("Validating card...");
        }, 1000);


        handler.postDelayed(() -> {
            updateLoadingMessage("Contacting bank...");
        }, 2500);


        handler.postDelayed(() -> {
            updateLoadingMessage("Processing transaction...");
        }, 3500);


        handler.postDelayed(() -> {
            updateLoadingMessage("Finalizing...");
            updateCreditsInDatabase();
        }, 4500);
    }

    private boolean validateInputs() {

        String cardNumber = etCardNumber.getText().toString().replace(" ", "");
        if (cardNumber.length() < 16) {
            Toast.makeText(this, "Please enter a valid card number", Toast.LENGTH_SHORT).show();
            etCardNumber.requestFocus();
            return false;
        }


        String expiry = etExpiry.getText().toString();
        if (expiry.length() < 5) {
            Toast.makeText(this, "Please enter a valid expiry date (MM/YY)", Toast.LENGTH_SHORT).show();
            etExpiry.requestFocus();
            return false;
        }


        String cvv = etCvv.getText().toString();
        if (cvv.length() < 3) {
            Toast.makeText(this, "Please enter a valid CVV", Toast.LENGTH_SHORT).show();
            etCvv.requestFocus();
            return false;
        }


        String cardholderName = etCardholderName.getText().toString().trim();
        if (cardholderName.isEmpty()) {
            Toast.makeText(this, "Please enter the cardholder name", Toast.LENGTH_SHORT).show();
            etCardholderName.requestFocus();
            return false;
        }


        if (selectedCredits == 0) {
            Toast.makeText(this, "Please select a credit amount", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showLoading(String message) {
        tvLoadingMessage.setText(message);
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    private void updateLoadingMessage(String message) {
        tvLoadingMessage.setText(message);
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }

    private void showSuccess(double newBalance) {
        hideLoading();
        tvSuccessMessage.setText(String.format("%d credits added!\nNew balance: %.2f DT", selectedCredits, newBalance));
        successOverlay.setVisibility(View.VISIBLE);
    }

    private void updateCreditsInDatabase() {
        double newCredits = currentCredits + selectedCredits;

        firebaseHelper.updateUserCredits(newCredits, new FirebaseHelper.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    currentCredits = newCredits;
                    showSuccess(newCredits);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(AddCreditActivity.this, 
                            "Payment failed: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
