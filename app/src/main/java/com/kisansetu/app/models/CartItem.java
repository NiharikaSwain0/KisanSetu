package com.kisansetu.app.models;

public class CartItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private String farmerId;

    public CartItem() {
        // Required for Firestore
    }

    public CartItem(String productId, String productName, double price, int quantity, String farmerId) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.farmerId = farmerId;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }
}
