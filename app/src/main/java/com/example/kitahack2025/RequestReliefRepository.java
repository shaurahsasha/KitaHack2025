package com.example.kitahack2025;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import android.util.Log;

public class RequestReliefRepository {
    private static final String TAG = "RequestReliefRepository";
    private static final String COLLECTION_NAME = "allRequestItems";
    private final FirebaseFirestore db;

    // Define the interface for callbacks
    public interface OnRequestCompleteListener {
        void onRequestSuccess();
        void onRequestFailure(Exception e);
    }

    public RequestReliefRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface OnRequestItemsLoadedListener {
        void onRequestReliefLoaded(List<RequestRelief> items);
        void onError(Exception e);
    }

    public void addRequestRelief(RequestRelief item, OnRequestCompleteListener listener) {
        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "No authenticated user found");
            listener.onRequestFailure(new Exception("User not authenticated"));
            return;
        }

        String userEmail = currentUser.getEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            System.err.println("User email is null or empty. Cannot fetch username.");
            return;
        }

        String ownerProfileImageUrl = currentUser.getPhotoUrl() != null ?
                currentUser.getPhotoUrl().toString() : "";

        // Create donation data map
        Map<String, Object> donationData = new HashMap<>();
        donationData.put("name", item.getName());
        donationData.put("foodCategory", item.getCategory());
        donationData.put("urgencyLevel", item.getUrgencylevel());
        donationData.put("quantity", item.getQuantity());
        donationData.put("pickupTime", item.getPickupTime());
        donationData.put("location", item.getLocation());
        donationData.put("imageResourceId", item.getImageResourceId());
        donationData.put("imageUrl", item.getImageUrl());
        donationData.put("email", userEmail);
        donationData.put("ownerProfileImageUrl", ownerProfileImageUrl);
        donationData.put("status", "active");
        donationData.put("createdAt", System.currentTimeMillis());
        donationData.put("donateType", "Relief");

        // Add to Firestore
        db.collection(COLLECTION_NAME)
                .add(donationData)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    item.setDocumentId(docId);
                    System.out.println("Document added with ID: " + docId);
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    listener.onRequestSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    listener.onRequestFailure(e);
                });
    }


    public void deleteRequestItem(String documentId, OnDeleteCompleteListener listener) {
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

    public void updateRequestStatus(String documentId, String status, OnStatusUpdateListener listener) {
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
}