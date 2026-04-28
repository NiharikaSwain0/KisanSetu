package com.kisansetu.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kisansetu.app.databinding.ActivityDeliveryDetailBinding;
import com.kisansetu.app.models.Delivery;

public class DeliveryDetailActivity extends AppCompatActivity {

    private ActivityDeliveryDetailBinding binding;
    private FirebaseFirestore db;
    private Delivery delivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        String deliveryId = getIntent().getStringExtra("deliveryId");
        loadDeliveryDetails(deliveryId);

        binding.navigateFarmerButton.setOnClickListener(v -> navigate(delivery.getFarmerAddress()));
        binding.navigateCustomerButton.setOnClickListener(v -> navigate(delivery.getCustomerAddress()));

        binding.pickedUpButton.setOnClickListener(v -> updateStatus("Picked Up"));
        binding.onTheWayButton.setOnClickListener(v -> updateStatus("On the Way"));
        binding.deliveredButton.setOnClickListener(v -> updateStatus("Delivered"));
    }

    private void loadDeliveryDetails(String deliveryId) {
        db.collection("Deliveries").document(deliveryId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                delivery = documentSnapshot.toObject(Delivery.class);
                binding.deliveryIdTextView.setText("Delivery #" + delivery.getDeliveryId());
                binding.statusTextView.setText("Status: " + delivery.getStatus());
                binding.farmerAddressTextView.setText("Pickup: " + delivery.getFarmerAddress());
                binding.customerAddressTextView.setText("Deliver to: " + delivery.getCustomerAddress());
            }
        });
    }

    private void updateStatus(String status) {
        db.collection("Deliveries").document(delivery.getDeliveryId()).update("status", status).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show();
            binding.statusTextView.setText("Status: " + status);

            // Also update the associated order status
            db.collection("Orders").document(delivery.getOrderId()).update("status", status);
        });
    }

    private void navigate(String address) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(address));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
        }
    }
}
