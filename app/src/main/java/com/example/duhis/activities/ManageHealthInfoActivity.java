package com.example.duhis.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;
import com.example.duhis.R;
import com.example.duhis.adapters.AdminHealthInfoAdapter;
import com.example.duhis.models.HealthInfo;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.UIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageHealthInfoActivity extends AppCompatActivity {

    private RecyclerView rvHealthInfo;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    private AdminHealthInfoAdapter adapter;
    private final List<HealthInfo> infoList = new ArrayList<>();
    private FirebaseHelper fb;

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

        fb = FirebaseHelper.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Manage Health Info");
        toolbar.setNavigationOnClickListener(v -> finish());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvHealthInfo = findViewById(R.id.rvHealthInfo);
        tvEmpty      = findViewById(R.id.tvEmpty);
        fabAdd       = findViewById(R.id.fabAdd);

        adapter = new AdminHealthInfoAdapter(infoList,
                this::showEditSheet,
                this::deleteInfo);
        rvHealthInfo.setLayoutManager(new LinearLayoutManager(this));
        rvHealthInfo.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddSheet());
        loadAll();
    }

    private void loadAll() {
        fb.healthInfo().orderBy("createdAt", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(snap -> {
                    infoList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        HealthInfo h = doc.toObject(HealthInfo.class);
                        if (h != null) { h.setInfoId(doc.getId()); infoList.add(h); }
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(infoList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void showAddSheet() { showSheet(null); }
    private void showEditSheet(HealthInfo info) { showSheet(info); }

    private void showSheet(HealthInfo existing) {
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.Theme_DUHIS_Dialog);
        View v = getLayoutInflater().inflate(R.layout.sheet_health_info_form, null);
        sheet.setContentView(v);

        TextInputEditText etTitle    = v.findViewById(R.id.etTitle);
        TextInputEditText etSummary  = v.findViewById(R.id.etSummary);
        TextInputEditText etContent  = v.findViewById(R.id.etContent);
        AutoCompleteTextView actvCat = v.findViewById(R.id.actvCategory);
        SwitchMaterial swFeatured   = v.findViewById(R.id.swFeatured);
        Button btnSave               = v.findViewById(R.id.btnSave);
        TextView tvSheetTitle        = v.findViewById(R.id.tvSheetTitle);

        actvCat.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, CATEGORIES));

        if (existing != null) {
            tvSheetTitle.setText("Edit Health Info");
            etTitle.setText(existing.getTitle());
            etSummary.setText(existing.getSummary());
            etContent.setText(existing.getContent());
            actvCat.setText(existing.getCategory(), false);
            swFeatured.setChecked(existing.isFeatured());
        } else {
            tvSheetTitle.setText("Add Health Info");
        }

        btnSave.setOnClickListener(btn -> {
            String title    = text(etTitle);
            String summary  = text(etSummary);
            String content  = text(etContent);
            String category = actvCat.getText().toString().trim();
            boolean featured = swFeatured.isChecked();

            if (title.isEmpty() || content.isEmpty() || category.isEmpty()) {
                UIUtils.showToast(this, "Title, content, and category are required");
                return;
            }

            if (existing != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("title",      title);
                updates.put("summary",    summary);
                updates.put("content",    content);
                updates.put("category",   category);
                updates.put("isFeatured", featured);
                updates.put("updatedAt",  Timestamp.now());

                fb.healthInfo().document(existing.getInfoId()).update(updates)
                        .addOnSuccessListener(unused -> { sheet.dismiss(); loadAll(); });
            } else {
                HealthInfo info = new HealthInfo(title, summary, content, category);
                info.setFeatured(featured);
                fb.healthInfo().add(info)
                        .addOnSuccessListener(ref -> { sheet.dismiss(); loadAll(); });
            }
        });

        sheet.show();
    }

    private void deleteInfo(HealthInfo info) {
        UIUtils.showConfirmDialog(this, "Delete", "Delete this health article?", "Delete", () ->
                fb.healthInfo().document(info.getInfoId()).delete()
                        .addOnSuccessListener(v -> { loadAll(); UIUtils.showToast(this, "Deleted"); }));
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}