package com.example.kitahack2025;

import static java.security.AccessController.getContext;

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
import android.widget.Toast;

public class RequestEssentialRepository {
    private static final String TAG = "RequestEssentialRepository";
    private static final String COLLECTION_NAME = "essentialRequest";
    private final FirebaseFirestore db;

    // Define the interface for callbacks
    public interface OnRequestCompleteListener {
        void onRequestSuccess();
        void onRequestFailure(Exception e);
    }

    public RequestEssentialRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface OnRequestItemsLoadedListener {
        void onRequestEssentialsLoaded(List<RequestEssential> items);
        void onError(Exception e);
    }

    public void getAllRequestItems(OnRequestItemsLoadedListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String email = currentUser.getEmail();

        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RequestEssential> items = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String name = document.getString("name");
                            String essentialCategory = document.getString("essentialCategory");
                            String urgencyLevel = document.getString("urgencyLevel");
                            String quantity = document.getString("quantity");
                            String pickupTime = document.getString("pickupTime");
                            String location = document.getString("location");
                            String status = document.getString("status");
                            String ownerProfileImageUrl = document.getString("ownerProfileImageUrl");
                            String requestType = document.getString("requestType");
                            String ownerEmail = document.getString("email");

                            int imageResourceId = R.drawable.img_placeholder;
                            Long resourceIdLong = document.getLong("imageResourceID");
                            if (resourceIdLong != null) {
                                imageResourceId = resourceIdLong.intValue();
                            }

                            String imageUrl = document.getString("imageUrl");

                            Long createdAt = document.getLong("createdAt");

                            if (name != null && !name.isEmpty()) {
                                RequestEssential item = new RequestEssential(
                                        name,
                                        essentialCategory != null ? essentialCategory : "",
                                        urgencyLevel != null ? urgencyLevel : "",
                                        quantity != null ? quantity : "",
                                        pickupTime != null ? pickupTime : "",
                                        location != null ? location : "",
                                        imageResourceId,
                                        imageUrl,
                                        requestType,
                                        ownerProfileImageUrl != null ? ownerProfileImageUrl : "",
                                        ownerEmail
                                );
                                // Set the document ID and status
                                item.setDocumentId(document.getId());
                                item.setStatus(status != null ? status : "active");
                                if (createdAt != null) {
                                    item.setCreatedAt(createdAt);
                                }
                                items.add(item);
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing document: " + e.getMessage());
                        }
                    }
                    listener.onRequestEssentialsLoaded(items);
                })
                .addOnFailureListener(listener::onError);
    }

    public void addRequestEssential(RequestEssential item, OnRequestCompleteListener listener) {
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

        // Create request data map
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("name", item.getName());
        requestData.put("essentialCategory", item.getEssentialCategory());
        requestData.put("urgencyLevel", item.getUrgencyLevel());
        requestData.put("quantity", item.getQuantity());
        requestData.put("pickupTime", item.getPickupTime());
        requestData.put("location", item.getLocation());
        requestData.put("imageResourceId", item.getImageResourceId());
        requestData.put("imageUrl", item.getImageUrl());
        requestData.put("email", userEmail);
        requestData.put("ownerProfileImageUrl", ownerProfileImageUrl);
        requestData.put("status", "active");
        requestData.put("createdAt", System.currentTimeMillis());
        requestData.put("requestType", "Food");

        // Add to Firestore
        db.collection(COLLECTION_NAME)
                .add(requestData)
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