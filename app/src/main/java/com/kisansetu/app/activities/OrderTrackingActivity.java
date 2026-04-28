package com.kisansetu.app.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kisansetu.app.databinding.ActivityOrderTrackingBinding;
import com.kisansetu.app.models.Order;

public class OrderTrackingActivity extends AppCompatActivity {

    private ActivityOrderTrackingBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        String orderId = getIntent().getStringExtra("orderId");
        if (orderId != null) {
            listenToOrder(orderId);
        }
    }

    private void listenToOrder(String orderId) {
        db.collection("Orders").document(orderId).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Order order = documentSnapshot.toObject(Order.class);
                updateUI(order);
            }
        });
    }

    private void updateUI(Order order) {
        String status = order.getStatus();
        binding.orderStatusTextView.setText("Status: " + status);

        int progress = 0;
        if ("Pending".equals(status)) progress = 25;
        else if ("Accepted".equals(status)) progress = 50;
        else if ("Picked Up".equals(status)) progress = 75;
        else if ("Delivered".equals(status)) progress = 100;

        binding.orderProgressBar.setProgress(progress);
    }
}
