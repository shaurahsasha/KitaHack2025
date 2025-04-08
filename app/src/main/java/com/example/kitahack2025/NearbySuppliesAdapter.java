package com.example.kitahack2025;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NearbySuppliesAdapter extends RecyclerView.Adapter<NearbySuppliesAdapter.ViewHolder> {
    private final List<OfferEssential> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(OfferEssential item);
    }

    public NearbySuppliesAdapter(List<OfferEssential> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nearby_supply, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OfferEssential item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView categoryTextView;
        private final TextView distanceTextView;
        private final ImageView categoryIcon;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.supply_title);
            categoryTextView = itemView.findViewById(R.id.supply_category);
            distanceTextView = itemView.findViewById(R.id.supply_distance);
            categoryIcon = itemView.findViewById(R.id.category_icon);
        }

        void bind(final OfferEssential item, final OnItemClickListener listener) {
            titleTextView.setText(item.getName());
            categoryTextView.setText(item.getCategory());

            // Set category icon
            int iconResource = "Food".equals(item.getCategory()) ?
                    R.drawable.ic_aegisaqua : R.drawable.ic_aegisaqua;
            categoryIcon.setImageResource(iconResource);

            // Calculate and display distance
            if (item.getLocation() != null) {
                DistanceCalculator calc = new DistanceCalculator("AIzaSyD3paVgDTxJxSRCxUy0cj09SEee_fEB9Zc");
                calc.calculateDistance(item.getLocation(), item.getLocation(),
                    new DistanceCalculator.DistanceCalculationCallback() {
                        @Override
                        public void onSuccess(double distance) {
                            distanceTextView.setText(String.format("%.1f km away", distance));
                        }

                        @Override
                        public void onFailure(String error) {
                            distanceTextView.setText("Distance unknown");
                        }
                    });
            } else {
                distanceTextView.setText("Location unknown");
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
