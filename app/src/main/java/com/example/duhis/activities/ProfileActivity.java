package com.example.duhis.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.example.duhis.R;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etDob, etAddress, etAllergies;
    private AutoCompleteTextView actvGender, actvBloodType;
    private Button btnSave;
    private View progressOverlay;
    private SessionManager session;
    private FirebaseHelper fb;

    private static final String[] GENDERS     = {"Male", "Female", "Prefer not to say"};
    private static final String[] BLOOD_TYPES = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fb      = FirebaseHelper.getInstance();
        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Profile");
        toolbar.setNavigationOnClickListener(v -> finish());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
        loadCurrentData();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void initViews() {
        etName        = findViewById(R.id.etName);
        etPhone       = findViewById(R.id.etPhone);
        etDob         = findViewById(R.id.etDob);
        etAddress     = findViewById(R.id.etAddress);
        etAllergies   = findViewById(R.id.etAllergies);
        actvGender    = findViewById(R.id.actvGender);
        actvBloodType = findViewById(R.id.actvBloodType);
        btnSave       = findViewById(R.id.btnSave);
        progressOverlay = findViewById(R.id.progressOverlay);

        actvGender.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, GENDERS));
        actvBloodType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, BLOOD_TYPES));
    }

    private void loadCurrentData() {
        String uid = session.getUid();
        if (uid == null) return;
        progressOverlay.setVisibility(View.VISIBLE);

        fb.users().document(uid).get().addOnSuccessListener(doc -> {
            progressOverlay.setVisibility(View.GONE);
            if (!doc.exists()) return;
            etName.setText(doc.getString("fullName"));
            etPhone.setText(doc.getString("phoneNumber"));
            etDob.setText(doc.getString("dateOfBirth"));
            etAddress.setText(doc.getString("address"));
            etAllergies.setText(doc.getString("allergies"));
            actvGender.setText(doc.getString("gender"), false);
            actvBloodType.setText(doc.getString("bloodType"), false);
        }).addOnFailureListener(e -> progressOverlay.setVisibility(View.GONE));
    }

    private void saveProfile() {
        String name   = text(etName);
        String phone  = text(etPhone);

        if (name.isEmpty()) { UIUtils.showToast(this, "Name cannot be empty"); return; }

        showProgress(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName",    name);
        updates.put("phoneNumber", phone);
        updates.put("dateOfBirth", text(etDob));
        updates.put("address",     text(etAddress));
        updates.put("allergies",   text(etAllergies));
        updates.put("gender",      actvGender.getText().toString());
        updates.put("bloodType",   actvBloodType.getText().toString());
        updates.put("updatedAt",   Timestamp.now());

        fb.users().document(session.getUid()).update(updates)
                .addOnSuccessListener(v -> {
                    showProgress(false);
                    session.updateName(name);
                    UIUtils.showToast(this, "Profile updated successfully!");
                    finish();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    UIUtils.showToast(this, "Update failed: " + e.getMessage());
                });
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}