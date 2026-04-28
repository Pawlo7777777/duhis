package com.example.duhis.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.example.duhis.R;
import com.example.duhis.adapters.AdminAppointmentAdapter;
import com.example.duhis.models.Appointment;
import com.example.duhis.models.Notification;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageAppointmentsActivity extends AppCompatActivity {

    private RecyclerView rvAppointments;
    private SwipeRefreshLayout swipeRefresh;
    private TabLayout tabLayout;
    private View tvEmpty;

    private AdminAppointmentAdapter adapter;
    private final List<Appointment> allList  = new ArrayList<>();
    private final List<Appointment> filtered = new ArrayList<>();
    private FirebaseHelper fb;
    private String currentFilter = "Pending";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_appointments);

        fb = FirebaseHelper.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Manage Appointments");
        toolbar.setNavigationOnClickListener(v -> finish());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvAppointments = findViewById(R.id.rvAppointments);
        swipeRefresh   = findViewById(R.id.swipeRefresh);
        tabLayout      = findViewById(R.id.tabLayout);
        tvEmpty        = findViewById(R.id.tvEmpty);

        adapter = new AdminAppointmentAdapter(filtered, this::handleAction);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAppointments.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.duhis_green);
        swipeRefresh.setOnRefreshListener(this::loadAll);

        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.addTab(tabLayout.newTab().setText("Approved"));
        tabLayout.addTab(tabLayout.newTab().setText("All"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = tab.getText() != null ? tab.getText().toString() : "Pending";
                applyFilter();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadAll();
    }

    private void loadAll() {
        fb.appointments()
                .orderByChild("createdAt")
                .get()
                .addOnSuccessListener(snap -> {
                    swipeRefresh.setRefreshing(false);
                    allList.clear();
                    for (DataSnapshot doc : snap.getChildren()) {
                        Appointment a = doc.getValue(Appointment.class);
                        if (a != null) {
                            a.setAppointmentId(doc.getKey());
                            allList.add(a);
                        }
                    }
                    Collections.reverse(allList);
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    UIUtils.showToast(this, "Failed to load: " + e.getMessage());
                    Log.d("ERRROOR", e.getMessage());
                });
    }

    private void applyFilter() {
        filtered.clear();
        for (Appointment a : allList) {
            boolean include;
            switch (currentFilter) {
                case "Pending":  include = a.isPending();  break;
                case "Approved": include = a.isApproved(); break;
                default:         include = true;           break;
            }
            if (include) filtered.add(a);
        }
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void handleAction(Appointment appt, String action) {
        String newStatus;
        switch (action) {
            case "approve":  newStatus = Appointment.STATUS_APPROVED;  break;
            case "cancel":   newStatus = Appointment.STATUS_CANCELLED; break;
            case "complete": newStatus = Appointment.STATUS_COMPLETED; break;
            default:         newStatus = appt.getStatus();             break;
        }

        UIUtils.showConfirmDialog(this, "Update Status",
                "Mark this appointment as " + newStatus + "?", "Confirm", () -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("status",    newStatus);
                    update.put("updatedAt", System.currentTimeMillis()); // ← long, not Timestamp

                    fb.appointments().child(appt.getAppointmentId()).updateChildren(update)
                            .addOnSuccessListener(v -> {
                                appt.setStatus(newStatus);
                                applyFilter();
                                notifyUser(appt, newStatus);
                                UIUtils.showToast(this, "Status updated to " + newStatus);
                            })
                            .addOnFailureListener(e ->
                                    UIUtils.showToast(this, "Update failed: " + e.getMessage()));
                });
    }

    private void notifyUser(Appointment appt, String newStatus) {
        String msg = "Your appointment on " + appt.getDate() + " at " + appt.getTime()
                + " has been " + newStatus.toLowerCase() + ".";

        Notification notif = new Notification(
                "Appointment " + newStatus,
                msg,
                Notification.TYPE_APPOINTMENT
        );
        notif.setTargetUserId(appt.getUserId());
        notif.setRelatedId(appt.getAppointmentId());

        String newKey = fb.notifications().push().getKey(); // ← push() not add()
        fb.notifications().child(newKey).setValue(notif)
                .addOnSuccessListener(unused -> {
                    fb.unreadCounts(appt.getUserId()).get()
                            .addOnSuccessListener(snap -> {
                                Long count = snap.getValue(Long.class);
                                fb.unreadCounts(appt.getUserId())
                                        .setValue(count != null ? count + 1 : 1);
                            });
                })
                .addOnFailureListener(e ->
                        UIUtils.showToast(this, "Failed to send notification: " + e.getMessage()));
    }
}