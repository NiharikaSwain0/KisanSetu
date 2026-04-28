package com.kisansetu.app.models;

public class Delivery {
    private String deliveryId;
    private String orderId;
    private String riderId;
    private String status; // Assigned, Picked Up, On the Way, Delivered
    private String farmerAddress;
    private String customerAddress;
    private long timestamp;

    public Delivery() {
        // Required for Firestore
    }

    public Delivery(String deliveryId, String orderId, String riderId, String status, String farmerAddress, String customerAddress) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.riderId = riderId;
        this.status = status;
        this.farmerAddress = farmerAddress;
        this.customerAddress = customerAddress;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getDeliveryId() { return deliveryId; }
    public void setDeliveryId(String deliveryId) { this.deliveryId = deliveryId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFarmerAddress() { return farmerAddress; }
    public void setFarmerAddress(String farmerAddress) { this.farmerAddress = farmerAddress; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
