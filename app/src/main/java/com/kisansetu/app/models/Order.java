package com.kisansetu.app.models;

import java.util.List;

public class Order {
    private String orderId;
    private String customerId;
    private List<CartItem> items;
    private double totalAmount;
    private String status; // Pending, Accepted, Rejected, Out for Delivery, Delivered
    private String farmerId;
    private String riderId;
    private long timestamp;
    private String deliveryAddress;
    private String paymentMode; // COD, Online

    public Order() {
        // Required for Firestore
    }

    public Order(String orderId, String customerId, List<CartItem> items, double totalAmount, String status, String farmerId, String deliveryAddress, String paymentMode) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.farmerId = farmerId;
        this.deliveryAddress = deliveryAddress;
        this.paymentMode = paymentMode;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}
