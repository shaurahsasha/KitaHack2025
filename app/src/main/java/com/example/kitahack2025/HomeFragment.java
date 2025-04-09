package com.example.kitahack2025;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.text.SimpleDateFormat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.button.MaterialButton;
import android.content.res.ColorStateList;
import android.graphics.Color;

public class HomeFragment extends Fragment {
    private Toolbar toolbar;
    private ImageView searchIcon, menuIcon, backArrow, sortIcon;
    private GridLayout donationGrid;
    private EditText searchEditText;
    private LinearLayout searchLayout;
    private View normalToolbarContent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OfferEssentialRepository donationItemRepo;
    private RequestEssentialRepository requestItemRepo;
    private List<Object> allItems = new ArrayList<>();
    private SortOption currentSortOption = SortOption.DEFAULT;
    private SortDirection currentSortDirection = SortDirection.ASCENDING;
    private View cachedSearchLayout;
    private View cachedNormalLayout;
    private boolean isSearchMode = false;
    private static final int SEARCH_DELAY = 100; // ms
    private Runnable searchRunnable;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private InputMethodManager imm;
    private View rootView;
    private MaterialButton allFilterButton, activeFilterButton, completedFilterButton;
    private String currentFilter = "all"; // Default filter
    private Map<String, List<Object>> filteredListsCache = new HashMap<>();
    private List<Object> currentDisplayedItems = new ArrayList<>();
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        checkUserExistence();
        // Initialize views and repository
        initializeViews(rootView);
        donationItemRepo = new OfferEssentialRepository();
        requestItemRepo = new RequestEssentialRepository();

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadItems);

        // Load donation items from Firestore
        loadItems();

        // Set up search functionality
        searchIcon.setOnClickListener(v -> {
            if (isSearchMode) return;
            isSearchMode = true;

            // Hardware acceleration for animations
            searchLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            // Update visibilities without triggering layout
            searchLayout.setVisibility(View.VISIBLE);
            normalToolbarContent.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);

            // Focus and keyboard handling
            searchEditText.requestFocus();
            if (imm != null) {
                imm.showSoftInput(searchEditText, 0);
            }

            // Reset layer type after transition
            searchLayout.post(() -> searchLayout.setLayerType(View.LAYER_TYPE_NONE, null));
        });

        backArrow.setOnClickListener(v -> closeSearch());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterItems(s.toString());
            }
        });

        Button sortButton = rootView.findViewById(R.id.sortButton);
        sortButton.setOnClickListener(v -> showSortOptions());

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        // Pre-initialize IMM at fragment creation
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void initializeViews(View view) {
        // Initialize all views first
        toolbar = view.findViewById(R.id.toolbar);
        searchIcon = view.findViewById(R.id.search_icon);
//        menuIcon = view.findViewById(R.id.menu_icon);
        donationGrid = view.findViewById(R.id.donation_grid);
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchLayout = view.findViewById(R.id.search_layout);
        backArrow = view.findViewById(R.id.back_arrow);
        normalToolbarContent = view.findViewById(R.id.normal_toolbar_content);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        sortIcon = view.findViewById(R.id.sort_icon);

        // Set up sort functionality
        sortIcon.setOnClickListener(v -> showSortOptions());

        ImageView searchBackButton = view.findViewById(R.id.search_back_button);
        searchBackButton.setOnClickListener(v -> closeSearch());

        // Pre-cache layouts and set initial states
        searchLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        normalToolbarContent.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        toolbar.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Pre-measure and cache layouts
        preWarmSearchLayout();

        // Optimize search icon for faster touch response
        searchIcon.setClickable(true);
        searchIcon.setFocusable(true);
        searchIcon.setOnTouchListener((v, event) -> {
            if (isSearchMode) return false;
            showSearch();
            return true;
        });

        // Optimize back button
        searchBackButton.setClickable(true);
        searchBackButton.setFocusable(true);
        searchBackButton.setOnTouchListener((v, event) -> {
            if (!isSearchMode) return false;
            closeSearch();
            return true;
        });

        // Optimize search text changes with debouncing
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> filterItems(s.toString());
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
            }
        });

        // Initialize filter buttons
        allFilterButton = view.findViewById(R.id.allFilterButton);
        activeFilterButton = view.findViewById(R.id.activeFilterButton);
        completedFilterButton = view.findViewById(R.id.completedFilterButton);

        // Set click listeners for filter buttons
        allFilterButton.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterButtonStates();
            quickApplyFilter();
        });

        activeFilterButton.setOnClickListener(v -> {
            currentFilter = "active";
            updateFilterButtonStates();
            quickApplyFilter();
        });

        completedFilterButton.setOnClickListener(v -> {
            currentFilter = "completed";
            updateFilterButtonStates();
            quickApplyFilter();
        });

        // Set initial button state
        updateFilterButtonStates();
    }

    private void checkUserExistence(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = currentUser.getEmail();

        if (email != null) {
            db.collection("users")
                    .document(email)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            // Create a new user document if it doesn't exist
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("username", "");
                            userData.put("location", "");
                            userData.put("phoneNumber", "");
                            userData.put("profileImage", "");

                            db.collection("users")
                                    .document(email)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User document created"))
                                    .addOnFailureListener(e -> Log.e("FirestoreError", "Failed to create user document: " + e.getMessage()));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("FirestoreError", "Error checking user document: " + e.getMessage()));
        }
    }

    private void loadItems() {
        Log.d("HomeFragment", "loadItems() called"); // <-- ADD HERE
        swipeRefreshLayout.setRefreshing(true);
        allItems.clear();
        filteredListsCache.clear(); // Clear the cache when loading new items

        donationItemRepo.getAllOfferItems(new OfferEssentialRepository.OnOfferEssentialsLoadedListener() {
            @Override
            public void onOfferEssentialsLoaded(List<OfferEssential> donationItems) {
                Log.d("HomeFragment", "onOfferEssentialsLoaded called"); // <-- ADD HERE
                Log.d("HomeFragment", "Loaded items count: " + donationItems.size()); // <-- ADD HERE

                // Print each item name
                for (OfferEssential item : donationItems) {
                    Log.d("HomeFragment", "Item: " + item.getName()); // <-- ADD HERE
                }

                Toast.makeText(getActivity(), "Loaded " + donationItems.size() + " items", Toast.LENGTH_SHORT).show(); // Optional toast

                allItems.addAll(donationItems);

                loadRequestItems(); // <-- If this does filtering, the list might get reduced to 0 here
            }

            @Override
            public void onError(Exception e) {
                Log.e("HomeFragment", "Error loading offer items", e); // <-- ADD HERE
                Toast.makeText(getActivity(), "Error loading offer items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    private void loadRequestItems() {
        requestItemRepo.getAllRequestItems(new RequestEssentialRepository.OnRequestEssentialsLoadedListener() {
            @Override
            public void onRequestEssentialsLoaded(List<RequestEssential> requestItems) {
                allItems.addAll(requestItems);
                // After loading all items, display them
                displayItems();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getActivity(), "Error loading request items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void displayItems() {
        // Check if fragment is attached
        if (!isAdded() || getActivity() == null) {
            return;
        }

        try {
            // Clear grid and sort items
            donationGrid.removeAllViews();
            Collections.sort(allItems, (a, b) -> {
                long aTime = (a instanceof OfferEssential) ? ((OfferEssential) a).getCreatedAt() : ((RequestEssential) a).getCreatedAt();
                long bTime = (b instanceof OfferEssential) ? ((OfferEssential) b).getCreatedAt() : ((RequestEssential) b).getCreatedAt();
                return Long.compare(bTime, aTime); // Descending order
            });

            // Add views for all items
            for (Object item : allItems) {
                String itemStatus = null;

                if (item instanceof OfferEssential) {
                    itemStatus = ((OfferEssential) item).getStatus();
                } else if (item instanceof RequestEssential) {
                    itemStatus = ((RequestEssential) item).getStatus();
                }

                // Apply filter
                if (currentFilter.equals("all") ||
                        (currentFilter.equals("active") && "active".equals(itemStatus)) ||
                        (currentFilter.equals("completed") && "completed".equals(itemStatus))) {

                    if (item instanceof OfferEssential) {
                        addDonationItemView((OfferEssential) item);
                    } else if (item instanceof RequestEssential) {
                        addRequestItemView((RequestEssential) item);
                    }
                }
            }

            swipeRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDonationItemView(OfferEssential item) {
        // Check if fragment is attached
        if (!isAdded() || getActivity() == null || getContext() == null) {
            return;
        }

        try {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View itemView = inflater.inflate(R.layout.donation_item_view, donationGrid, false);

            ImageView itemImage = itemView.findViewById(R.id.category_icon);
            TextView itemName = itemView.findViewById(R.id.item_name);
            TextView itemCategory = itemView.findViewById(R.id.item_category);
            TextView itemInfo = itemView.findViewById(R.id.item_expiredDate);
            TextView itemQuantity = itemView.findViewById(R.id.item_quantity);
            TextView itemPickupTime = itemView.findViewById(R.id.item_pickupTime);
            TextView itemLocation = itemView.findViewById(R.id.item_distance);
            TextView statusIndicator = itemView.findViewById(R.id.status_indicator);

            // Show status indicator if item is completed
            if (item.getStatus() != null && item.getStatus().equals("completed")) {
                statusIndicator.setVisibility(View.VISIBLE);
                // Optional: Add some visual dimming to the entire card
                itemView.setAlpha(0.8f);
            } else {
                statusIndicator.setVisibility(View.GONE);
                itemView.setAlpha(1.0f);
            }

            // Load image using Glide
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(itemImage);
            } else {
                try {
                    itemImage.setImageResource(item.getImageResourceId());
                } catch (Resources.NotFoundException e) {
                    Log.w("HomeFragment", "Invalid imageResourceId: " + item.getImageResourceId() + ". Using placeholder.");
                    itemImage.setImageResource(R.drawable.placeholder_image);
                }
            }


            itemName.setText(item.getName());

            if ("Essential".equals(item.getOfferType())) {
                itemCategory.setText("Essential Category : " + (item.getEssentialCategory() != null ? item.getEssentialCategory() : "N/A"));
                itemInfo.setText("Expires : " + (item.getExpiredDate() != null ? item.getExpiredDate() : "N/A"));
                itemQuantity.setText("Quantity : " + (item.getQuantity() != null ? item.getQuantity() : "N/A"));
                itemPickupTime.setText("Pickup Time : " + (item.getPickupTime() != null ? item.getPickupTime() : "N/A"));
                itemLocation.setText(item.getLocation());

                itemView.setOnClickListener(v -> {
                    OfferEssentialDetails detailFragment = OfferEssentialDetails.newInstance(item);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, detailFragment)
                            .addToBackStack(null)
                            .commit();
                });
            } else {
                itemCategory.setText("Item Category : " + (item.getEssentialCategory() != null ? item.getEssentialCategory() : "N/A"));
                itemInfo.setText("Description : " + (item.getDescription() != null ? item.getDescription() : "N/A"));
                itemQuantity.setText("Quantity : " + (item.getQuantity() != null ? item.getQuantity() : "N/A"));
                itemPickupTime.setText("Pickup Time : " + (item.getPickupTime() != null ? item.getPickupTime() : "N/A"));
                itemLocation.setText(item.getLocation());

                itemView.setOnClickListener(v -> {
                    OfferReliefDetails detailFragment = OfferReliefDetails.newInstance(item);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, detailFragment)
                            .addToBackStack(null)
                            .commit();
                });
            }

            itemQuantity.setText("Quantity : " + (item.getQuantity() != null ? item.getQuantity() : "N/A"));
            itemPickupTime.setText("Pickup Time : " + (item.getPickupTime() != null ? item.getPickupTime() : "N/A"));
            itemLocation.setText(item.getLocation());

            donationGrid.addView(itemView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRequestItemView(RequestEssential item) {
        View itemView = getLayoutInflater().inflate(R.layout.donation_item_view, donationGrid, false);

        ImageView itemImage = itemView.findViewById(R.id.category_icon);
        TextView itemName = itemView.findViewById(R.id.item_name);
        TextView itemCategory = itemView.findViewById(R.id.item_category);
        TextView itemInfo = itemView.findViewById(R.id.item_expiredDate);
        TextView itemQuantity = itemView.findViewById(R.id.item_quantity);
        TextView itemPickupTime = itemView.findViewById(R.id.item_pickupTime);
        TextView itemLocation = itemView.findViewById(R.id.item_distance);
        TextView statusIndicator = itemView.findViewById(R.id.status_indicator);

        // Show status indicator if item is completed
        if (item.getStatus() != null && item.getStatus().equals("completed")) {
            statusIndicator.setVisibility(View.VISIBLE);
            // Optional: Add some visual dimming to the entire card
            itemView.setAlpha(0.8f);
        } else {
            statusIndicator.setVisibility(View.GONE);
            itemView.setAlpha(1.0f);
        }

        // Load image using Glide
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(itemImage);
        } else {
            itemImage.setImageResource(item.getImageResourceId());
        }

        itemName.setText(item.getName());

        if ("Essential".equals(item.getRequestType())) {
            itemCategory.setText("Item Category : " + (item.getEssentialCategory() != null ? item.getEssentialCategory() : "N/A"));
            itemInfo.setText("Urgency Level : " + (item.getUrgencyLevel() != null ? item.getUrgencyLevel() : "N/A"));
            itemQuantity.setText("Quantity : " + (item.getQuantity() != null ? item.getQuantity() : "N/A"));
            itemLocation.setText(item.getLocation());

            itemPickupTime.setVisibility(View.GONE);
            itemView.setOnClickListener(v -> {
                RequestEssentialDetails detailFragment = RequestEssentialDetails.newInstance(item);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            });
        } else {
            itemCategory.setText("Item Category : " + (item.getEssentialCategory() != null ? item.getEssentialCategory() : "N/A"));
            itemInfo.setText("Urgency Level : " + (item.getUrgencyLevel() != null ? item.getUrgencyLevel() : "N/A"));
            itemQuantity.setText("Quantity : " + (item.getQuantity() != null ? item.getQuantity() : "N/A"));
            itemLocation.setText(item.getLocation());

            itemPickupTime.setVisibility(View.GONE);
            itemView.setOnClickListener(v -> {
                RequestReliefDetails detailFragment = RequestReliefDetails.newInstance(item);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
        donationGrid.addView(itemView);
    }

    private void filterItems(String query) {
        currentSearchQuery = query != null ? query : "";
        quickApplyFilter();
    }

    private void showSortOptions() {
        String[] options = Arrays.stream(SortOption.values())
                .map(SortOption::getDisplayName)
                .toArray(String[]::new);

        new AlertDialog.Builder(requireContext())
                .setTitle("Sort by")
                .setSingleChoiceItems(options, currentSortOption.ordinal(), (dialog, which) -> {
                    currentSortOption = SortOption.values()[which];
                    if (currentSortOption != SortOption.DEFAULT) {
                        showSortDirectionDialog();
                    } else {
                        sortItems();
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void showSortDirectionDialog() {
        String[] directions = Arrays.stream(SortDirection.values())
                .map(SortDirection::getDisplayName)
                .toArray(String[]::new);

        new AlertDialog.Builder(requireContext())
                .setTitle("Sort Direction")
                .setSingleChoiceItems(directions, currentSortDirection.ordinal(), (dialog, which) -> {
                    currentSortDirection = SortDirection.values()[which];
                    sortItems();
                    dialog.dismiss();
                })
                .show();
    }

    private void sortItems() {
        quickApplyFilter();
    }

    private void closeSearch() {
        if (!isSearchMode) return;
        isSearchMode = false;

        // Hide keyboard first since it's the most time-consuming operation
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }

        // Batch view updates in a single operation
        searchLayout.setVisibility(View.GONE);
        normalToolbarContent.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);

        // Clear search text without animation
        searchEditText.setText(null);
    }

    private void showSearch() {
        if (isSearchMode) return;
        isSearchMode = true;

        // Pre-fetch next frame
        rootView.post(() -> {
            // Hardware acceleration
            searchLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            // Update visibilities in a single frame
            searchLayout.setAlpha(1f);
            searchLayout.setVisibility(View.VISIBLE);
            normalToolbarContent.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);

            // Show keyboard
            searchEditText.requestFocus();
            if (imm != null) {
                imm.showSoftInput(searchEditText, 0);
            }
        });
    }

    // Add method to pre-warm the search layout
    private void preWarmSearchLayout() {
        if (searchLayout != null) {
            searchLayout.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            searchLayout.layout(0, 0, searchLayout.getMeasuredWidth(), searchLayout.getMeasuredHeight());
        }
    }

    private void updateFilterButtonStates() {
        // Reset all buttons to unselected state
        allFilterButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.button_green)));
        activeFilterButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.button_green)));
        completedFilterButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.button_green)));

        allFilterButton.setBackgroundColor(Color.TRANSPARENT);
        activeFilterButton.setBackgroundColor(Color.TRANSPARENT);
        completedFilterButton.setBackgroundColor(Color.TRANSPARENT);

        allFilterButton.setTextColor(getResources().getColor(R.color.button_green));
        activeFilterButton.setTextColor(getResources().getColor(R.color.button_green));
        completedFilterButton.setTextColor(getResources().getColor(R.color.button_green));

        // Highlight selected button
        switch (currentFilter) {
            case "all":
                allFilterButton.setBackgroundColor(getResources().getColor(R.color.button_green));
                allFilterButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.button_green)));
                allFilterButton.setTextColor(Color.WHITE);
                break;
            case "active":
                activeFilterButton.setBackgroundColor(getResources().getColor(R.color.button_green));
                activeFilterButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.button_green)));
                activeFilterButton.setTextColor(Color.WHITE);
                break;
            case "completed":
                completedFilterButton.setBackgroundColor(getResources().getColor(R.color.button_green));
                completedFilterButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.button_green)));
                completedFilterButton.setTextColor(Color.WHITE);
                break;
        }
    }

    private void quickApplyFilter() {
        // Check if we have a cached filtered list
        List<Object> filteredItems = filteredListsCache.get(currentFilter);

        if (filteredItems == null) {
            // Create and cache the filtered list if it doesn't exist
            filteredItems = new ArrayList<>();
            for (Object item : allItems) {
                String itemStatus = null;
                if (item instanceof OfferEssential) {
                    itemStatus = ((OfferEssential) item).getStatus();
                } else if (item instanceof RequestEssential) {
                    itemStatus = ((RequestEssential) item).getStatus();
                }

                if (currentFilter.equals("all") ||
                        (currentFilter.equals("active") && "active".equals(itemStatus)) ||
                        (currentFilter.equals("completed") && "completed".equals(itemStatus))) {
                    filteredItems.add(item);
                }
            }
            filteredListsCache.put(currentFilter, filteredItems);
        }

        // Apply search filter if there's an active search
        List<Object> finalItems = filteredItems;
        if (!currentSearchQuery.isEmpty()) {
            String lowercaseQuery = currentSearchQuery.toLowerCase();
            finalItems = filteredItems.stream()
                    .filter(item -> {
                        String name = (item instanceof OfferEssential) ?
                                ((OfferEssential) item).getName() :
                                ((RequestEssential) item).getName();
                        return name != null && name.toLowerCase().contains(lowercaseQuery);
                    })
                    .collect(Collectors.toList());
        }

        // Apply current sort if needed
        if (currentSortOption != SortOption.DEFAULT) {
            sortItemsList(finalItems);
        }

        // Update UI only if the content has changed
        if (!finalItems.equals(currentDisplayedItems)) {
            currentDisplayedItems = new ArrayList<>(finalItems);
            updateGridView(finalItems);
        }
    }

    private void updateGridView(List<Object> items) {
        donationGrid.removeAllViews();
        for (Object item : items) {
            if (item instanceof OfferEssential) {
                addDonationItemView((OfferEssential) item);
            } else if (item instanceof RequestEssential) {
                addRequestItemView((RequestEssential) item);
            }
        }
    }

    private void sortItemsList(List<Object> items) {
        switch (currentSortOption) {
            case DATE_CREATED:
                items.sort((a, b) -> {
                    long timeA = (a instanceof OfferEssential) ? ((OfferEssential) a).getCreatedAt() : ((RequestEssential) a).getCreatedAt();
                    long timeB = (b instanceof OfferEssential) ? ((OfferEssential) b).getCreatedAt() : ((RequestEssential) b).getCreatedAt();
                    return currentSortDirection == SortDirection.ASCENDING ?
                            Long.compare(timeA, timeB) : Long.compare(timeB, timeA);
                });
                break;
            case EXPIRY_DATE:
                items.sort((a, b) -> {
                    if (a instanceof OfferEssential && b instanceof OfferEssential) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                            Date dateA = sdf.parse(((OfferEssential) a).getExpiredDate());
                            Date dateB = sdf.parse(((OfferEssential) b).getExpiredDate());
                            return currentSortDirection == SortDirection.ASCENDING ?
                                    dateA.compareTo(dateB) : dateB.compareTo(dateA);
                        } catch (Exception e) {
                            return 0;
                        }
                    }
                    return 0;
                });
                break;
            case CATEGORY:
                items.sort((a, b) -> {
                    if (a instanceof OfferEssential && b instanceof OfferEssential) {
                        String categoryA = ((OfferEssential) a).getEssentialCategory();
                        String categoryB = ((OfferEssential) b).getEssentialCategory();
                        if (categoryA == null) categoryA = "";
                        if (categoryB == null) categoryB = "";
                        int result = categoryA.compareToIgnoreCase(categoryB);
                        return currentSortDirection == SortDirection.ASCENDING ? result : -result;
                    }
                    return 0;
                });
                break;
            case QUANTITY:
                items.sort((a, b) -> {
                    try {
                        String qtyA = (a instanceof OfferEssential) ?
                                ((OfferEssential) a).getQuantity() : ((RequestEssential) a).getQuantity();
                        String qtyB = (b instanceof OfferEssential) ?
                                ((OfferEssential) b).getQuantity() : ((RequestEssential) b).getQuantity();

                        int numA = Integer.parseInt(qtyA.replaceAll("[^0-9]", ""));
                        int numB = Integer.parseInt(qtyB.replaceAll("[^0-9]", ""));
                        return currentSortDirection == SortDirection.ASCENDING ?
                                Integer.compare(numA, numB) : Integer.compare(numB, numA);
                    } catch (Exception e) {
                        return 0;
                    }
                });
                break;
            case LOCATION:
                items.sort((a, b) -> {
                    String locA = (a instanceof OfferEssential) ?
                            ((OfferEssential) a).getLocation() : ((RequestEssential) a).getLocation();
                    String locB = (b instanceof OfferEssential) ?
                            ((OfferEssential) b).getLocation() : ((RequestEssential) b).getLocation();
                    if (locA == null) locA = "";
                    if (locB == null) locB = "";
                    int result = locA.compareToIgnoreCase(locB);
                    return currentSortDirection == SortDirection.ASCENDING ? result : -result;
                });
                break;
            case PICKUP_TIME:
                items.sort((a, b) -> {
                    if (a instanceof OfferEssential && b instanceof OfferEssential) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                            Date timeA = sdf.parse(((OfferEssential) a).getPickupTime());
                            Date timeB = sdf.parse(((OfferEssential) b).getPickupTime());
                            return currentSortDirection == SortDirection.ASCENDING ?
                                    timeA.compareTo(timeB) : timeB.compareTo(timeA);
                        } catch (Exception e) {
                            return 0;
                        }
                    }
                    return 0;
                });
                break;
            case DEFAULT:
            default:
                // Keep the default order (by creation date)
                items.sort((a, b) -> {
                    long timeA = (a instanceof OfferEssential) ? ((OfferEssential) a).getCreatedAt() : ((RequestEssential) a).getCreatedAt();
                    long timeB = (b instanceof OfferEssential) ? ((OfferEssential) b).getCreatedAt() : ((RequestEssential) b).getCreatedAt();
                    return Long.compare(timeB, timeA); // Descending order by default
                });
                break;
        }
    }
}