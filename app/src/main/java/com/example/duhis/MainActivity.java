package com.example.duhis;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.duhis.activities.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.duhis.R;
import com.example.duhis.fragments.HomeFragment;
import com.example.duhis.fragments.AppointmentsFragment;
import com.example.duhis.fragments.HealthInfoFragment;
import com.example.duhis.fragments.EmergencyFragment;
import com.example.duhis.fragments.ProfileFragment;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView tvNotifBadge;
    private SessionManager session;
    private FirebaseHelper fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ensure Firebase is initialized
        FirebaseApp.initializeApp(this);

        // Initialize session and FirebaseHelper with context
        session = new SessionManager( this);
        fb = FirebaseHelper.getInstance(this);  // Pass context here

        bottomNav = findViewById(R.id.bottomNav);
        tvNotifBadge = findViewById(R.id.tvNotifBadge);

        // Check if user is logged in
        if (!session.isLoggedIn() || fb.getAuth().getCurrentUser() == null) {
            // User is not logged in, redirect to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Set user online status
        String uid = session.getUid();
        if (uid != null) {
            fb.setUserOnline(uid);
        }

        setupBottomNav();
        listenUnreadCount();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if      (id == R.id.nav_home)        fragment = new HomeFragment();
            else if (id == R.id.nav_appointments) fragment = new AppointmentsFragment();
            else if (id == R.id.nav_health_info)  fragment = new HealthInfoFragment();
            else if (id == R.id.nav_emergency)    fragment = new EmergencyFragment();
            else if (id == R.id.nav_profile)      fragment = new ProfileFragment();

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /** Listen to RTDB unread count and show badge */
    private void listenUnreadCount() {
        String uid = session.getUid();
        if (uid == null) return;

        fb.unreadCounts(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long count = snapshot.getValue(Long.class);
                if (count != null && count > 0) {
                    tvNotifBadge.setVisibility(View.VISIBLE);
                    tvNotifBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                } else {
                    tvNotifBadge.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error if needed
                error.toException().printStackTrace();
            }
        });
    }

    public void navigateTo(int navItemId) {
        bottomNav.setSelectedItemId(navItemId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String uid = session.getUid();
        if (uid != null && fb != null) {
            fb.setUserOffline(uid);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update online status when returning to app
        String uid = session.getUid();
        if (uid != null && fb != null && session.isLoggedIn()) {
            fb.setUserOnline(uid);
        }
    }
}