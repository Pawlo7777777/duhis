package com.example.duhis.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;

import com.example.duhis.R;
import com.example.duhis.adapters.AdminHealthInfoAdapter;
import com.example.duhis.models.HealthInfo;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.ImageUploadHelper;
import com.example.duhis.utils.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageHealthInfoActivity extends AppCompatActivity {

    private RecyclerView rvHealthInfo;
    private View tvEmpty;
    private FloatingActionButton fabAdd;

    private AdminHealthInfoAdapter adapter;
    private final List<HealthInfo> infoList = new ArrayList<>();
    private FirebaseHelper fb;

    private Uri selectedImageUri = null;
    private ImageView currentSheetCoverPreview;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null
                        && result.getData().getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (currentSheetCoverPreview != null) {
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .into(currentSheetCoverPreview);
                    }
                }
            });

    private static final String[] CATEGORIES = {
            "Prevention", "Nutrition", "Mental Health", "Maternal Health",
            "Child Health", "Senior Health", "Dental Health", "Emergency Tips",
            "Infectious Diseases", "General Wellness"
    };

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_health_info);

        fb = FirebaseHelper.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Health Info");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvHealthInfo = findViewById(R.id.rvHealthInfo);
        tvEmpty      = findViewById(R.id.tvEmpty);
        fabAdd       = findViewById(R.id.fabAdd);

        adapter = new AdminHealthInfoAdapter(infoList, this::showEditSheet, this::deleteInfo);
        rvHealthInfo.setLayoutManager(new LinearLayoutManager(this));
        rvHealthInfo.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showSheet(null));
        loadAll();
    }

    private void loadAll() {
        fb.healthInfo().orderByChild("createdAt").get()
                .addOnSuccessListener(snap -> {
                    infoList.clear();
                    for (DataSnapshot doc : snap.getChildren()) {
                        HealthInfo h = doc.getValue(HealthInfo.class);
                        if (h != null) {
                            h.setInfoId(doc.getKey());
                            infoList.add(h);
                        }
                    }
                    Collections.reverse(infoList);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(infoList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void showEditSheet(HealthInfo h) { showSheet(h); }

    private void showSheet(HealthInfo existing) {
        selectedImageUri = null;

        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.sheet_health_info_form, null);
        sheet.setContentView(v);

        ImageView ivCoverPreview    = v.findViewById(R.id.ivCoverPreview);
        LinearLayout llPickImage    = v.findViewById(R.id.llPickImage);
        ProgressBar pbUpload        = v.findViewById(R.id.pbUpload);
        TextInputEditText etTitle   = v.findViewById(R.id.etTitle);
        TextInputEditText etSummary = v.findViewById(R.id.etSummary);
        TextInputEditText etContent = v.findViewById(R.id.etContent);
        AutoCompleteTextView actvCat= v.findViewById(R.id.actvCategory);
        SwitchMaterial swFeatured   = v.findViewById(R.id.swFeatured);
        Button btnSave              = v.findViewById(R.id.btnSave);
        TextView tvSheetTitle       = v.findViewById(R.id.tvSheetTitle);

        currentSheetCoverPreview = ivCoverPreview;

        actvCat.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, CATEGORIES));

        if (existing != null) {
            tvSheetTitle.setText("Edit Health Info");
            etTitle.setText(existing.getTitle());
            etSummary.setText(existing.getSummary());
            etContent.setText(existing.getContent());
            actvCat.setText(existing.getCategory(), false);
            swFeatured.setChecked(existing.isFeatured());

            if (existing.getImageUrl() != null && !existing.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(existing.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.img_health_placeholder)
                        .into(ivCoverPreview);
            }
        } else {
            tvSheetTitle.setText("Add Health Info");
        }

        llPickImage.setOnClickListener(pick -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(btn -> {
            String title     = text(etTitle);
            String summary   = text(etSummary);
            String content   = text(etContent);
            String category  = actvCat.getText().toString().trim();
            boolean featured = swFeatured.isChecked();

            if (title.isEmpty() || content.isEmpty() || category.isEmpty()) {
                UIUtils.showToast(this, "Title, content, and category are required");
                return;
            }

            if (selectedImageUri != null) {
                // ✅ Cloudinary upload — no Firebase Storage involved
                pbUpload.setVisibility(View.VISIBLE);
                btnSave.setEnabled(false);

                ImageUploadHelper.upload(this, selectedImageUri, new ImageUploadHelper.OnUploadResult() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        pbUpload.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        saveToDatabase(title, summary, content, category,
                                featured, imageUrl, existing, sheet);
                    }

                    @Override
                    public void onFailure(String error) {
                        pbUpload.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        UIUtils.showToast(ManageHealthInfoActivity.this,
                                "Image upload failed: " + error);
                    }
                });

            } else {
                // No new image selected — keep existing imageUrl or save without one
                String existingUrl = existing != null ? existing.getImageUrl() : null;
                saveToDatabase(title, summary, content, category, featured,
                        existingUrl, existing, sheet);
            }
        });

        sheet.show();
    }

    private void saveToDatabase(String title, String summary, String content,
                                String category, boolean featured, String imageUrl,
                                HealthInfo existing, BottomSheetDialog sheet) {
        if (existing != null) {
            // Update existing record
            Map<String, Object> updates = new HashMap<>();
            updates.put("title",      title);
            updates.put("summary",    summary);
            updates.put("content",    content);
            updates.put("category",   category);
            updates.put("featured", featured);
            updates.put("updatedAt",  System.currentTimeMillis());
            if (imageUrl != null) updates.put("imageUrl", imageUrl);

            fb.healthInfo().child(existing.getInfoId()).updateChildren(updates)
                    .addOnSuccessListener(unused -> { sheet.dismiss(); loadAll(); })
                    .addOnFailureListener(e ->
                            UIUtils.showToast(this, "Save failed: " + e.getMessage()));
        } else {
            // Create new record
            String newKey = fb.healthInfo().push().getKey();
            if (newKey == null) {
                UIUtils.showToast(this, "Failed to generate key, try again");
                return;
            }

            HealthInfo info = new HealthInfo(title, summary, content, category);
            info.setInfoId(newKey);
            info.setFeatured(featured);
            info.setCreatedAt(System.currentTimeMillis());
            info.setUpdatedAt(System.currentTimeMillis());
            if (imageUrl != null) info.setImageUrl(imageUrl);

            fb.healthInfo().child(newKey).setValue(info)
                    .addOnSuccessListener(unused -> { sheet.dismiss(); loadAll(); })
                    .addOnFailureListener(e ->
                            UIUtils.showToast(this, "Save failed: " + e.getMessage()));
        }
    }

    private void deleteInfo(HealthInfo info) {
        UIUtils.showConfirmDialog(this, "Delete", "Delete this health article?", "Delete", () ->
                fb.healthInfo().child(info.getInfoId()).removeValue()
                        .addOnSuccessListener(unused -> {
                            loadAll();
                            UIUtils.showToast(this, "Deleted");
                        }));
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}