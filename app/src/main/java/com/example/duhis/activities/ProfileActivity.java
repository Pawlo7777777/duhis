package com.example.duhis.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.example.duhis.R;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.ImageUploadHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etDob, etAddress, etAllergies;
    private AutoCompleteTextView actvGender, actvBloodType;
    private Button btnSave;
    private View progressOverlay;
    private ProgressBar uploadProgressBar;
    private CircleImageView ivProfileImage;
    private SessionManager session;
    private FirebaseHelper fb;

    private Uri selectedImageUri = null;
    private String currentImageUrl = null;

    private static final String[] GENDERS     = {"Male", "Female", "Prefer not to say"};
    private static final String[] BLOOD_TYPES = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"};

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    // Preview the selected image immediately
                    Glide.with(this)
                            .load(selectedImageUri)
                            .placeholder(R.drawable.ic_default_avatar)
                            .circleCrop()
                            .into(ivProfileImage);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fb      = FirebaseHelper.getInstance(this);
        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadCurrentData();

        btnSave.setOnClickListener(v -> handleSave());
    }

    private void initViews() {
        ivProfileImage  = findViewById(R.id.ivProfileImage);
        etName          = findViewById(R.id.etName);
        etPhone         = findViewById(R.id.etPhone);
        etDob           = findViewById(R.id.etDob);
        etAddress       = findViewById(R.id.etAddress);
        etAllergies     = findViewById(R.id.etAllergies);
        actvGender      = findViewById(R.id.actvGender);
        actvBloodType   = findViewById(R.id.actvBloodType);
        btnSave         = findViewById(R.id.btnSave);
        progressOverlay = findViewById(R.id.progressOverlay);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);

        actvGender.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, GENDERS));
        actvBloodType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, BLOOD_TYPES));

        // Camera icon / tap avatar to pick image
        ivProfileImage.setOnClickListener(v -> openImagePicker());
        findViewById(R.id.ivEditPhoto).setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadCurrentData() {
        String uid = session.getUid();
        if (uid == null) return;
        showProgress(true);

        fb.users().child(uid).get()
                .addOnSuccessListener(snap -> {
                    showProgress(false);
                    if (!snap.exists()) return;

                    etName.setText(snap.child("fullName").getValue(String.class));
                    etPhone.setText(snap.child("phoneNumber").getValue(String.class));
                    etDob.setText(snap.child("dateOfBirth").getValue(String.class));
                    etAddress.setText(snap.child("address").getValue(String.class));
                    etAllergies.setText(snap.child("allergies").getValue(String.class));
                    actvGender.setText(snap.child("gender").getValue(String.class), false);
                    actvBloodType.setText(snap.child("bloodType").getValue(String.class), false);

                    currentImageUrl = snap.child("profileImageUrl").getValue(String.class);
                    if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(currentImageUrl)
                                .placeholder(R.drawable.ic_default_avatar)
                                .circleCrop()
                                .into(ivProfileImage);
                    }
                })
                .addOnFailureListener(e -> showProgress(false));
    }

    // If a new image was selected, upload it first then save; otherwise save directly
    private void handleSave() {
        String name = text(etName);
        if (name.isEmpty()) {
            UIUtils.showToast(this, "Name cannot be empty");
            return;
        }

        if (selectedImageUri != null) {
            uploadImageThenSave(name);
        } else {
            saveProfile(name, currentImageUrl);
        }
    }

    private void uploadImageThenSave(String name) {
        showProgress(true);
        uploadProgressBar.setVisibility(View.VISIBLE);

        ImageUploadHelper.upload(this, selectedImageUri, new ImageUploadHelper.OnUploadResult() {
            @Override
            public void onSuccess(String imageUrl) {
                uploadProgressBar.setVisibility(View.GONE);
                saveProfile(name, imageUrl);
            }

            @Override
            public void onFailure(String error) {
                uploadProgressBar.setVisibility(View.GONE);
                showProgress(false);
                UIUtils.showToast(ProfileActivity.this, "Image upload failed: " + error);
            }
        });
    }

    private void saveProfile(String name, String imageUrl) {
        showProgress(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName",        name);
        updates.put("phoneNumber",     text(etPhone));
        updates.put("dateOfBirth",     text(etDob));
        updates.put("address",         text(etAddress));
        updates.put("allergies",       text(etAllergies));
        updates.put("gender",          actvGender.getText().toString());
        updates.put("bloodType",       actvBloodType.getText().toString());
        updates.put("updatedAt",       System.currentTimeMillis());
        if (imageUrl != null) {
            updates.put("profileImageUrl", imageUrl);
        }

        fb.users().child(session.getUid()).updateChildren(updates)
                .addOnSuccessListener(v -> {
                    showProgress(false);
                    session.updateName(name);
                    if (imageUrl != null){
                        session.updateAvatar(imageUrl);
                    }
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