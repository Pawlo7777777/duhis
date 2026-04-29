package com.example.duhis.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;

import com.example.duhis.R;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvAdminName, tvTotalUsers, tvPendingAppts, tvTotalAppts, tvTotalHealthInfo;
    private CardView cardManageAppts, cardManageHealth, cardManageUsers, cardSendNotif;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private FirebaseHelper fb;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        fb      = FirebaseHelper.getInstance(this);
        session = new SessionManager(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Admin Dashboard");

        // Drawer setup
        drawerLayout   = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set header name/email
        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.navHeaderName)).setText(session.getName());
        ((TextView) headerView.findViewById(R.id.navHeaderEmail)).setText(session.getEmail());

        // Drawer item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                // already here
            } else if (id == R.id.nav_appointments) {
                startActivity(new Intent(this, ManageAppointmentsActivity.class));
            } else if (id == R.id.nav_health) {
                startActivity(new Intent(this, ManageHealthInfoActivity.class));
            } else if (id == R.id.nav_users) {
                 startActivity(new Intent(this, ManageUsersActivity.class));
            } else if (id == R.id.nav_notifications) {
                 startActivity(new Intent(this, SendNotificationActivity.class));
            } else if (id == R.id.nav_logout) {
                confirmLogout();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Highlight dashboard as selected by default
        navigationView.setCheckedItem(R.id.nav_dashboard);

        initViews();
        loadStats();
        setupCardClicks();
    }

    private void initViews() {
        tvAdminName      = findViewById(R.id.tvAdminName);
        tvTotalUsers     = findViewById(R.id.tvTotalUsers);
        tvPendingAppts   = findViewById(R.id.tvPendingAppts);
        tvTotalAppts     = findViewById(R.id.tvTotalAppts);
        tvTotalHealthInfo= findViewById(R.id.tvTotalHealthInfo);
        cardManageAppts  = findViewById(R.id.cardManageAppts);
        cardManageHealth = findViewById(R.id.cardManageHealth);
        cardManageUsers  = findViewById(R.id.cardManageUsers);
        cardSendNotif    = findViewById(R.id.cardSendNotif);

        tvAdminName.setText(UIUtils.getGreeting() + ", " + session.getName());
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    private void loadStats() {
        // Total users
        fb.users().get().addOnSuccessListener(snap ->
                tvTotalUsers.setText(String.valueOf(snap.getChildrenCount())));

        // Total appointments
        fb.appointments().get().addOnSuccessListener(snap ->
                tvTotalAppts.setText(String.valueOf(snap.getChildrenCount())));

        // Pending appointments
        fb.appointments()
                .orderByChild("status")
                .equalTo("Pending")
                .get()
                .addOnSuccessListener(snap ->
                        tvPendingAppts.setText(String.valueOf(snap.getChildrenCount())));

        // Total health info articles
        fb.healthInfo().get().addOnSuccessListener(snap ->
                tvTotalHealthInfo.setText(String.valueOf(snap.getChildrenCount())));
    }

    private void setupCardClicks() {
        cardManageAppts.setOnClickListener(v ->
                startActivity(new Intent(this, ManageAppointmentsActivity.class)));
        cardManageHealth.setOnClickListener(v ->
                startActivity(new Intent(this, ManageHealthInfoActivity.class)));
        cardManageUsers.setOnClickListener(v ->
                startActivity(new Intent(this, ManageUsersActivity.class)));
        cardSendNotif.setOnClickListener(v ->
                startActivity(new Intent(this, SendNotificationActivity.class)));
    }

    private void confirmLogout() {
        UIUtils.showConfirmDialog(this, "Logout",
                "Are you sure you want to logout?", "Logout", () -> {
                    fb.setUserOffline(session.getUid());
                    FirebaseAuth.getInstance().signOut();
                    session.logout();
                    Intent i = new Intent(this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }
}