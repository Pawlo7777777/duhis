package com.example.duhis.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duhis.R;
import com.example.duhis.models.EmergencyContact;

import java.util.List;
import java.util.function.Consumer;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {

    private List<EmergencyContact> contacts;
    private Consumer<String> onCallClick;

    public EmergencyContactAdapter(List<EmergencyContact> contacts, Consumer<String> onCallClick) {
        this.contacts = contacts;
        this.onCallClick = onCallClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyContact contact = contacts.get(position);

        if (contact != null) {
            holder.tvName.setText(contact.getName());
            holder.tvDescription.setText(contact.getDescription());
            holder.tvPhone.setText(contact.getPhoneNumber());

            // Set icon based on contact type
            setContactIcon(holder.ivIcon, contact.getCategory());

            // Set card background color based on urgency/sort order
            setCardColor(holder.cardContact, contact.getSortOrder());

            // Call button click listener
            holder.btnCall.setOnClickListener(v -> {
                if (onCallClick != null) {
                    onCallClick.accept(contact.getPhoneNumber());
                }
            });

            // Make entire card clickable as well
            holder.cardContact.setOnClickListener(v -> {
                if (onCallClick != null) {
                    onCallClick.accept(contact.getPhoneNumber());
                }
            });
        }
    }

    private void setContactIcon(ImageView ivIcon, String type) {
        switch (type) {
            case "health_center":
                ivIcon.setImageResource(R.drawable.ic_health_center);
                break;
            case "hospital":
                ivIcon.setImageResource(R.drawable.ic_hospital);
                break;
            case "ambulance":
                ivIcon.setImageResource(R.drawable.ic_ambulance);
                break;
            case "fire":
                ivIcon.setImageResource(R.drawable.ic_fire_department);
                break;
            case "police":
                ivIcon.setImageResource(R.drawable.ic_police);
                break;
            default:
                ivIcon.setImageResource(R.drawable.ic_emergency_call);
                break;
        }
    }

    private void setCardColor(CardView cardView, int sortOrder) {
        // Priority colors based on sort order (lower number = higher priority)
        switch (sortOrder) {
            case 3: // Ambulance
            case 4: // Fire
            case 5: // Police
                // Keep default white with red tint for high priority
                cardView.setCardBackgroundColor(0xFFFFF3F3);
                break;
            default:
                cardView.setCardBackgroundColor(0xFFFFFFFF);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return contacts != null ? contacts.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardContact;
        ImageView ivIcon;
        TextView tvName, tvDescription, tvPhone;
        View btnCall;

        ViewHolder(View itemView) {
            super(itemView);
            cardContact = itemView.findViewById(R.id.cardContact);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnCall = itemView.findViewById(R.id.btnCall);
        }
    }
}