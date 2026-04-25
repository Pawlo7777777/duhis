package com.example.duhis.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duhis.R;
import com.example.duhis.models.HealthInfo;

import java.util.List;

public class AdminHealthInfoAdapter extends RecyclerView.Adapter<AdminHealthInfoAdapter.VH> {

    public interface OnEdit   { void onEdit(HealthInfo info); }
    public interface OnDelete { void onDelete(HealthInfo info); }

    private final List<HealthInfo> list;
    private final OnEdit   onEdit;
    private final OnDelete onDelete;

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
        h.tvCategory.setText(info.getCategory());
        h.tvFeatured.setVisibility(info.isFeatured() ? View.VISIBLE : View.GONE);

        h.card.setOnClickListener(v -> onEdit.onEdit(info));
        h.btnEdit.setOnClickListener(v -> onEdit.onEdit(info));
        h.btnDelete.setOnClickListener(v -> onDelete.onDelete(info));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvTitle, tvCategory, tvFeatured;
        ImageButton btnEdit, btnDelete;
        VH(View v) {
            super(v);
            card       = v.findViewById(R.id.card);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvFeatured = v.findViewById(R.id.tvFeatured);
            btnEdit    = v.findViewById(R.id.btnEdit);
            btnDelete  = v.findViewById(R.id.btnDelete);
        }
    }
}