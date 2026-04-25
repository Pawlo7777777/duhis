package com.example.duhis.models;

import com.google.firebase.Timestamp;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String bloodType;
    private String allergies;
    private String profileImageUrl;
    private String role; // "user" or "admin"
    private String fcmToken;
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {} // Required by Firestore

    public User(String uid, String fullName, String email, String phoneNumber) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = "user";
        this.isActive = true;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters
    public String getUid() { return uid; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public String getBloodType() { return bloodType; }
    public String getAllergies() { return allergies; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getRole() { return role; }
    public String getFcmToken() { return fcmToken; }
    public boolean isActive() { return isActive; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // Setters
    public void setUid(String uid) { this.uid = uid; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setGender(String gender) { this.gender = gender; }
    public void setAddress(String address) { this.address = address; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setRole(String role) { this.role = role; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public void setActive(boolean active) { isActive = active; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isAdmin() { return "admin".equals(role); }
}