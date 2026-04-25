package com.example.duhis.utils;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Centralized Firebase references.
 *
 * Firestore  → structured data: users, appointments, healthInfo, notifications, emergencyContacts
 * Realtime DB → live presence / typing / real-time appointment status stream
 */
public class FirebaseHelper {

    // ── Firestore collections ────────────────────────────────────────────────
    public static final String COL_USERS               = "users";
    public static final String COL_APPOINTMENTS        = "appointments";
    public static final String COL_HEALTH_INFO         = "healthInfo";
    public static final String COL_NOTIFICATIONS       = "notifications";
    public static final String COL_EMERGENCY_CONTACTS  = "emergencyContacts";

    // ── Realtime DB paths ────────────────────────────────────────────────────
    public static final String RTDB_ONLINE_USERS          = "onlineUsers";
    public static final String RTDB_APPOINTMENT_STATUS    = "appointmentStatus";
    public static final String RTDB_UNREAD_COUNTS         = "unreadCounts";

    private static FirebaseHelper instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseDatabase realtimeDb;

    // Private constructor with Context parameter for initialization
    private FirebaseHelper(Context context) {
        // CRITICAL: Ensure Firebase is initialized before using any Firebase services
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance();
    }

    // Updated getInstance method to accept Context
    public static synchronized FirebaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseHelper(context);
        }
        return instance;
    }

    // Keep this for backward compatibility but it will cause issues if called
    // without prior Firebase initialization. Better to remove or update all calls.
    @Deprecated
    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "FirebaseHelper not initialized with Context. " +
                            "Please call getInstance(Context) instead."
            );
        }
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }

    // ── Firestore helpers ────────────────────────────────────────────────────
    public CollectionReference users()              { return firestore.collection(COL_USERS); }
    public CollectionReference appointments()       { return firestore.collection(COL_APPOINTMENTS); }
    public CollectionReference healthInfo()         { return firestore.collection(COL_HEALTH_INFO); }
    public CollectionReference notifications()      { return firestore.collection(COL_NOTIFICATIONS); }
    public CollectionReference emergencyContacts()  { return firestore.collection(COL_EMERGENCY_CONTACTS); }

    public String currentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    // ── Realtime DB helpers ──────────────────────────────────────────────────
    public DatabaseReference onlineUsers()          { return realtimeDb.getReference(RTDB_ONLINE_USERS); }
    public DatabaseReference appointmentStatus()    { return realtimeDb.getReference(RTDB_APPOINTMENT_STATUS); }
    public DatabaseReference unreadCounts(String uid) {
        return realtimeDb.getReference(RTDB_UNREAD_COUNTS).child(uid);
    }

    /** Mark current user as online; call onDisconnect to auto-clean. */
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

    // Helper method to check if Firebase is initialized
    public static boolean isInitialized() {
        return instance != null;
    }
}