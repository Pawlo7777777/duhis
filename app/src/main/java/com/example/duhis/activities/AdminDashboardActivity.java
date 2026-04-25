package com.example.duhis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;
import com.example.duhis.R;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvAdminName, tvTotalUsers, tvPendingAppts, tvTotalAppts, tvTotalHealthInfo;
    private CardView cardManageAppts, cardManageHealth, cardManageUsers,
            cardSendNotif, cardEmergency;
    private FirebaseHelper fb;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        fb      = FirebaseHelper.getInstance();
        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Admin Dashboard");
        toolbar.inflateMenu(R.menu.menu_admin);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) { confirmLogout(); return true; }
            return false;
        });

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

    private void loadStats() {
        // Total users
        fb.users().get().addOnSuccessListener(snap ->
                tvTotalUsers.setText(String.valueOf(snap.size())));

        // Total appointments
        fb.appointments().get().addOnSuccessListener(snap ->
                tvTotalAppts.setText(String.valueOf(snap.size())));

        // Pending appointments
        fb.appointments()
                .whereEqualTo("status", "Pending")
                .get()
                .addOnSuccessListener(snap ->
                        tvPendingAppts.setText(String.valueOf(snap.size())));

        // Total health info articles
        fb.healthInfo().get().addOnSuccessListener(snap ->
                tvTotalHealthInfo.setText(String.valueOf(snap.size())));
    }

    private void setupCardClicks() {
        cardManageAppts.setOnClickListener(v ->
                startActivity(new Intent(this, ManageAppointmentsActivity.class)));
        cardManageHealth.setOnClickListener(v ->
                startActivity(new Intent(this, ManageHealthInfoActivity.class)));
//        cardManageUsers.setOnClickListener(v ->
//                startActivity(new Intent(this, ManageUsersActivity.class)));
//        cardSendNotif.setOnClickListener(v ->
//                startActivity(new Intent(this, SendNotificationActivity.class)));
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