package com.fuxdevs.dropndispense.user.models;

import java.util.Date;

public class Parcel {
    private String id;
    private String userId;
    private String productName;
    private double price;
    private String trackingNumber;
    private String deliveryDate; // Changed to String format MM/DD/YYYY
    private String platform;
    private String status;
    private Date createdAt;

    // Empty constructor for Firebase
    public Parcel() {}

    public Parcel(String userId, String productName, double price, String trackingNumber, 
                 String deliveryDate, String platform, String status) {
        this.userId = userId;
        this.productName = productName;
        this.price = price;
        this.trackingNumber = trackingNumber;
        this.deliveryDate = deliveryDate;
        this.platform = platform;
        this.status = status;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    
    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }
    
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}