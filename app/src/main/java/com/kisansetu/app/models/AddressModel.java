package com.kisansetu.app.models;

public class AddressModel {
    private String id;
    private String label; // Home, Work, Other
    private String fullAddress;
    private String houseNo;
    private String landmark;
    private String receiverName;
    private String receiverPhone;
    private double latitude;
    private double longitude;

    public AddressModel() {}

    public AddressModel(String id, String label, String fullAddress, String houseNo, String landmark, String receiverName, String receiverPhone, double latitude, double longitude) {
        this.id = id;
        this.label = label;
        this.fullAddress = fullAddress;
        this.houseNo = houseNo;
        this.landmark = landmark;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
    public String getHouseNo() { return houseNo; }
    public void setHouseNo(String houseNo) { this.houseNo = houseNo; }
    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}