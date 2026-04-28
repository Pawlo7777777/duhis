package com.example.duhis.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duhis.R;
import com.example.duhis.models.Notification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;
    private Consumer<Notification> onItemClick;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

    public NotificationAdapter(List<Notification> notifications, Consumer<Notification> onItemClick) {
        this.notifications = notifications;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notifications, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        if (notification != null) {
            // Set title and message
            holder.tvTitle.setText(notification.getTitle() != null ? notification.getTitle() : "Notification");
            holder.tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "");

            // Set timestamp
            if (notification.getCreatedAt() != 0) {
                holder.tvTime.setText(dateFormat.format(new java.util.Date(notification.getCreatedAt())));
            } else {
                holder.tvTime.setText("Just now");
            }

            // Set read/unread styling
            if (notification.isRead()) {
                holder.cardNotification.setCardBackgroundColor(
                        holder.itemView.getContext().getColor(R.color.white));
                holder.tvTitle.setTypeface(null, android.graphics.Typeface.NORMAL);
                holder.ivUnreadIndicator.setVisibility(View.GONE);
            } else {
                holder.cardNotification.setCardBackgroundColor(
                        holder.itemView.getContext().getColor(R.color.unread_background));
                holder.tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                holder.ivUnreadIndicator.setVisibility(View.VISIBLE);
            }

            // Set icon based on notification type
            setNotificationIcon(holder.ivIcon, notification.getType());

            // Click listener
            holder.itemView.setOnClickListener(v -> {
                if (onItemClick != null) {
                    onItemClick.accept(notification);
                }
            });
        }
    }

    private void setNotificationIcon(ImageView ivIcon, String type) {
        if (type == null) {
            ivIcon.setImageResource(R.drawable.ic_notification_default);
            return;
        }

        switch (type.toLowerCase()) {
            case "appointment":
                ivIcon.setImageResource(R.drawable.ic_notification_appointment);
                break;
            case "reminder":
                ivIcon.setImageResource(R.drawable.ic_notification_reminder);
                break;
            case "alert":
                ivIcon.setImageResource(R.drawable.ic_notification_alert);
                break;
            case "message":
                ivIcon.setImageResource(R.drawable.ic_notification_message);
                break;
            case "health_tip":
                ivIcon.setImageResource(R.drawable.ic_notification_health);
                break;
            default:
                ivIcon.setImageResource(R.drawable.ic_notification_default);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardNotification;
        ImageView ivIcon;
        View ivUnreadIndicator;
        TextView tvTitle, tvMessage, tvTime;

        @SuppressLint("WrongViewCast")
        ViewHolder(View itemView) {
            super(itemView);
            cardNotification = itemView.findViewById(R.id.cardNotification);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivUnreadIndicator = itemView.findViewById(R.id.ivUnreadIndicator);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}