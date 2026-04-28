package com.kisansetu.app.models;

import com.google.firebase.firestore.PropertyName;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String role; // Farmer, Customer, Rider, Admin
    private String address;
    private String farmName;
    private boolean isOnline; // For Riders
    private String vehicleType; // For Riders
    private String vehicleNumber; // For Riders
    private String profileImage;

    public User() {
        // Required for Firestore
    }

    public User(String uid, String fullName, String email, String phone, String role, String address, String farmName) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.address = address;
        this.farmName = farmName;
        this.isOnline = false;
        this.vehicleType = "";
        this.vehicleNumber = "";
        this.profileImage = "";
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    @PropertyName("isOnline")
    public boolean isOnline() { return isOnline; }
    
    @PropertyName("isOnline")
    public void setOnline(boolean online) { isOnline = online; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}
