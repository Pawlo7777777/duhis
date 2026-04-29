package com.example.duhis.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.example.duhis.R;
import com.example.duhis.models.Notification;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.UIUtils;

import java.util.HashMap;
import java.util.Map;

public class SendNotificationActivity extends AppCompatActivity {

    private TextInputLayout tilTitle, tilMessage, tilType;
    private TextInputEditText etTitle, etMessage;
    private AutoCompleteTextView actvType;
    private RadioGroup rgAudience;
    private RadioButton rbAllUsers, rbSpecificUser;
    private TextInputLayout tilUserEmail;
    private TextInputEditText etUserEmail;
    private Button btnSend;
    private View progressOverlay;
    private TextView tvPreview;
    private FirebaseHelper fb;

    private static final String[] NOTIFICATION_TYPES = {
            "General Announcement", "Health Alert", "Reminder", "Update", "Emergency"
    };

    private static final Map<String, String> TYPE_MAP = new HashMap<>();
    static {
        TYPE_MAP.put("General Announcement", Notification.TYPE_GENERAL);
        TYPE_MAP.put("Health Alert",         Notification.TYPE_HEALTH_ALERT);
        TYPE_MAP.put("Reminder",             Notification.TYPE_APPOINTMENT);
        TYPE_MAP.put("Update",               Notification.TYPE_GENERAL);
        TYPE_MAP.put("Emergency",            Notification.TYPE_HEALTH_ALERT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);

        fb = FirebaseHelper.getInstance(this); // ← pass context

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Send Notification");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        setupTypeDropdown();
        setupAudienceSelector();
        setupPreview();

        btnSend.setOnClickListener(v -> sendNotification());
    }

    private void initViews() {
        tilTitle       = findViewById(R.id.tilTitle);
        tilMessage     = findViewById(R.id.tilMessage);
        tilType        = findViewById(R.id.tilType);
        etTitle        = findViewById(R.id.etTitle);
        etMessage      = findViewById(R.id.etMessage);
        actvType       = findViewById(R.id.actvType);
        rgAudience     = findViewById(R.id.rgAudience);
        rbAllUsers     = findViewById(R.id.rbAllUsers);
        rbSpecificUser = findViewById(R.id.rbSpecificUser);
        tilUserEmail   = findViewById(R.id.tilUserEmail);
        etUserEmail    = findViewById(R.id.etUserEmail);
        btnSend        = findViewById(R.id.btnSend);
        progressOverlay= findViewById(R.id.progressOverlay);
        tvPreview      = findViewById(R.id.tvPreview);
    }

    private void setupTypeDropdown() {
        actvType.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, NOTIFICATION_TYPES));
        actvType.setOnItemClickListener((parent, view, position, id) -> updatePreview());
    }

    private void setupAudienceSelector() {
        rgAudience.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSpecificUser) {
                tilUserEmail.setVisibility(View.VISIBLE);
            } else {
                tilUserEmail.setVisibility(View.GONE);
                etUserEmail.setText("");
                tilUserEmail.setError(null);
            }
            updatePreview();
        });
    }

    private void setupPreview() {
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updatePreview(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        etTitle.addTextChangedListener(watcher);
        etMessage.addTextChangedListener(watcher);
    }

    private void updatePreview() {
        String title    = etTitle.getText() != null ? etTitle.getText().toString() : "";
        String message  = etMessage.getText() != null ? etMessage.getText().toString() : "";
        String audience = rbSpecificUser.isChecked() ? "Specific User" : "All Users";

        tvPreview.setText("📧 Preview:\n\n" +
                "Title: "    + (title.isEmpty()   ? "[No title]"    : title)   + "\n" +
                "Message: "  + (message.isEmpty() ? "[No message]"  : message) + "\n" +
                "Audience: " + audience + "\n" +
                "Type: "     + (actvType.getText().toString().isEmpty() ? "[Not selected]" : actvType.getText().toString()));
    }

    private void sendNotification() {
        String title       = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String message     = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        String typeDisplay = actvType.getText() != null ? actvType.getText().toString().trim() : "";

        tilTitle.setError(null); tilMessage.setError(null); tilType.setError(null);
        boolean valid = true;

        if (title.isEmpty())       { tilTitle.setError("Title is required");           valid = false; }
        if (message.isEmpty())     { tilMessage.setError("Message is required");       valid = false; }
        if (typeDisplay.isEmpty()) { tilType.setError("Select notification type");     valid = false; }

        if (rbSpecificUser.isChecked()) {
            String userEmail = etUserEmail.getText() != null ? etUserEmail.getText().toString().trim() : "";
            if (userEmail.isEmpty()) { tilUserEmail.setError("Enter user email");      valid = false; }
        }

        if (!valid) return;

        showProgress(true);
        String notificationType = TYPE_MAP.getOrDefault(typeDisplay, Notification.TYPE_GENERAL);

        if (rbAllUsers.isChecked()) {
            sendToAllUsers(title, message, notificationType);
        } else {
            sendToSpecificUser(title, message, notificationType);
        }
    }

    private void sendToAllUsers(String title, String message, String type) {
        fb.users().get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists() || snap.getChildrenCount() == 0) {
                        showProgress(false);
                        UIUtils.showToast(this, "No users found");
                        return;
                    }

                    long totalUsers = snap.getChildrenCount();
                    int[] successCount = {0};

                    for (DataSnapshot doc : snap.getChildren()) {
                        String userId = doc.getKey();

                        Notification notif = new Notification(title, message, type);
                        notif.setTargetUserId(userId);
                        notif.setRead(false);
                        // createdAt set automatically in constructor via System.currentTimeMillis()

                        String key = fb.notifications().push().getKey();
                        fb.notifications().child(key).setValue(notif)
                                .addOnSuccessListener(unused -> {
                                    // Increment unread count
                                    fb.unreadCounts(userId).get().addOnSuccessListener(snap2 -> {
                                        Long current = snap2.getValue(Long.class);
                                        fb.unreadCounts(userId).setValue(current != null ? current + 1 : 1);
                                    });
                                    successCount[0]++;
                                    if (successCount[0] == totalUsers) finishWithSuccess((int) totalUsers);
                                })
                                .addOnFailureListener(e -> {
                                    successCount[0]++;
                                    if (successCount[0] == totalUsers) finishWithSuccess((int) totalUsers);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    UIUtils.showToast(this, "Failed to get users: " + e.getMessage());
                });
    }

    private void sendToSpecificUser(String title, String message, String type) {
        String userEmail = etUserEmail.getText() != null ? etUserEmail.getText().toString().trim() : "";

        // Query users by email field
        fb.users()
                .orderByChild("email")
                .equalTo(userEmail)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists() || snap.getChildrenCount() == 0) {
                        showProgress(false);
                        tilUserEmail.setError("No user found with this email");
                        return;
                    }

                    // Get the first matched user's key
                    DataSnapshot userSnap = snap.getChildren().iterator().next();
                    String userId = userSnap.getKey();

                    Notification notif = new Notification(title, message, type);
                    notif.setTargetUserId(userId);
                    notif.setRead(false);

                    String key = fb.notifications().push().getKey();
                    fb.notifications().child(key).setValue(notif)
                            .addOnSuccessListener(unused -> {
                                fb.unreadCounts(userId).get().addOnSuccessListener(snap2 -> {
                                    Long current = snap2.getValue(Long.class);
                                    fb.unreadCounts(userId).setValue(current != null ? current + 1 : 1);
                                });
                                finishWithSuccess(1);
                            })
                            .addOnFailureListener(e -> {
                                showProgress(false);
                                UIUtils.showToast(this, "Failed to send notification: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    UIUtils.showToast(this, "Failed to find user: " + e.getMessage());
                });
    }

    private void finishWithSuccess(int count) {
        showProgress(false);
        UIUtils.showToast(this, "Notification sent to " + count + " user(s)");
        finish();
    }

    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!show);
    }
}