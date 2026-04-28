package com.example.duhis.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.example.duhis.R;
import com.example.duhis.activities.AppointmentActivity;
import com.example.duhis.activities.HealthInfoActivity;
import com.example.duhis.activities.NotificationsActivity;
import com.example.duhis.adapters.HealthInfoAdapter;
import com.example.duhis.models.HealthInfo;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    // Views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnDrawerToggle;
    private ImageButton btnNotifications;
    private TextView tvGreeting, tvUserName, tvSeeAll;
    private RecyclerView rvFeatured;
    private SwipeRefreshLayout swipeRefresh;
    private View cardAppointment, cardHealthInfo, cardEmergency, cardNotifications;

    // Data
    private HealthInfoAdapter adapter;
    private final List<HealthInfo> featuredList = new ArrayList<>();
    private FirebaseHelper fb;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fb = FirebaseHelper.getInstance(requireContext());
        session = new SessionManager(requireContext());

        bindViews(view);
        setupGreeting();
        setupRecyclerView();
        setupDrawer();
        setupCardClicks();
        setupSeeAll();
        loadFeaturedHealth();

        swipeRefresh.setColorSchemeResources(R.color.duhis_green);
        swipeRefresh.setOnRefreshListener(this::loadFeaturedHealth);
    }

    // ─────────────────────────────────────────
    //  View Binding
    // ─────────────────────────────────────────

    private void bindViews(View view) {
        drawerLayout      = view.findViewById(R.id.drawerLayout);
        navigationView    = view.findViewById(R.id.navigationView);
        btnDrawerToggle   = view.findViewById(R.id.btnDrawerToggle);
        btnNotifications  = view.findViewById(R.id.btnNotifications);
        tvGreeting        = view.findViewById(R.id.tvGreeting);
        tvUserName        = view.findViewById(R.id.tvUserName);
        tvSeeAll          = view.findViewById(R.id.tvSeeAll);
        rvFeatured        = view.findViewById(R.id.rvFeatured);
        swipeRefresh      = view.findViewById(R.id.swipeRefresh);
        cardAppointment   = view.findViewById(R.id.cardAppointment);
        cardHealthInfo    = view.findViewById(R.id.cardHealthInfo);
        cardEmergency     = view.findViewById(R.id.cardEmergency);
        cardNotifications = view.findViewById(R.id.cardNotifications);
    }

    // ─────────────────────────────────────────
    //  Greeting
    // ─────────────────────────────────────────

    private void setupGreeting() {
        tvGreeting.setText(UIUtils.getGreeting() + ",");
        tvUserName.setText(session.getName() != null ? session.getName() : "");
    }

    // ─────────────────────────────────────────
    //  RecyclerView
    // ─────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new HealthInfoAdapter(featuredList, item -> {
            Intent intent = new Intent(getActivity(), HealthInfoActivity.class);
            intent.putExtra("infoId", item.getInfoId());
            startActivity(intent);
        });
        rvFeatured.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeatured.setAdapter(adapter);
    }

    // ─────────────────────────────────────────
    //  Drawer Setup
    // ─────────────────────────────────────────

    private void setupDrawer() {
        // Toggle button opens/closes the drawer
        btnDrawerToggle.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });

        // Notification bell — goes to NotificationsActivity
        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), NotificationsActivity.class)));

        // Handle drawer menu item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(navigationView);
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Already on home — do nothing
                return true;
            } else if (id == R.id.nav_appointment) {
                startActivity(new Intent(getActivity(), AppointmentActivity.class));
                return true;
            } else if (id == R.id.nav_health_info) {
                startActivity(new Intent(getActivity(), HealthInfoActivity.class));
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(getActivity(), NotificationsActivity.class));
                return true;
            } else if (id == R.id.nav_emergency) {
                navigateToEmergency();
                return true;
            }
            return false;
        });

        // Update the drawer header with user info
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView navUserName = headerView.findViewById(R.id.navUserName);
            TextView navUserEmail = headerView.findViewById(R.id.navUserEmail);
            if (navUserName != null && session.getName() != null) {
                navUserName.setText(session.getName());
            }
            if (navUserEmail != null && session.getEmail() != null) {
                navUserEmail.setText(session.getEmail());
            }
        }
    }

    // ─────────────────────────────────────────
    //  Card Clicks
    // ─────────────────────────────────────────

    private void setupCardClicks() {
        cardAppointment.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AppointmentActivity.class)));

        cardHealthInfo.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), HealthInfoActivity.class)));

        cardEmergency.setOnClickListener(v -> navigateToEmergency());

        cardNotifications.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), NotificationsActivity.class)));
    }

    private void setupSeeAll() {
        tvSeeAll.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), HealthInfoActivity.class)));
    }

    private void navigateToEmergency() {
        if (getActivity() instanceof com.example.duhis.MainActivity) {
            ((com.example.duhis.MainActivity) getActivity())
                    .navigateTo(R.id.nav_emergency);
        }
    }

    // ─────────────────────────────────────────
    //  Back-press: close drawer first
    // ─────────────────────────────────────────

    /**
     * Call this from the host Activity's onBackPressed().
     * Returns true if the drawer was open and has been closed (consume the event).
     */
    public boolean onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
            return true;
        }
        return false;
    }

    // ─────────────────────────────────────────
    //  Firebase Load
    // ─────────────────────────────────────────

    private void loadFeaturedHealth() {
        if (fb.getAuth().getCurrentUser() == null) {
            UIUtils.showToast(requireContext(), "Not signed in");
            swipeRefresh.setRefreshing(false);
            return;
        }

        fb.healthInfo()
                .orderByChild("isFeatured")
                .equalTo(true)
                .limitToLast(5)
                .get()
                .addOnSuccessListener(snapshot -> {
                    swipeRefresh.setRefreshing(false);
                    featuredList.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot doc : snapshot.getChildren()) {
                            HealthInfo info = doc.getValue(HealthInfo.class);
                            if (info != null) {
                                info.setInfoId(doc.getKey());
                                featuredList.add(info);
                            }
                        }
                        Collections.reverse(featuredList); // newest first
                    } else {
                        UIUtils.showToast(requireContext(),
                                "No featured health information available");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    UIUtils.showToast(requireContext(),
                            "Failed to load health info: " + e.getMessage());
                    Log.d("EERRRRRROORRR", e.getMessage());
                });
    }
}