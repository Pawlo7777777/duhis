package com.example.duhis.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.example.duhis.R;
import com.example.duhis.utils.UIUtils;
import com.example.duhis.utils.ValidationUtils;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private Button btnReset;
    private View progressOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilEmail        = findViewById(R.id.tilEmail);
        etEmail         = findViewById(R.id.etEmail);
        btnReset        = findViewById(R.id.btnReset);
        progressOverlay = findViewById(R.id.progressOverlay);

        btnReset.setOnClickListener(v -> sendReset());
    }

    private void sendReset() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        tilEmail.setError(null);

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Enter a valid email address");
            return;
        }

        progressOverlay.setVisibility(View.VISIBLE);
        btnReset.setEnabled(false);

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> {
                    progressOverlay.setVisibility(View.GONE);
                    UIUtils.showToast(this, "Reset link sent to " + email);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressOverlay.setVisibility(View.GONE);
                    btnReset.setEnabled(true);
                    tilEmail.setError("Error: " + e.getMessage());
                });
    }
}