package com.example.kitahack2025;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.net.Uri;
import android.content.Intent;
import android.provider.MediaStore;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import android.widget.ProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import androidx.core.content.FileProvider;

import java.util.Date;
import java.util.Map;

import android.app.AlertDialog;

public class RequestEssentialFragment extends Fragment {
    protected EditText essentialNameInput, essentialCategoryInput, quantityInput, pickupTimeInput, locationInput;
    protected Spinner urgencyLevelInput;
    protected Button submitButton;
    private ImageView backButton;
    private RequestEssentialRepository requestEssentialRepository;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private Calendar timeCalendar;
    private SimpleDateFormat timeFormatter;
    protected ImageView foodImageView;
    protected Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private FirebaseStorage storage;
    protected StorageReference storageRef;
    protected ProgressBar progressBar;
    private Uri photoUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestEssentialRepository = new RequestEssentialRepository();
        calendar = Calendar.getInstance();
        timeCalendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        timeFormatter = new SimpleDateFormat("hh:mm a", Locale.US); // 12-hour format with AM/PM

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        // Show selected image
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .into(foodImageView);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_essential, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);

        // Set up click listeners
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        submitButton.setOnClickListener(v -> submitRequest());

        // Set up time picker
        pickupTimeInput.setOnClickListener(v -> showTimePicker());
        pickupTimeInput.setFocusable(false);
    }

    private void initializeViews(View view) {
        essentialNameInput = view.findViewById(R.id.essential_name_input);
        essentialCategoryInput = view.findViewById(R.id.essential_category_input);
        urgencyLevelInput = view.findViewById(R.id.essential_urgency_input);
        quantityInput = view.findViewById(R.id.essential_quantity_input);
        pickupTimeInput = view.findViewById(R.id.essential_pickup_input);
        locationInput = view.findViewById(R.id.event_seats_available_input);
        submitButton = view.findViewById(R.id.submit_button);
        backButton = view.findViewById(R.id.back_button);
        foodImageView = view.findViewById(R.id.food_image);
    }

    protected void submitRequest() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        View view = getView();
        if (view == null) {
            Toast.makeText(getContext(), "Error: View not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        if (selectedImageUri != null) {
            // Upload image first
            String imageFileName = "food_images/" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child(imageFileName);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get download URL
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    // Create and save request with image URL
                                    saveRequestWithImage(downloadUri.toString());
                                    progressBar.setVisibility(View.GONE);
                                    submitButton.setEnabled(true);
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    submitButton.setEnabled(true);
                                    Toast.makeText(getContext(),
                                            "Failed to get image URL: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(getContext(),
                                "Failed to upload image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Save request without image
            saveRequestWithImage(null);
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
        }
    }

    private void saveRequestWithImage(String imageUrl) {
        urgencyLevelInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUrgency = parent.getItemAtPosition(position).toString();
                Log.d("SpinnerSelection", "Selected urgency level: " + selectedUrgency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected if needed
            }
        });

        String name = essentialNameInput.getText().toString();
        String foodCategory = essentialCategoryInput.getText().toString();
        String urgencyLevel = urgencyLevelInput.getSelectedItem().toString();
        String quantity = quantityInput.getText().toString();
        String pickupTime = pickupTimeInput.getText().toString();
        String location = locationInput.getText().toString();
        String requestType = "Food";

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to request", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = currentUser.getEmail();
        String ownerProfileImageUrl = currentUser.getPhotoUrl() != null ?
                currentUser.getPhotoUrl().toString() : "";

        // Create new request with all fields including profile image URL
        RequestEssential newRequest = new RequestEssential(
                name,
                foodCategory,
                urgencyLevel,
                quantity,
                pickupTime,
                location,
                R.drawable.img_placeholder,
                imageUrl,
                requestType,
                ownerProfileImageUrl,
                currentUserEmail
        );

        // Save to repository
        RequestEssentialRepository repository = new RequestEssentialRepository();
        repository.addRequestEssential(newRequest, new RequestEssentialRepository.OnRequestCompleteListener() {
            @Override
            public void onRequestSuccess() {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Request added successfully", Toast.LENGTH_SHORT).show();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    // Fetch the username from the users collection
                    db.collection("users")
                            .whereEqualTo("email", currentUserEmail)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                String ownersUsername = "Unknown User";
                                if (!querySnapshot.isEmpty()) {
                                    ownersUsername = querySnapshot.getDocuments().get(0).getString("username");
                                }

                                Map<String, Object> notificationData = new HashMap<>();
                                notificationData.put("ownerEmail", currentUserEmail);
                                notificationData.put("itemName", name);
                                notificationData.put("location", location);
                                notificationData.put("imageUrl", imageUrl);
                                notificationData.put("expiredDate", pickupTime);
                                notificationData.put("status", "unread");
                                notificationData.put("message", ownersUsername + " has a new request!");
                                notificationData.put("activityType", "request");
                                notificationData.put("notiType", "all");

                                db.collection("notifications")
                                        .add(notificationData)
                                        .addOnSuccessListener(documentReference -> {
                                            Log.d("Notification", "Notification stored successfully");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("NotificationError", "Failed to store notification: " + e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("UserQueryError", "Failed to fetch requester username: " + e.getMessage());

                                // Fallback: Store notification with requester email if username fetch fails
                                Map<String, Object> notificationData = new HashMap<>();
                                notificationData.put("ownerEmail", currentUserEmail);
                                notificationData.put("itemName", name);
                                notificationData.put("location", location);
                                notificationData.put("imageUrl", imageUrl);
                                notificationData.put("expiredDate", pickupTime);
                                notificationData.put("status", "unread");
                                notificationData.put("message", currentUserEmail + " has a new request!");
                                notificationData.put("activityType", "request");
                                notificationData.put("notiType", "all");

                                db.collection("notifications")
                                        .add(notificationData)
                                        .addOnSuccessListener(documentReference -> {
                                            Log.d("Notification", "Notification stored successfully (fallback to email)");
                                        })
                                        .addOnFailureListener(err -> {
                                            Log.e("NotificationError", "Failed to store notification (fallback): " + err.getMessage());
                                        });
                            });

                    // Clear form or navigate back
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onRequestFailure(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Failed to add request: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected boolean validateInputs() {
        if (essentialNameInput.getText().toString().trim().isEmpty()) {
            essentialNameInput.setError("Name is required");
            return false;
        }
        if (essentialCategoryInput.getText().toString().trim().isEmpty()) {
            essentialCategoryInput.setError("Food category is required");
            return false;
        }
        if (quantityInput.getText().toString().trim().isEmpty()) {
            quantityInput.setError("Quantity is required");
            return false;
        }
        if (pickupTimeInput.getText().toString().trim().isEmpty()) {
            pickupTimeInput.setError("Pickup time is required");
            return false;
        }
        if (locationInput.getText().toString().trim().isEmpty()) {
            locationInput.setError("Location is required");
            return false;
        }
        return true;
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                R.style.CustomPickerTheme,
                (view, hourOfDay, minute) -> {
                    timeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    timeCalendar.set(Calendar.MINUTE, minute);
                    updateTimeLabel();
                },
                timeCalendar.get(Calendar.HOUR_OF_DAY),
                timeCalendar.get(Calendar.MINUTE),
                false
        );

        timePickerDialog.show();
    }

    private void updateTimeLabel() {
        pickupTimeInput.setText(timeFormatter.format(timeCalendar.getTime()));
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",        /* suffix */
                storageDir     /* directory */
        );
    }
}