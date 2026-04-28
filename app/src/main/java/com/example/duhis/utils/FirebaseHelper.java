package com.example.duhis.utils;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Centralized Firebase references.
 * All data now uses Realtime Database.
 */
public class FirebaseHelper {

    // ── Realtime DB paths ────────────────────────────────────────────────────
    public static final String RTDB_USERS              = "users";
    public static final String RTDB_APPOINTMENTS       = "appointments";
    public static final String RTDB_HEALTH_INFO        = "healthInfo";
    public static final String RTDB_NOTIFICATIONS      = "notifications";
    public static final String RTDB_EMERGENCY_CONTACTS = "emergencyContacts";
    public static final String RTDB_ONLINE_USERS       = "onlineUsers";
    public static final String RTDB_APPOINTMENT_STATUS = "appointmentStatus";
    public static final String RTDB_UNREAD_COUNTS      = "unreadCounts";

    private static FirebaseHelper instance;
    private final FirebaseAuth auth;
    private final FirebaseDatabase realtimeDb;
    private final FirebaseStorage storage;

    private FirebaseHelper(Context context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }
        auth       = FirebaseAuth.getInstance();
        realtimeDb = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseHelper(context);
        }
        return instance;
    }

    @Deprecated
    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "FirebaseHelper not initialized. Call getInstance(Context) first.");
        }
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }

    public String currentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    // ── Realtime DB helpers ──────────────────────────────────────────────────
    public DatabaseReference users()             { return realtimeDb.getReference(RTDB_USERS); }
    public DatabaseReference appointments()      { return realtimeDb.getReference(RTDB_APPOINTMENTS); }
    public DatabaseReference healthInfo()        { return realtimeDb.getReference(RTDB_HEALTH_INFO); }
    public DatabaseReference notifications()     { return realtimeDb.getReference(RTDB_NOTIFICATIONS); }
    public DatabaseReference emergencyContacts() { return realtimeDb.getReference(RTDB_EMERGENCY_CONTACTS); }
    public DatabaseReference onlineUsers()       { return realtimeDb.getReference(RTDB_ONLINE_USERS); }
    public DatabaseReference appointmentStatus() { return realtimeDb.getReference(RTDB_APPOINTMENT_STATUS); }
    public DatabaseReference unreadCounts(String uid) {
        return realtimeDb.getReference(RTDB_UNREAD_COUNTS).child(uid);
    }

    // ── Presence helpers ─────────────────────────────────────────────────────
    public void setUserOnline(String uid) {
        if (uid == null || uid.isEmpty()) return;
        DatabaseReference ref = onlineUsers().child(uid);
        ref.setValue(true);
        ref.onDisconnect().removeValue();
    }

    public void setUserOffline(String uid) {
        if (uid == null || uid.isEmpty()) return;
        onlineUsers().child(uid).removeValue();
    }

    public static boolean isInitialized() {
        return instance != null;
    }
    public StorageReference healthInfoImages() {
        return storage.getReference("healthInfoImages");
    }
}