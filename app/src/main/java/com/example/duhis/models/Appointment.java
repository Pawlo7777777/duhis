package com.example.duhis.models;

import static android.icu.util.UniversalTimeScale.toLong;

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
    private String date;
    private String time;
    private String notes;
    private String status;
    private String adminRemarks;
    private Object createdAt;
    private Object updatedAt;

    // Required by Realtime Database deserializer
    public Appointment() {}

    public Appointment(String userId, String userName, String userPhone,
                       String consultationType, String date, String time, String notes) {
        this.userId            = userId;
        this.userName          = userName;
        this.userPhone         = userPhone;
        this.consultationType  = consultationType;
        this.date              = date;
        this.time              = time;
        this.notes             = notes;
        this.status            = STATUS_PENDING;
        this.createdAt         = System.currentTimeMillis();
        this.updatedAt         = System.currentTimeMillis();
    }

    // Getters
    public String getAppointmentId()     { return appointmentId; }
    public String getUserId()            { return userId; }
    public String getUserName()          { return userName; }
    public String getUserPhone()         { return userPhone; }
    public String getConsultationType()  { return consultationType; }
    public String getDate()              { return date; }
    public String getTime()              { return time; }
    public String getNotes()             { return notes; }
    public String getStatus()            { return status; }
    public String getAdminRemarks()      { return adminRemarks; }
    public long getCreatedAt() {
        return toLong(createdAt);
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long)    return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Double)  return ((Double) value).longValue();
        if (value instanceof java.util.Map) {
            Object seconds = ((java.util.Map<?, ?>) value).get("seconds");
            if (seconds instanceof Long)    return (Long) seconds * 1000L;
            if (seconds instanceof Integer) return ((Integer) seconds).longValue() * 1000L;
            if (seconds instanceof Double)  return ((Double) seconds).longValue() * 1000L;
        }
        return 0L;
    }

    public long getUpdatedAt() {
        return toLong(updatedAt);
    }

    // Setters
    public void setAppointmentId(String appointmentId)       { this.appointmentId = appointmentId; }
    public void setUserId(String userId)                     { this.userId = userId; }
    public void setUserName(String userName)                 { this.userName = userName; }
    public void setUserPhone(String userPhone)               { this.userPhone = userPhone; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }
    public void setDate(String date)                         { this.date = date; }
    public void setTime(String time)                         { this.time = time; }
    public void setNotes(String notes)                       { this.notes = notes; }
    public void setAdminRemarks(String adminRemarks)         { this.adminRemarks = adminRemarks; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Object updatedAt) { this.updatedAt = updatedAt; }
    public void setStatus(String status) {
        this.status    = status;
        this.updatedAt = System.currentTimeMillis();
    }

    // Status helpers — derived from `status` string, not stored as separate booleans
    public boolean isPending()   { return STATUS_PENDING.equals(status); }
    public boolean isApproved()  { return STATUS_APPROVED.equals(status); }
    public boolean isCancelled() { return STATUS_CANCELLED.equals(status); }
    public boolean isCompleted() { return STATUS_COMPLETED.equals(status); }
}