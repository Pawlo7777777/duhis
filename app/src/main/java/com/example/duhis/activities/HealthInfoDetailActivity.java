package com.example.duhis.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.duhis.R;
import com.example.duhis.models.HealthInfo;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.UIUtils;

public class HealthInfoDetailActivity extends AppCompatActivity {

    private ImageView ivCover;
    private TextView tvTitle, tvCategory, tvContent, tvDate;
    private View progressOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_info_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivCover         = findViewById(R.id.ivCover);
        tvTitle         = findViewById(R.id.tvTitle);
        tvCategory      = findViewById(R.id.tvCategory);
        tvContent       = findViewById(R.id.tvContent);
        tvDate          = findViewById(R.id.tvDate);
        progressOverlay = findViewById(R.id.progressOverlay);

        String infoId = getIntent().getStringExtra("infoId");
        if (infoId == null) { finish(); return; }

        loadInfo(infoId);
    }

    private void loadInfo(String infoId) {
        progressOverlay.setVisibility(View.VISIBLE);
        FirebaseHelper.getInstance().healthInfo().document(infoId).get()
                .addOnSuccessListener(doc -> {
                    progressOverlay.setVisibility(View.GONE);
                    if (!doc.exists()) return;
                    HealthInfo info = doc.toObject(HealthInfo.class);
                    if (info == null) return;

                    tvTitle.setText(info.getTitle());
                    tvCategory.setText(info.getCategory());
                    tvContent.setText(info.getContent());

                    if (info.getCreatedAt() != null) {
                        tvDate.setText(UIUtils.formatDate(info.getCreatedAt().toDate()));
                    }

                    if (info.getImageUrl() != null && !info.getImageUrl().isEmpty()) {
                        Glide.with(this).load(info.getImageUrl())
                                .placeholder(R.drawable.img_health_placeholder)
                                .into(ivCover);
                    }
                })
                .addOnFailureListener(e -> {
                    progressOverlay.setVisibility(View.GONE);
                    UIUtils.showToast(this, "Failed to load health info");
                    finish();
                });
    }
}