package com.example.duhis.models;

import com.google.firebase.Timestamp;

public class Appointment {
    public static final String STATUS_PENDING   = "Pending";
    public static final String STATUS_APPROVED  = "Approved";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_COMPLETED = "Completed";

    private String appointmentId;
    private String userId;
    private String userName;
    private String userPhone;
    private String consultationType;
    private String date;          // "2024-12-25"
    private String time;          // "09:00 AM"
    private String notes;
    private String status;
    private String adminRemarks;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Appointment() {} // Firestore

    public Appointment(String userId, String userName, String userPhone,
                       String consultationType, String date, String time, String notes) {
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.consultationType = consultationType;
        this.date = date;
        this.time = time;
        this.notes = notes;
        this.status = STATUS_PENDING;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters
    public String getAppointmentId() { return appointmentId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserPhone() { return userPhone; }
    public String getConsultationType() { return consultationType; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public String getAdminRemarks() { return adminRemarks; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // Setters
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(String status) { this.status = status; this.updatedAt = Timestamp.now(); }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isPending()   { return STATUS_PENDING.equals(status); }
    public boolean isApproved()  { return STATUS_APPROVED.equals(status); }
    public boolean isCancelled() { return STATUS_CANCELLED.equals(status); }
    public boolean isCompleted() { return STATUS_COMPLETED.equals(status); }
}