package com.example.duhis.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duhis.R;
import com.example.duhis.models.Appointment;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    public interface OnItemClick { void onClick(Appointment appt); }

    private final List<Appointment> list;
    private final OnItemClick listener;

    public AppointmentAdapter(List<Appointment> list, OnItemClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Appointment a = list.get(pos);
        h.tvType.setText(a.getConsultationType());
        h.tvDate.setText(a.getDate() + "  •  " + a.getTime());
        h.tvStatus.setText(a.getStatus());

        int bgColor, textColor;
        switch (a.getStatus()) {
            case Appointment.STATUS_APPROVED:
                bgColor   = R.color.duhis_green_light;
                textColor = R.color.status_approved;
                break;
            case Appointment.STATUS_CANCELLED:
                bgColor   = R.color.duhis_red_light;
                textColor = R.color.status_cancelled;
                break;
            case Appointment.STATUS_COMPLETED:
                bgColor   = R.color.duhis_blue_light;
                textColor = R.color.status_completed;
                break;
            default: // Pending
                bgColor   = R.color.duhis_orange_light;
                textColor = R.color.status_pending;
        }

        h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(h.itemView.getContext(), bgColor)));
        h.tvStatus.setTextColor(ContextCompat.getColor(h.itemView.getContext(), textColor));

        h.card.setOnClickListener(v -> listener.onClick(a));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvType, tvDate, tvStatus;
        VH(View v) {
            super(v);
            card     = v.findViewById(R.id.cardAppointment);
            tvType   = v.findViewById(R.id.tvType);
            tvDate   = v.findViewById(R.id.tvDate);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}