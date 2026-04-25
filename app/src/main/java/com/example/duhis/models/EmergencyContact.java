package com.example.duhis.models;

public class EmergencyContact {
    private String contactId;
    private String name;
    private String description;
    private String phoneNumber;
    private String category;   // hospital, fire, police, ambulance, health_center
    private String iconRes;    // drawable resource name
    private int sortOrder;

    public EmergencyContact() {}

    public EmergencyContact(String name, String description, String phoneNumber,
                            String category, int sortOrder) {
        this.name = name;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.category = category;
        this.sortOrder = sortOrder;
    }

    public String getContactId() { return contactId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getCategory() { return category; }
    public String getIconRes() { return iconRes; }
    public int getSortOrder() { return sortOrder; }

    public void setContactId(String contactId) { this.contactId = contactId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setCategory(String category) { this.category = category; }
    public void setIconRes(String iconRes) { this.iconRes = iconRes; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}