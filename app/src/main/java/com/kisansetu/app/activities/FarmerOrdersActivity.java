package com.kisansetu.app.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kisansetu.app.adapters.OrderAdapter;
import com.kisansetu.app.databinding.ActivityFarmerOrdersBinding;
import com.kisansetu.app.models.Order;
import java.util.ArrayList;
import java.util.List;

public class FarmerOrdersActivity extends AppCompatActivity {

    private ActivityFarmerOrdersBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Order> orderList;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFarmerOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        orderList = new ArrayList<>();

        adapter = new OrderAdapter(orderList, new OrderAdapter.OnOrderClickListener() {

            @Override
            public void onAcceptClick(Order order) {
                updateOrderStatus(order, "Accepted");
            }

            @Override
            public void onRejectClick(Order order) {
                updateOrderStatus(order, "Rejected");
            }

            @Override
            public void onItemClick(Order order) {
                // View order details
            }

            @Override
            public void onAssignRiderClick(Order order) {
                // optional
            }
            @Override
            public void onSelfDeliveryClick(Order order){

            }
        });

        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.ordersRecyclerView.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        db.collection("Orders")
                .whereEqualTo("farmerId", mAuth.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        orderList.add(order);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void updateOrderStatus(Order order, String status) {
        db.collection("Orders")
                .document(order.getOrderId())
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order " + status, Toast.LENGTH_SHORT).show();
                    loadOrders();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating order status", Toast.LENGTH_SHORT).show();
                });
    }
}