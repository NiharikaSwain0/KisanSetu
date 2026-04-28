package com.kisansetu.app.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kisansetu.app.R;
import com.kisansetu.app.adapters.DeliveryAdapter;
import com.kisansetu.app.databinding.ActivityRiderDashboardBinding;
import com.kisansetu.app.models.Delivery;
import com.kisansetu.app.models.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RiderDashboardActivity extends AppCompatActivity {

    private ActivityRiderDashboardBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    private static final String CHANNEL_ID = "delivery_notifications";
    private List<Delivery> deliveryList;
    private DeliveryAdapter adapter;
    private boolean isInitialLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRiderDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        deliveryList = new ArrayList<>();

        createNotificationChannel();
        setupRecyclerView();
        loadRiderInfo();
        checkLocationPermission();
        checkNotificationPermission();
        // setupStats(); // Removed as we changed the UI to focus on Today's Earning
        loadActiveDeliveries();

        binding.onlineToggleSwitch.setOnClickListener(v -> {
            boolean isChecked = binding.onlineToggleSwitch.isChecked();
            updateOnlineStatus(isChecked);
        });

        binding.profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(RiderDashboardActivity.this, ProfileActivity.class));
        });

        // binding.logoutButton.setOnClickListener(v -> { // Removed from main dashboard layout
        //     mAuth.signOut();
        //     Intent intent = new Intent(RiderDashboardActivity.this, LoginActivity.class);
        //     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //     startActivity(intent);
        // });
    }

    private void setupRecyclerView() {
        adapter = new DeliveryAdapter(deliveryList, delivery -> {
            Intent intent = new Intent(RiderDashboardActivity.this, DeliveryDetailActivity.class);
            intent.putExtra("deliveryId", delivery.getDeliveryId());
            startActivity(intent);
        });
        binding.deliveriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.deliveriesRecyclerView.setAdapter(adapter);
    }

    private void loadRiderInfo() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("Users").document(uid).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    binding.riderGreetingTextView.setText(user.getFullName());
                    // if (user.getVehicleType() != null && !user.getVehicleType().isEmpty()) {
                    //     binding.vehicleInfoTextView.setText("Vehicle: " + user.getVehicleType() + " (" + user.getVehicleNumber() + ")");
                    // }
                    binding.onlineToggleSwitch.setChecked(user.isOnline());
                    updateStatusUI(user.isOnline());

                    if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                        Glide.with(this)
                                .load(user.getProfileImage())
                                .placeholder(android.R.drawable.ic_menu_camera)
                                .into(binding.profileIcon);
                    }
                }
            }
        });
    }

    private void loadActiveDeliveries() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("Deliveries")
                .whereEqualTo("riderId", uid)
                .whereNotEqualTo("status", "Delivered")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        boolean hasNewDelivery = value.size() > deliveryList.size() && !isInitialLoad;
                        
                        deliveryList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            deliveryList.add(doc.toObject(Delivery.class));
                        }
                        adapter.notifyDataSetChanged();
                        
                        if (hasNewDelivery) {
                            sendNewDeliveryNotification();
                        }
                        isInitialLoad = false;
                    }
                });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Delivery Notifications";
            String description = "Notifications for new delivery assignments";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNewDeliveryNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(this, RiderDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New Delivery Assigned!")
                .setContentText("You have a new delivery task. Open the app to view details.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void updateOnlineStatus(boolean isOnline) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("Users").document(uid).update("isOnline", isOnline)
                .addOnSuccessListener(aVoid -> {
                    String msg = isOnline ? "You are now Online" : "You are now Offline";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.onlineToggleSwitch.setChecked(!isOnline);
                    updateStatusUI(!isOnline);
                    Toast.makeText(this, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLocationUI(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String fullLocation = (city != null ? city : "") + (state != null ? ", " + state : "");
                binding.locationTextView.setText("Location: " + fullLocation);
                
                // Update in Firestore
                String uid = mAuth.getUid();
                if (uid != null) {
                    db.collection("Users").document(uid).update("address", fullLocation);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateStatusUI(boolean isOnline) {
        if (isOnline) {
            binding.statusLabelTextView.setText("You are Online");
            binding.statusLabelTextView.setTextColor(getResources().getColor(R.color.rider_accent_lime));
        } else {
            binding.statusLabelTextView.setText("You are Offline");
            binding.statusLabelTextView.setTextColor(getResources().getColor(R.color.rider_reject_red));
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    updateStatusInFirestore(location);
                    updateLocationUI(location);
                }
            });
        }
    }

    private void updateStatusInFirestore(Location location) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("Users").document(uid)
                .update("latitude", location.getLatitude(), "longitude", location.getLongitude())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update live location", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupStats() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        // Fetch earnings from orders where this rider is assigned
        db.collection("Orders")
                .whereEqualTo("riderId", uid)
                .whereEqualTo("status", "Delivered")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double earnings = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Rider gets 10% commission for demo
                        Double total = doc.getDouble("totalAmount");
                        if (total != null) {
                            earnings += total * 0.10;
                        }
                    }
                    binding.dailyEarningsTextView.setText("₹ " + String.format("%.2f", earnings));
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
