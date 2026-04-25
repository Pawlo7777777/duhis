package com.example.duhis.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duhis.R;
import com.example.duhis.models.HealthInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HealthInfoAdapter extends RecyclerView.Adapter<HealthInfoAdapter.ViewHolder> {

    private List<HealthInfo> items;
    private Consumer<HealthInfo> onItemClick;

    public HealthInfoAdapter(List<HealthInfo> items, Consumer<HealthInfo> onItemClick) {
        this.items = items != null ? items : new ArrayList<>();
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_health_info_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthInfo item = items.get(position);

        if (item != null) {
            holder.tvTitle.setText(item.getTitle() != null ? item.getTitle() : "Untitled");

            String summary = item.getSummary();
            if (summary != null && !summary.isEmpty()) {
                holder.tvSummary.setText(summary);
            } else {
                holder.tvSummary.setText("No summary available");
            }

            // Set category with badge style
            String category = item.getCategory();
            if (category != null && !category.isEmpty()) {
                holder.tvCategory.setText(category);
                holder.tvCategory.setVisibility(View.VISIBLE);

                // Set different colors based on category
                setCategoryColor(holder.tvCategory, category);
            } else {
                holder.tvCategory.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null && item != null) {
                onItemClick.accept(item);
            }
        });
    }

    private void setCategoryColor(TextView tvCategory, String category) {
        switch (category.toLowerCase()) {
            case "prevention":
                tvCategory.setBackgroundResource(R.drawable.bg_category_prevention);
                break;
            case "nutrition":
                tvCategory.setBackgroundResource(R.drawable.bg_category_nutrition);
                break;
            case "mental health":
                tvCategory.setBackgroundResource(R.drawable.bg_category_mental);
                break;
            default:
                tvCategory.setBackgroundResource(R.drawable.bg_category_default);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<HealthInfo> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSummary, tvCategory;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}