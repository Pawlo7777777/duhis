package com.example.duhis.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.example.duhis.R;
import com.example.duhis.activities.AppointmentActivity;
import com.example.duhis.adapters.AppointmentAdapter;
import com.example.duhis.models.Appointment;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppointmentsFragment extends Fragment {

    private RecyclerView rvAppointments;
    private SwipeRefreshLayout swipeRefresh;
    private TabLayout tabLayout;
    private View tvEmpty;
    private FloatingActionButton fabBook;

    private AppointmentAdapter adapter;
    private final List<Appointment> allList  = new ArrayList<>();
    private final List<Appointment> filtered = new ArrayList<>();
    private FirebaseHelper fb;
    private SessionManager session;
    private String currentFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appointments, container, false);
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fb      = FirebaseHelper.getInstance(requireContext()); // ← pass context
        session = new SessionManager(requireContext());

        rvAppointments = view.findViewById(R.id.rvAppointments);
        swipeRefresh   = view.findViewById(R.id.swipeRefresh);
        tabLayout      = view.findViewById(R.id.tabLayout);
        tvEmpty        = view.findViewById(R.id.tvEmpty);
        fabBook        = view.findViewById(R.id.fabBook);

        adapter = new AppointmentAdapter(filtered, appt -> {
            Intent intent = new Intent(getActivity(), AppointmentActivity.class);
            intent.putExtra("appointmentId", appt.getAppointmentId());
            intent.putExtra("viewMode", true);
            startActivity(intent);
        });
        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAppointments.setAdapter(adapter);

        fabBook.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AppointmentActivity.class)));

        swipeRefresh.setColorSchemeResources(R.color.duhis_green);
        swipeRefresh.setOnRefreshListener(this::loadAppointments);

        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.addTab(tabLayout.newTab().setText("Approved"));
        tabLayout.addTab(tabLayout.newTab().setText("Done"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = tab.getText() != null ? tab.getText().toString() : "All";
                applyFilter();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadAppointments();
    }

    private void loadAppointments() {
        String uid = session.getUid();

        fb.appointments()
                .orderByChild("userId")
                .equalTo(uid)
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
                    Collections.reverse(allList); // newest first
                    applyFilter();
                })
                .addOnFailureListener(e -> swipeRefresh.setRefreshing(false));
    }

    private void applyFilter() {
        filtered.clear();
        for (Appointment a : allList) {
            boolean include;
            switch (currentFilter) {
                case "Pending":  include = a.isPending();                        break;
                case "Approved": include = a.isApproved();                       break;
                case "Done":     include = a.isCompleted() || a.isCancelled();   break;
                default:         include = true;                                  break;
            }
            if (include) filtered.add(a);
        }
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAppointments();
    }
}