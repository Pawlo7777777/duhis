package com.example.duhis.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.Query;
import com.example.duhis.R;
import com.example.duhis.adapters.NotificationAdapter;
import com.example.duhis.models.Notification;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty, tvMarkAll;
    private NotificationAdapter adapter;
    private final List<Notification> notifList = new ArrayList<>();
    private FirebaseHelper fb;
    private SessionManager session;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        fb      = FirebaseHelper.getInstance();
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

        adapter = new NotificationAdapter(notifList, this::markAsRead);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.duhis_green);
        swipeRefresh.setOnRefreshListener(this::loadNotifications);

        tvMarkAll.setOnClickListener(v -> markAllRead());

        loadNotifications();
    }

    private void loadNotifications() {
        String uid = session.getUid();
        fb.notifications()
                .whereEqualTo("targetUserId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snap -> {
                    swipeRefresh.setRefreshing(false);
                    notifList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) { n.setNotificationId(doc.getId()); notifList.add(n); }
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(notifList.isEmpty() ? View.VISIBLE : View.GONE);
                    // Reset RTDB badge
                    fb.unreadCounts(uid).setValue(0);
                })
                .addOnFailureListener(e -> swipeRefresh.setRefreshing(false));
    }

    private void markAsRead(Notification notif) {
        if (notif.isRead()) return;
        Map<String, Object> update = new HashMap<>();
        update.put("isRead", true);
        fb.notifications().document(notif.getNotificationId()).update(update);
        notif.setRead(true);
        adapter.notifyDataSetChanged();
    }

    private void markAllRead() {
        for (Notification n : notifList) {
            if (!n.isRead()) markAsRead(n);
        }
    }
}