package com.example.duhis.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.example.duhis.R;
import com.example.duhis.adapters.NotificationAdapter;
import com.example.duhis.models.Notification;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private SwipeRefreshLayout swipeRefresh;
    private View tvEmpty, tvMarkAll;
    private NotificationAdapter adapter;
    private final List<Notification> notifList = new ArrayList<>();
    private FirebaseHelper fb;
    private SessionManager session;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        fb      = FirebaseHelper.getInstance(this);
        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Notifications");
        toolbar.setNavigationOnClickListener(v -> finish());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvNotifications = findViewById(R.id.rvNotifications);
        swipeRefresh    = findViewById(R.id.swipeRefresh);
        tvEmpty         = findViewById(R.id.tvEmpty);
        tvMarkAll       = findViewById(R.id.tvMarkAll);

        adapter = new NotificationAdapter(notifList, this::onNotificationClicked);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.duhis_green);
        swipeRefresh.setOnRefreshListener(this::loadNotifications);

        tvMarkAll.setOnClickListener(v -> markAllRead());

        loadNotifications();
    }

    private void loadNotifications() {
        String uid = session.getUid();
        if (uid == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        fb.notifications()
                .orderByChild("targetUserId")
                .equalTo(uid)
                .get()
                .addOnSuccessListener(snap -> {
                    swipeRefresh.setRefreshing(false);
                    notifList.clear();

                    for (DataSnapshot doc : snap.getChildren()) {
                        Notification n = doc.getValue(Notification.class);
                        if (n != null) {
                            n.setNotificationId(doc.getKey());
                            notifList.add(n);
                        }
                    }

                    // Newest first
                    Collections.sort(notifList, (a, b) ->
                            Long.compare(b.getCreatedAt(), a.getCreatedAt()));

                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(notifList.isEmpty() ? View.VISIBLE : View.GONE);

                    // Reset unread badge in RTDB
                    fb.unreadCounts(uid).setValue(0);
                })
                .addOnFailureListener(e -> swipeRefresh.setRefreshing(false));
    }

    /**
     * Called when the user taps a notification item.
     * Marks it as read in DB and updates the UI.
     */
    private void onNotificationClicked(Notification notif) {
        markAsRead(notif);
        // You can add navigation here later, e.g. open appointment detail
    }

    /**
     * Marks a single notification as read both in Firebase and locally.
     * Writes both "isRead" and "read" to fix the inconsistent fields in DB.
     */
    private void markAsRead(Notification notif) {
        if (notif.isRead()) return; // already read, skip

        Map<String, Object> update = new HashMap<>();
        update.put("isRead", true);
        update.put("read",   true); // fix the duplicate field in DB

        fb.notifications()
                .child(notif.getNotificationId())
                .updateChildren(update)
                .addOnSuccessListener(unused -> {
                    notif.setRead(true);
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Marks ALL unread notifications as read in a single batch update.
     */
    private void markAllRead() {
        String uid = session.getUid();
        if (uid == null) return;

        // Build a batch update map: { "notifId/isRead": true, "notifId/read": true, ... }
        Map<String, Object> batchUpdate = new HashMap<>();
        for (Notification n : notifList) {
            if (!n.isRead()) {
                batchUpdate.put(n.getNotificationId() + "/isRead", true);
                batchUpdate.put(n.getNotificationId() + "/read",   true);
            }
        }

        if (batchUpdate.isEmpty()) return; // nothing to update

        fb.notifications()
                .updateChildren(batchUpdate)
                .addOnSuccessListener(unused -> {
                    // Update all local objects and refresh UI in one pass
                    for (Notification n : notifList) {
                        n.setRead(true);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}