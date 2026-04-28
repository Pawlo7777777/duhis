package com.example.duhis.models;

public class HealthInfo {
    private String infoId;
    private String title;
    private String summary;
    private String content;
    private String category;
    private String imageUrl;
    private boolean isFeatured;
    private long createdAt;
    private long updatedAt;

    public HealthInfo() {} // required for Realtime Database

    public HealthInfo(String title, String summary, String content, String category) {
        this.title     = title;
        this.summary   = summary;
        this.content   = content;
        this.category  = category;
        this.isFeatured = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getInfoId()    { return infoId; }
    public String getTitle()     { return title; }
    public String getSummary()   { return summary; }
    public String getContent()   { return content; }
    public String getCategory()  { return category; }
    public String getImageUrl()  { return imageUrl; }
    public boolean isFeatured()  { return isFeatured; }
    public long getCreatedAt()   { return createdAt; }
    public long getUpdatedAt()   { return updatedAt; }

    public void setInfoId(String infoId)     { this.infoId = infoId; }
    public void setTitle(String title)       { this.title = title; }
    public void setSummary(String summary)   { this.summary = summary; }
    public void setContent(String content)   { this.content = content; }
    public void setCategory(String category) { this.category = category; }
    public void setFeatured(boolean featured){ this.isFeatured = featured; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}