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
import com.example.duhis.models.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> users;
    private Consumer<User> onUserClick;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public UserAdapter(List<User> users, Consumer<User> onUserClick) {
        this.users = users;
        this.onUserClick = onUserClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        if (user != null) {
            holder.tvName.setText(user.getFullName());
            holder.tvEmail.setText(user.getEmail());

            String phone = user.getPhoneNumber();
            holder.tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "No phone");

            // Set role badge
            if ("admin".equals(user.getRole())) {
                holder.tvRole.setText("Admin");
                holder.tvRole.setBackgroundResource(R.drawable.ic_role_admin);
            } else {
                holder.tvRole.setText("User");
                holder.tvRole.setBackgroundResource(R.drawable.ic_role_user);
            }

            // Set date
            if (user.getCreatedAt() != null) {
                holder.tvDate.setText(dateFormat.format(user.getCreatedAt().toDate()));
            } else {
                holder.tvDate.setText("N/A");
            }

            holder.cardUser.setOnClickListener(v -> {
                if (onUserClick != null) {
                    onUserClick.accept(user);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardUser;
        TextView tvName, tvEmail, tvPhone, tvRole, tvDate;
        ImageView ivAvatar;

        ViewHolder(View itemView) {
            super(itemView);
            cardUser = itemView.findViewById(R.id.cardUser);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}