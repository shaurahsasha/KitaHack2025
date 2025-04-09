package com.example.kitahack2025;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class OfferEssentialRepository {
    private static final String TAG = "OfferEssentialRepository";
    private static final String COLLECTION_NAME = "allOfferItems";
    private final FirebaseFirestore db;

    // Define the interface for callbacks
    public interface OnOfferCompleteListener {
        void onOfferSuccess();
        void onOfferFailure(Exception e);
    }

    public OfferEssentialRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface OnOfferEssentialsLoadedListener {
        void onOfferEssentialsLoaded(List<OfferEssential> items);
        void onError(Exception e);
    }

    public void getAllOfferItems(OnOfferEssentialsLoadedListener listener) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OfferEssential> items = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String name = document.getString("name");
                            String essentialCategory = document.getString("essentialCategory");
                            String description = document.getString("description");
                            String category = document.getString("category");
                            String expiryDate = document.getString("expiredDate");
                            String quantity = document.getString("quantity");
                            String pickupTime = document.getString("pickupTime");
                            String location = document.getString("location");
                            String ownerEmail = document.getString("email");
                            String status = document.getString("status");
                            String ownerProfileImageUrl = document.getString("ownerProfileImageUrl");
                            String offerType = document.getString("offerType");
                            String feedback = document.getString("feedback");
                            String receiverEmail = document.getString("receiverEmail");

                            int imageResourceId = R.drawable.img_placeholder;
                            Long resourceIdLong = document.getLong("imageResourceID");
                            if (resourceIdLong != null) {
                                imageResourceId = resourceIdLong.intValue();
                            }

                            String imageUrl = document.getString("imageUrl");

                            Long createdAt = document.getLong("createdAt");

                            if (name != null && !name.isEmpty()) {
                                OfferEssential item = new OfferEssential(
                                        name,
                                        essentialCategory != null ? essentialCategory : "",
                                        description != null ? description : "",
                                        category != null ? category : "",
                                        expiryDate != null ? expiryDate : "",
                                        quantity != null ? quantity : "",
                                        pickupTime != null ? pickupTime : "",
                                        location != null ? location : "",
                                        imageResourceId,
                                        imageUrl,
                                        offerType,
                                        ownerProfileImageUrl != null ? ownerProfileImageUrl : "",
                                        ownerEmail
                                );
                                // Set the document ID and status
                                item.setDocumentId(document.getId());
                                item.setStatus(status != null ? status : "active");
                                item.setFeedback(feedback);
                                item.setReceiverEmail(receiverEmail);
                                if (createdAt != null) {
                                    item.setCreatedAt(createdAt);
                                }
                                items.add(item);
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing document: " + e.getMessage());
                        }
                    }
                    listener.onOfferEssentialsLoaded(items);
                })
                .addOnFailureListener(listener::onError);
    }

    public void addOfferItem(OfferEssential item, OnOfferCompleteListener listener) {
        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "No authenticated user found");
            listener.onOfferFailure(new Exception("User not authenticated"));
            return;
        }

        String userEmail = currentUser.getEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            System.err.println("User email is null or empty. Cannot fetch username.");
            return;
        }

        String ownerProfileImageUrl = currentUser.getPhotoUrl() != null ?
                currentUser.getPhotoUrl().toString() : "";

        // Create offer data map
        Map<String, Object> offerData = new HashMap<>();
        offerData.put("name", item.getName());
        offerData.put("essentialCategory", item.getEssentialCategory());
        offerData.put("expiredDate", item.getExpiredDate());
        offerData.put("quantity", item.getQuantity());
        offerData.put("pickupTime", item.getPickupTime());
        offerData.put("location", item.getLocation());
        offerData.put("imageResourceID", item.getImageResourceId());
        offerData.put("imageUrl", item.getImageUrl());
        offerData.put("email", userEmail);
        offerData.put("ownerProfileImageUrl", ownerProfileImageUrl);
        offerData.put("status", "active");
        offerData.put("createdAt", System.currentTimeMillis());
        offerData.put("offerType", "Essential");

        // Add to Firestore
        db.collection(COLLECTION_NAME)
                .add(offerData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    listener.onOfferSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    listener.onOfferFailure(e);
                });
    }


    public void deleteOfferItem(String documentId, OnDeleteCompleteListener listener) {
        if (documentId == null) {
            System.err.println("Cannot delete item: document ID is null");
            if (listener != null) {
                listener.onDeleteFailure(new Exception("Document ID is null"));
            }
            return;
        }

        System.out.println("Attempting to delete document with ID: " + documentId);

        db.collection(COLLECTION_NAME)
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Successfully deleted document: " + documentId);
                    if (listener != null) {
                        listener.onDeleteSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    System.err.println("Failed to delete document: " + documentId + ", error: " + e.getMessage());
                    if (listener != null) {
                        listener.onDeleteFailure(e);
                    }
                });
    }

    public interface OnDeleteCompleteListener {
        void onDeleteSuccess();
        void onDeleteFailure(Exception e);
    }

    public void updateOfferStatus(String documentId, String status, OnStatusUpdateListener listener) {
        if (documentId == null) {
            if (listener != null) {
                listener.onUpdateFailure(new Exception("Document ID is null"));
            }
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(documentId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onUpdateSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onUpdateFailure(e);
                    }
                });
    }

    public interface OnStatusUpdateListener {
        void onUpdateSuccess();
        void onUpdateFailure(Exception e);
    }

    public void updateOfferWithFields(String documentId, Map<String, Object> updates,
                                         OnStatusUpdateListener listener) {
        db.collection(COLLECTION_NAME)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    listener.onUpdateSuccess();
                })
                .addOnFailureListener(listener::onUpdateFailure);
    }
}