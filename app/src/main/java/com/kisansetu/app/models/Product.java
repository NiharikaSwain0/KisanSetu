package com.kisansetu.app.models;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String quantity;
    private String imageUrl;
    private String farmerId;
    private String category;
    private String harvestTime;
    private boolean isClosed; // if all sold

    public Product() {
        // Required for Firestore
    }

    public Product(String id, String name, String description, double price, String quantity, String imageUrl, String farmerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.farmerId = farmerId;
        this.isClosed = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getHarvestTime() { return harvestTime; }
    public void setHarvestTime(String harvestTime) { this.harvestTime = harvestTime; }

    public boolean isClosed() { return isClosed; }
    public void setClosed(boolean closed) { isClosed = closed; }
}
