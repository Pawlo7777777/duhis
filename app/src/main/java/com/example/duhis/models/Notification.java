package com.example.duhis.models;

public class Notification {
    public static final String TYPE_GENERAL      = "general";
    public static final String TYPE_APPOINTMENT  = "appointment";
    public static final String TYPE_HEALTH_ALERT = "health_alert";
    public static final String TYPE_REMINDER     = "reminder";
    public static final String TYPE_ANNOUNCEMENT = "announcement";

    private String notificationId;
    private String title;
    private String message;
    private String type;
    private String targetUserId;
    private boolean isRead;
    private String relatedId;
    private long createdAt;

    public Notification() {}

    public Notification(String title, String message, String type) {
        this.title     = title;
        this.message   = message;
        this.type      = type;
        this.isRead    = false;
        this.createdAt = System.currentTimeMillis(); // ← long, not Timestamp
    }

    public String getNotificationId()  { return notificationId; }
    public String getTitle()           { return title; }
    public String getMessage()         { return message; }
    public String getType()            { return type; }
    public String getTargetUserId()    { return targetUserId; }
    public boolean isRead()            { return isRead; }
    public String getRelatedId()       { return relatedId; }
    public long getCreatedAt()         { return createdAt; }

    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public void setTitle(String title)                   { this.title = title; }
    public void setMessage(String message)               { this.message = message; }
    public void setType(String type)                     { this.type = type; }
    public void setTargetUserId(String targetUserId)     { this.targetUserId = targetUserId; }
    public void setRead(boolean read)                    { this.isRead = read; }
    public void setRelatedId(String relatedId)           { this.relatedId = relatedId; }
    public void setCreatedAt(long createdAt)             { this.createdAt = createdAt; }
}