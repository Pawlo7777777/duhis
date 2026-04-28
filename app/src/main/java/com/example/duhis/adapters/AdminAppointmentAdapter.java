package com.example.duhis.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duhis.R;
import com.example.duhis.models.Appointment;

import java.util.List;

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.VH> {

    public interface OnAction { void onAction(Appointment appt, String action); }

    private final List<Appointment> list;
    private final OnAction listener;

    public AdminAppointmentAdapter(List<Appointment> list, OnAction listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_appointment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Appointment a = list.get(pos);
        h.tvName.setText(a.getUserName());
        h.tvPhone.setText(a.getUserPhone());
        h.tvType.setText(a.getConsultationType());
        h.tvDate.setText(a.getDate());   // ← was combined into tvDateTime
        h.tvTime.setText(a.getTime());   // ← now its own field
        h.tvStatus.setText(a.getStatus());

        if (a.getNotes() != null && !a.getNotes().isEmpty()) {
            h.tvNotes.setVisibility(View.VISIBLE);
            h.tvNotes.setText("Notes: " + a.getNotes());
        } else {
            h.tvNotes.setVisibility(View.GONE);
        }

        // Status chip color
        int bgColor, textColor;
        switch (a.getStatus()) {
            case Appointment.STATUS_APPROVED:
                bgColor = R.color.duhis_green_light; textColor = R.color.status_approved; break;
            case Appointment.STATUS_CANCELLED:
                bgColor = R.color.duhis_red_light;   textColor = R.color.status_cancelled; break;
            case Appointment.STATUS_COMPLETED:
                bgColor = R.color.duhis_blue_light;  textColor = R.color.status_completed; break;
            default:
                bgColor = R.color.duhis_orange_light; textColor = R.color.status_pending;
        }
        h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(h.itemView.getContext(), bgColor)));
        h.tvStatus.setTextColor(ContextCompat.getColor(h.itemView.getContext(), textColor));

        // Action buttons visibility
        if (a.isPending()) {
            h.btnApprove.setVisibility(View.VISIBLE);
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnComplete.setVisibility(View.GONE);
        } else if (a.isApproved()) {
            h.btnApprove.setVisibility(View.GONE);
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnComplete.setVisibility(View.VISIBLE);
        } else {
            h.btnApprove.setVisibility(View.GONE);
            h.btnCancel.setVisibility(View.GONE);
            h.btnComplete.setVisibility(View.GONE);
        }

        h.btnApprove.setOnClickListener(v  -> listener.onAction(a, "approve"));
        h.btnCancel.setOnClickListener(v   -> listener.onAction(a, "cancel"));
        h.btnComplete.setOnClickListener(v -> listener.onAction(a, "complete"));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvType, tvDate, tvTime, tvStatus, tvNotes;
        Button btnApprove, btnCancel, btnComplete;

        VH(View v) {
            super(v);
            tvName      = v.findViewById(R.id.tvPatientName);      // ← was tvName
            tvPhone     = v.findViewById(R.id.tvPatientPhone);     // ← was tvPhone
            tvType      = v.findViewById(R.id.tvConsultationType); // ← was tvType
            tvDate      = v.findViewById(R.id.tvDate);
            tvTime      = v.findViewById(R.id.tvTime);             // ← now separate
            tvStatus    = v.findViewById(R.id.tvStatus);
            tvNotes     = v.findViewById(R.id.tvNotes);
            btnApprove  = v.findViewById(R.id.btnApprove);
            btnCancel   = v.findViewById(R.id.btnCancel);
            btnComplete = v.findViewById(R.id.btnComplete);
        }
    }
}