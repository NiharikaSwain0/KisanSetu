package com.kisansetu.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kisansetu.app.databinding.ItemDeliveryBinding;
import com.kisansetu.app.models.Delivery;
import java.util.List;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder> {

    private List<Delivery> deliveryList;
    private OnDeliveryClickListener listener;

    public interface OnDeliveryClickListener {
        void onItemClick(Delivery delivery);
    }

    public DeliveryAdapter(List<Delivery> deliveryList, OnDeliveryClickListener listener) {
        this.deliveryList = deliveryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeliveryBinding binding = ItemDeliveryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DeliveryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        Delivery delivery = deliveryList.get(position);
        holder.bind(delivery);
    }

    @Override
    public int getItemCount() {
        return deliveryList.size();
    }

    class DeliveryViewHolder extends RecyclerView.ViewHolder {
        private ItemDeliveryBinding binding;

        public DeliveryViewHolder(ItemDeliveryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Delivery delivery) {
            binding.deliveryIdTextView.setText("Delivery #" + delivery.getDeliveryId().substring(0, 5).toUpperCase());
            binding.deliveryStatusTextView.setText("Status: " + delivery.getStatus());
            binding.farmerAddressTextView.setText("Pickup: " + delivery.getFarmerAddress());
            binding.customerAddressTextView.setText("Drop: " + delivery.getCustomerAddress());
            
            // Simulation of distance for hackathon demo
            binding.distanceText.setText("2.4 KM");

            itemView.setOnClickListener(v -> listener.onItemClick(delivery));
            
            binding.acceptBtn.setOnClickListener(v -> {
                // Logic for accepting order
                listener.onItemClick(delivery);
            });
            
            binding.rejectBtn.setOnClickListener(v -> {
                // Logic for rejecting order
            });
        }
    }
}
