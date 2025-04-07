package com.example.kitahack2025;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ActionsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
//    private ImageView notificationIV;
    private Button offerEssentialButton, offerReliefButton, requestEssentialButton, requestReliefButton, coordSheltersButton, nearbySuppliesButton;
    private String mParam1;
    private String mParam2;

    public ActionsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ActionsFragment newInstance(String param1, String param2) {
        ActionsFragment fragment = new ActionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the Give Away Food button
        offerEssentialButton = view.findViewById(R.id.offer_essential_button);
        offerReliefButton = view.findViewById(R.id.offer_relief_button);
        requestEssentialButton = view.findViewById(R.id.request_essential_button);
        requestReliefButton = view.findViewById(R.id.request_essential_button);
        coordSheltersButton = view.findViewById(R.id.coord_shelters_button);
        nearbySuppliesButton = view.findViewById(R.id.nearby_supplies_button);
//        notificationIV = view.findViewById(R.id.menu_icon2);

//        notificationIV.setOnClickListener(v -> {
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new NotificationAll())
//                    .addToBackStack(null)
//                    .commit();
//        });

        // Set click listener
        offerEssentialButton.setOnClickListener(v -> {
            // Navigate to DonateItemFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new OfferEssentialFragment())
                    .addToBackStack(null)
                    .commit();
        });

        offerReliefButton.setOnClickListener(v -> {
            // Navigate to DonateNonFoodFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new OfferReliefFragment())
                    .addToBackStack(null)
                    .commit();
        });

        requestEssentialButton.setOnClickListener(v -> {
            // Navigate to RequestFoodFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RequestEssentialFragment())
                    .addToBackStack(null)
                    .commit();
        });

        requestReliefButton.setOnClickListener(v -> {
            // Navigate to RequestNonFoodFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RequestReliefFragment())
                    .addToBackStack(null)
                    .commit();
        });

        coordSheltersButton.setOnClickListener(v -> {
            // Navigate to HostActivityFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CoordinateSheltersFragment())
                    .addToBackStack(null)
                    .commit();
        });

        Button nearbySuppliesButton = view.findViewById(R.id.nearby_supplies_button);
        nearbySuppliesButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new NearbySuppliesFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}