package com.example.duhis.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.duhis.R;
import com.example.duhis.models.Appointment;
import com.example.duhis.models.Notification;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AppointmentActivity extends AppCompatActivity {

    private TextInputLayout tilDate, tilTime, tilType, tilNotes;
    private TextInputEditText etDate, etTime, etNotes;
    private AutoCompleteTextView actvType;
    private Button btnSubmit;
    private View progressOverlay;

    private FirebaseHelper fb;
    private SessionManager session;

    private static final String[] CONSULTATION_TYPES = {
            "General Check-up", "Prenatal Care", "Immunization / Vaccination",
            "Family Planning Consultation", "Child Health Monitoring",
            "TB / DOTS Consultation", "Dental Health", "Mental Health Support",
            "Nutrition Counseling", "Senior Citizen Health Check"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        fb      = FirebaseHelper.getInstance();
        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Book Appointment");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        setupTypeDropdown();
        setupDateTimePickers();

        btnSubmit.setOnClickListener(v -> submitAppointment());
    }

    private void initViews() {
        tilDate         = findViewById(R.id.tilDate);
        tilTime         = findViewById(R.id.tilTime);
        tilType         = findViewById(R.id.tilType);
        tilNotes        = findViewById(R.id.tilNotes);
        etDate          = findViewById(R.id.etDate);
        etTime          = findViewById(R.id.etTime);
        etNotes         = findViewById(R.id.etNotes);
        actvType        = findViewById(R.id.actvType);
        btnSubmit       = findViewById(R.id.btnSubmit);
        progressOverlay = findViewById(R.id.progressOverlay);
    }

    private void setupTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, CONSULTATION_TYPES);
        actvType.setAdapter(adapter);
    }

    private void setupDateTimePickers() {
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> showDatePicker());
        tilDate.setEndIconOnClickListener(v -> showDatePicker());

        etTime.setFocusable(false);
        etTime.setClickable(true);
        etTime.setOnClickListener(v -> showTimePicker());
        tilTime.setEndIconOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1); // minimum: tomorrow
        new DatePickerDialog(this, (dp, y, m, d) -> {
            String date = String.format("%04d-%02d-%02d", y, m + 1, d);
            String display = String.format("%s %02d, %04d",
                    new java.text.DateFormatSymbols().getMonths()[m], d, y);
            etDate.setText(display);
            etDate.setTag(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (tp, h, min) -> {
            String amPm = h >= 12 ? "PM" : "AM";
            int hour12  = h == 0 ? 12 : (h > 12 ? h - 12 : h);
            etTime.setText(String.format("%02d:%02d %s", hour12, min, amPm));
        }, 8, 0, false).show();
    }

    private void submitAppointment() {
        String dateDisplay = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String dateValue   = etDate.getTag() != null ? etDate.getTag().toString() : "";
        String time        = etTime.getText() != null ? etTime.getText().toString().trim() : "";
        String type        = actvType.getText() != null ? actvType.getText().toString().trim() : "";
        String notes       = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        tilDate.setError(null); tilTime.setError(null); tilType.setError(null);
        boolean valid = true;

        if (dateDisplay.isEmpty()) { tilDate.setError("Select a date"); valid = false; }
        if (time.isEmpty())        { tilTime.setError("Select a time"); valid = false; }
        if (type.isEmpty())        { tilType.setError("Select consultation type"); valid = false; }
        if (!valid) return;

        showProgress(true);

        Appointment appt = new Appointment(
                session.getUid(), session.getName(), session.getPhone(),
                type, dateValue, time, notes);

        fb.appointments().add(appt)
                .addOnSuccessListener(ref -> {
                    saveNotification(ref.getId(), type, dateDisplay, time);
                    showProgress(false);
                    UIUtils.showToast(this, "Appointment booked successfully!");
                    finish();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    UIUtils.showSnackbarError(btnSubmit, "Failed: " + e.getMessage());
                });
    }

    private void saveNotification(String apptId, String type, String date, String time) {
        Notification notif = new Notification(
                "Appointment Booked",
                "Your " + type + " appointment on " + date + " at " + time + " is pending approval.",
                Notification.TYPE_APPOINTMENT);
        notif.setTargetUserId(session.getUid());
        notif.setRelatedId(apptId);

        fb.notifications().add(notif).addOnSuccessListener(ref -> {
            // Increment RTDB unread count
            fb.unreadCounts(session.getUid()).get().addOnSuccessListener(snap -> {
                Long current = snap.getValue(Long.class);
                fb.unreadCounts(session.getUid()).setValue(current != null ? current + 1 : 1);
            });
        });
    }

    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
    }
}