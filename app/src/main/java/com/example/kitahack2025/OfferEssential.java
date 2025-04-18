package com.example.kitahack2025;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OfferEssential implements Serializable {
    private String name,essentialCategory, description, category, expiryDate, quantity, pickupTime, location, imageUrl, documentId, status, ownerProfileImageUrl, offerType, email;
    private int imageResourceId;
    private long createdAt;
    private String feedback;
    private String receiverEmail;
    private String locationArea; // e.g., "Bukit Jalil"
    private String locationState; // e.g., "Kuala Lumpur"

    // Constructor
    public OfferEssential() {
        // Required empty constructor for Firestore
    }

    public OfferEssential(String name, String essentialCategory, String description, String category, String expiredDate, String quantity, String pickupTime, String location, int imageResourceId, String imageUrl, String offerType, String ownerProfileImageUrl, String email) {
        this.name = name;
        this.essentialCategory = essentialCategory;
        this.description = description;
        this.category = category;
        this.expiryDate = expiredDate;
        this.quantity = quantity;
        this.pickupTime = pickupTime;
        this.location = location;
        this.imageResourceId = imageResourceId;
        this.imageUrl = imageUrl;
        this.ownerProfileImageUrl = ownerProfileImageUrl;
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
        this.offerType = offerType;
        this.email = email;
        // Parse location into area and state
        String[] locationParts = location.split(",");
        this.locationArea = locationParts[0].trim();
        this.locationState = locationParts.length > 1 ? locationParts[1].trim() : "";
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public String getEssentialCategory() {
        return essentialCategory;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getExpiredDate() {
        return expiryDate;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public String getLocation() {
        return location;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedCreationDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);
        return sdf.format(new Date(createdAt));
    }

    public String getOwnerProfileImageUrl() {
        return ownerProfileImageUrl;
    }

    public void setOwnerProfileImageUrl(String ownerProfileImageUrl) {
        this.ownerProfileImageUrl = ownerProfileImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOfferType(){
        return offerType;
    }

    public void setOfferType(String offerType){
        this.offerType = offerType;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getLocationArea() {
        return locationArea;
    }

    public String getLocationState() {
        return locationState;
    }
}