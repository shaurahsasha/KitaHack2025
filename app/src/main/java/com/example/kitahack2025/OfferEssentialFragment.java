package com.example.kitahack2025;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class OfferEssentialFragment extends Fragment {
    protected EditText essentialNameInput, essentialCategoryInput, expiryDateInput, quantityInput, pickupTimeInput, locationInput;
    protected Button submitButton;
    private ImageView backButton;
    private EssentialItemRepository essentialItemRepository;
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
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Uri photoUri;
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        essentialItemRepository = new EssentialItemRepository();
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

        // Initialize camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (photoUri != null) {
                            selectedImageUri = photoUri;
                            // Show selected image
                            Glide.with(this)
                                    .load(photoUri)
                                    .centerCrop()
                                    .into(foodImageView);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offer_essential, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);

        // Set up click listeners
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        submitButton.setOnClickListener(v -> submitOffer());

        // Set up date picker
        expiryDateInput.setOnClickListener(v -> showDatePicker());
        expiryDateInput.setFocusable(false); // Prevent keyboard from showing up

        // Set up time picker
        pickupTimeInput.setOnClickListener(v -> showTimePicker());
        pickupTimeInput.setFocusable(false);
    }

    private void initializeViews(View view) {
        essentialNameInput = view.findViewById(R.id.essential_name_input);
        essentialCategoryInput = view.findViewById(R.id.essential_category_input);
        expiryDateInput = view.findViewById(R.id.essential_expiry_input);
        quantityInput = view.findViewById(R.id.essential_category_input);
        pickupTimeInput = view.findViewById(R.id.essential_pickup_input);
        locationInput = view.findViewById(R.id.event_seats_available_input);
        submitButton = view.findViewById(R.id.submit_button);
        backButton = view.findViewById(R.id.back_button);
        foodImageView = view.findViewById(R.id.food_image);
        Button uploadImageButton = view.findViewById(R.id.upload_image_button);

        uploadImageButton.setOnClickListener(v -> showImageSourceDialog());
    }

    protected void submitOffer() {
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
                                    // Create and save offer with image URL
                                    saveOfferWithImage(downloadUri.toString());
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
            // Save offer without image
            saveOfferWithImage(null);
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
        }
    }

    private void saveOfferWithImage(String imageUrl) {
        String name = essentialNameInput.getText().toString();
        String essentialCategory = essentialCategoryInput.getText().toString();
        String description = "";
        String category = "";
        String expiredDate = expiryDateInput.getText().toString();
        String quantity = quantityInput.getText().toString();
        String pickupTime = pickupTimeInput.getText().toString();
        String location = locationInput.getText().toString();
        String offerType = "Food";

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String ownerEmail = currentUser.getEmail();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to offer", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerUsername = currentUser.getDisplayName();
        String ownerProfileImageUrl = currentUser.getPhotoUrl() != null ?
                currentUser.getPhotoUrl().toString() : "";

        // Create new offer with all fields including profile image URL
        EssentialItem newOffer = new EssentialItem(
                name,
                essentialCategory,
                description,
                category,
                expiredDate,
                quantity,
                pickupTime,
                location,
                R.drawable.img_placeholder,
                imageUrl,
                ownerUsername,
                offerType,
                ownerProfileImageUrl
        );

        // Save to repository
        EssentialItemRepository repository = new EssentialItemRepository();
        repository.addOfferItem(newOffer, new EssentialItemRepository.OnOfferCompleteListener() {
            @Override
            public void onOfferSuccess() {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Offer added successfully", Toast.LENGTH_SHORT).show();
                    // Clear form or navigate back
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onOfferFailure(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Failed to add offer: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the username from the users collection
        db.collection("users")
                .whereEqualTo("email", ownerEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    String ownersUsername = "Unknown User";
                    if (!querySnapshot.isEmpty()) {
                        ownersUsername = querySnapshot.getDocuments().get(0).getString("username");
                    }

                    Map<String, Object> notificationData = new HashMap<>();
                    notificationData.put("ownerEmail", ownerEmail);
                    notificationData.put("essentialName", name);
                    notificationData.put("location", location);
                    notificationData.put("imageUrl", imageUrl);
                    notificationData.put("expiredDate", expiredDate);
                    notificationData.put("status", "unread");
                    notificationData.put("message", ownersUsername + " has a new offer!");
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
                    notificationData.put("ownerEmail", ownerEmail);
                    notificationData.put("essentialName", name);
                    notificationData.put("location", location);
                    notificationData.put("imageUrl", imageUrl);
                    notificationData.put("expiredDate", expiredDate);
                    notificationData.put("status", "unread");
                    notificationData.put("message", ownerEmail + " has a new offer!");
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
    }

    protected boolean validateInputs() {
        if (essentialNameInput.getText().toString().trim().isEmpty()) {
            essentialNameInput.setError("Essential name is required");
            return false;
        }
        if (essentialCategoryInput.getText().toString().trim().isEmpty()) {
            essentialCategoryInput.setError("Essential category is required");
            return false;
        }
        if (expiryDateInput.getText().toString().trim().isEmpty()) {
            expiryDateInput.setError("Expiry date is required");
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

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                R.style.CustomPickerTheme,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateExpiryDateLabel();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date as today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void updateExpiryDateLabel() {
        expiryDateInput.setText(dateFormatter.format(calendar.getTime()));
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

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Take photo with camera
                        if (checkCameraPermission()) {
                            openCamera();
                        } else {
                            requestCameraPermission();
                        }
                    } else {
                        // Choose from gallery
                        openImagePicker();
                    }
                })
                .show();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(),
                        "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(requireContext(),
                        "com.shareplateapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(),
                        "Camera permission is required to take photos",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}