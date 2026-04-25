package com.example.duhis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.duhis.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.example.duhis.R;
import com.example.duhis.models.User;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;
import com.example.duhis.utils.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPhone, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private View progressOverlay;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        initViews();
        setListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressOverlay = findViewById(R.id.progressOverlay);
    }

    private void setListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = text(etName);
        String email = text(etEmail);
        String phone = text(etPhone);
        String pass = text(etPassword);
        String confirm = text(etConfirmPassword);

        clearErrors();
        boolean valid = true;

        if (!ValidationUtils.isValidName(name)) {
            tilName.setError("Enter your full name");
            valid = false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Enter a valid email");
            valid = false;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            tilPhone.setError("Enter a valid PH phone number (09XXXXXXXXX)");
            valid = false;
        }
        if (!ValidationUtils.isValidPassword(pass)) {
            tilPassword.setError("Password must be at least 8 characters");
            valid = false;
        }
        if (!ValidationUtils.passwordsMatch(pass, confirm)) {
            tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        }
        if (!valid) return;

        UIUtils.hideKeyboard(this);
        showProgress(true);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    User user = new User(uid, name, email, phone);

                    FirebaseHelper.getInstance().users().document(uid).set(user)
                            .addOnSuccessListener(v -> {
                                showProgress(false);
                                new SessionManager(this).createSession(uid, name, email, "user", phone);
                                FirebaseHelper.getInstance().setUserOnline(uid);

                                Intent intent = new Intent(this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showProgress(false);
                                UIUtils.showToast(this, "Failed to save user data: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    UIUtils.showSnackbarError(btnRegister, "Registration failed: " + e.getMessage());
                });
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}