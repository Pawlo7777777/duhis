package com.example.duhis.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import com.example.duhis.R;
import com.example.duhis.models.HealthInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminHealthInfoAdapter extends RecyclerView.Adapter<AdminHealthInfoAdapter.VH> {

    public interface OnEdit   { void onEdit(HealthInfo info); }
    public interface OnDelete { void onDelete(HealthInfo info); }

    private final List<HealthInfo> list;
    private final OnEdit   onEdit;
    private final OnDelete onDelete;

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public AdminHealthInfoAdapter(List<HealthInfo> list, OnEdit onEdit, OnDelete onDelete) {
        this.list     = list;
        this.onEdit   = onEdit;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_health_info, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        HealthInfo info = list.get(pos);

        h.tvTitle.setText(info.getTitle());

        // Cover image
        if (info.getImageUrl() != null && !info.getImageUrl().isEmpty()) {
            h.ivCover.setVisibility(View.VISIBLE);
            Glide.with(h.itemView.getContext())
                    .load(info.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.img_health_placeholder)
                    .into(h.ivCover);
        } else {
            h.ivCover.setVisibility(View.GONE);
        }

        // Summary
        if (info.getSummary() != null && !info.getSummary().isEmpty()) {
            h.tvSummary.setVisibility(View.VISIBLE);
            h.tvSummary.setText(info.getSummary());
        } else {
            h.tvSummary.setVisibility(View.GONE);
        }

        // Category chip
        if (info.getCategory() != null && !info.getCategory().isEmpty()) {
            h.tvCategory.setVisibility(View.VISIBLE);
            h.tvCategory.setText(info.getCategory());
        } else {
            h.tvCategory.setVisibility(View.GONE);
        }

        // Date
        if (info.getCreatedAt() > 0) {
            h.tvDate.setText(DATE_FMT.format(new Date(info.getCreatedAt())));
        } else {
            h.tvDate.setText("—");
        }

        // Featured badge
        h.tvFeatured.setVisibility(info.isFeatured() ? View.VISIBLE : View.GONE);

        h.card.setOnClickListener(v -> onEdit.onEdit(info));
        h.btnEdit.setOnClickListener(v -> onEdit.onEdit(info));
        h.btnDelete.setOnClickListener(v -> onDelete.onDelete(info));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView ivCover;                                            // ← added
        TextView tvTitle, tvSummary, tvCategory, tvFeatured, tvDate;
        LinearLayout btnEdit, btnDelete;

        VH(View v) {
            super(v);
            card       = v.findViewById(R.id.cardItem);
            ivCover    = v.findViewById(R.id.ivCover);               // ← added
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvSummary  = v.findViewById(R.id.tvSummary);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvFeatured = v.findViewById(R.id.tvFeatured);
            tvDate     = v.findViewById(R.id.tvDate);
            btnEdit    = v.findViewById(R.id.ivEdit);
            btnDelete  = v.findViewById(R.id.ivDelete);
        }
    }
}