package com.example.duhis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.duhis.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.duhis.R;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;
import com.example.duhis.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private View progressOverlay;

    private FirebaseAuth auth;
    private SessionManager session;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // CRITICAL: Initialize Firebase first
        FirebaseApp.initializeApp(this);

        // Initialize FirebaseHelper with Context
        firebaseHelper = FirebaseHelper.getInstance(this);
        auth = firebaseHelper.getAuth();
        session = new SessionManager(this);

        initViews();
        setListeners();

        // Check if user is already logged in
        checkAutoLogin();
    }

    private void checkAutoLogin() {
        if (session.isLoggedIn() && auth.getCurrentUser() != null) {
            navigateToDashboard(session.getRole());
        }
    }

    private void navigateToDashboard(String role) {
        // Null-safe role check
        Intent intent;
        if ("admin".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        tilEmail         = findViewById(R.id.tilEmail);
        tilPassword      = findViewById(R.id.tilPassword);
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        tvRegister       = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressOverlay  = findViewById(R.id.progressOverlay);
    }

    private void setListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validate inputs
        boolean valid = true;
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            valid = false;
        }
        if (!valid) return;

        // Hide keyboard and show progress
        UIUtils.hideKeyboard(this);
        showProgress(true);

        // Attempt Firebase sign in
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        showProgress(false);
                        UIUtils.showToast(this, "User not found");
                        return;
                    }
                    loadUserAndNavigate(user.getUid());
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    UIUtils.showSnackbarError(btnLogin, "Login failed: " + e.getMessage());
                });
    }

    private void loadUserAndNavigate(String uid) {
        firebaseHelper.users().child(uid).get()
                .addOnSuccessListener(snap -> {
                    showProgress(false);

                    if (!snap.exists()) {
                        UIUtils.showToast(this, "User data not found. Please register.");
                        auth.signOut();
                        session.logout();
                        return;
                    }

                    String name  = snap.child("fullName").getValue(String.class);
                    String email = snap.child("email").getValue(String.class);
                    String role  = snap.child("role").getValue(String.class);
                    String phone = snap.child("phoneNumber").getValue(String.class);

                    session.createSession(uid, name, email, role, phone);
                    firebaseHelper.setUserOnline(uid);
                    navigateToDashboard(role);
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    UIUtils.showToast(this, "Error loading user: " + e.getMessage());
                    Log.d("Errorrr", e.getMessage());
                    auth.signOut();
                    session.logout();
                });
    }
    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}