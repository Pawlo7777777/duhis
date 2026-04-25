package com.example.duhis.models;

import com.google.firebase.Timestamp;

public class Notification {
    public static final String TYPE_GENERAL     = "general";
    public static final String TYPE_APPOINTMENT = "appointment";
    public static final String TYPE_HEALTH_ALERT = "health_alert";
    public static final String TYPE_REMINDER     = "reminder";
    public static final String TYPE_ANNOUNCEMENT = "announcement";

    private String notificationId;
    private String title;
    private String message;
    private String type;
    private String targetUserId;   // null = broadcast to all
    private boolean isRead;
    private String relatedId;      // appointmentId if type == appointment
    private Timestamp createdAt;

    public Notification() {}

    public Notification(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.createdAt = Timestamp.now();
    }

    public String getNotificationId() { return notificationId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public String getTargetUserId() { return targetUserId; }
    public boolean isRead() { return isRead; }
    public String getRelatedId() { return relatedId; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setType(String type) { this.type = type; }
    public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
    public void setRead(boolean read) { isRead = read; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}