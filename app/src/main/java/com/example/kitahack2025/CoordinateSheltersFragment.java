package com.example.kitahack2025;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CoordinateSheltersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoordinateSheltersFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText shelterNameInput, shelterAddressInput, shelterCapacityInput, dateOpenedInput;
    private Spinner operationalHoursSpinner, shelterTypeSpinner;
    private TextView shelterContactInput;
    private Button submitButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    public CoordinateSheltersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CoordinateSheltersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CoordinateSheltersFragment newInstance(String param1, String param2) {
        CoordinateSheltersFragment fragment = new CoordinateSheltersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coordinate_shelters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupSpinners();
        setupDatePicker();
        setupSubmitButton();
    }

    private void initializeViews(View view) {
        shelterNameInput = view.findViewById(R.id.event_name_input);
        shelterAddressInput = view.findViewById(R.id.event_location_input);
        shelterCapacityInput = view.findViewById(R.id.event_seats_available_input);
        dateOpenedInput = view.findViewById(R.id.event_date_input);
        shelterContactInput = view.findViewById(R.id.event_description_input);
        shelterTypeSpinner = view.findViewById(R.id.shelter_type_spinner);
        operationalHoursSpinner = view.findViewById(R.id.operational_hours_input);
        submitButton = view.findViewById(R.id.submit_button);

        // Make date input non-editable
        dateOpenedInput.setFocusable(false);
    }

    private void setupSpinners() {
        // Setup shelter type spinner
        String[] shelterTypes = {"Medical Camps", "Evacuation"};
        ArrayAdapter<String> shelterTypeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, shelterTypes);
        shelterTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shelterTypeSpinner.setAdapter(shelterTypeAdapter);

        // Setup operational hours spinner
        String[] operationalHours = {
                "24 Hours",
                "8:00 AM - 8:00 PM",
                "9:00 AM - 5:00 PM",
                "6:00 AM - 6:00 PM",
                "7:00 AM - 7:00 PM"
        };
        ArrayAdapter<String> hoursAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, operationalHours);
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operationalHoursSpinner.setAdapter(hoursAdapter);
    }

    private void setupDatePicker() {
        dateOpenedInput.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    dateOpenedInput.setText(dateFormatter.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> submitShelter());
    }

    private void submitShelter() {
        if (!validateInputs()) {
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to submit a shelter", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create shelter data
        Map<String, Object> shelterData = new HashMap<>();
        shelterData.put("name", shelterNameInput.getText().toString().trim());
        shelterData.put("address", shelterAddressInput.getText().toString().trim());
        shelterData.put("capacity", shelterCapacityInput.getText().toString().trim());
        shelterData.put("operationalHours", operationalHoursSpinner.getSelectedItem().toString());
        shelterData.put("dateOpened", dateOpenedInput.getText().toString().trim());
        shelterData.put("type", shelterTypeSpinner.getSelectedItem().toString());
        shelterData.put("ownerEmail", currentUser.getEmail());
        shelterData.put("status", "active");

        // Save to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("shelters")
                .add(shelterData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Shelter added successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add shelter: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs() {
        if (shelterNameInput == null || shelterNameInput.getText().toString().trim().isEmpty()) {
            shelterNameInput.setError("Shelter name is required");
            return false;
        }
        if (shelterAddressInput == null || shelterAddressInput.getText().toString().trim().isEmpty()) {
            shelterAddressInput.setError("Address is required");
            return false;
        }
        if (shelterCapacityInput == null || shelterCapacityInput.getText().toString().trim().isEmpty()) {
            shelterCapacityInput.setError("Capacity is required");
            return false;
        }
        if (dateOpenedInput == null || dateOpenedInput.getText().toString().trim().isEmpty()) {
            dateOpenedInput.setError("Date opened is required");
            return false;
        }
        if (shelterTypeSpinner == null || shelterTypeSpinner.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Shelter type is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}