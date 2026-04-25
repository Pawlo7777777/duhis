package com.example.duhis.models;

import com.google.firebase.Timestamp;

public class HealthInfo {
    private String infoId;
    private String title;
    private String summary;
    private String content;
    private String category;    // e.g., "Prevention", "Nutrition", "Mental Health"
    private String imageUrl;
    private boolean isFeatured;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public HealthInfo() {}

    public HealthInfo(String title, String summary, String content, String category) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.category = category;
        this.isFeatured = false;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public String getInfoId() { return infoId; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public boolean isFeatured() { return isFeatured; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    public void setInfoId(String infoId) { this.infoId = infoId; }
    public void setTitle(String title) { this.title = title; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setContent(String content) { this.content = content; }
    public void setCategory(String category) { this.category = category; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setFeatured(boolean featured) { isFeatured = featured; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}