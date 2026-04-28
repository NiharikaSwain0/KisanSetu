package com.kisansetu.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import com.kisansetu.app.adapters.DashboardOrderAdapter;
import com.kisansetu.app.adapters.DashboardProductAdapter;
import com.kisansetu.app.databinding.ActivityFarmerDashboardBinding;
import com.kisansetu.app.models.Order;
import com.kisansetu.app.models.Product;
import com.kisansetu.app.models.User;
import com.kisansetu.app.utils.VoiceAssistantHelper;
import com.kisansetu.app.utils.VoiceCommandProcessor;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FarmerDashboardActivity extends AppCompatActivity {

    ActivityFarmerDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private DashboardProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> filteredProductList = new ArrayList<>();

    private DashboardOrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();
    
    private VoiceAssistantHelper voiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ VERY IMPORTANT (this fixes your error)
        binding = ActivityFarmerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupRecyclerViews();
        setupSearch();
        loadFarmName();
        loadProducts();
        loadOrders();
        initVoiceAssistant();
        requestLocation();

        // ✅ Button click (this will now work)
        binding.addProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        binding.smartToolsButton.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, SmartToolsActivity.class);
            startActivity(intent);
        });

        // ✅ Optional: change button text
        binding.addProductButton.setText("Add Product");

        binding.notificationIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show();
        });

        binding.profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // ✅ Profile click (to edit farmer data)
        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        binding.micButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 200);
            } else {
                voiceHelper.startListening();
                Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String query) {
        filteredProductList.clear();
        if (query.isEmpty()) {
            filteredProductList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredProductList.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void initVoiceAssistant() {
        voiceHelper = new VoiceAssistantHelper(this, new VoiceAssistantHelper.VoiceAssistantListener() {
            @Override
            public void onResults(String text) {
                // Show what was heard to debug
                Toast.makeText(FarmerDashboardActivity.this, "Heard: " + text, Toast.LENGTH_SHORT).show();
                processVoiceCommand(text.toLowerCase());
            }

            @Override
            public void onError(String error) {
                Toast.makeText(FarmerDashboardActivity.this, "Voice Assistant: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processVoiceCommand(String command) {
        String jsonResponse = VoiceCommandProcessor.process(command);
        Log.d("VoiceCommand", "Response: " + jsonResponse);

        try {
            JSONObject response = new JSONObject(jsonResponse);
            String action = response.optString("action");

            switch (action) {
                case "search_product":
                    String query = response.optString("query");
                    if (!query.isEmpty()) {
                        voiceHelper.speak("Theek hai, " + query + " ke liye search kar raha hoon.");
                        binding.searchEditText.setText(query);
                        filterProducts(query);
                    } else {
                        voiceHelper.speak("Aap kya search karna chahte hain?");
                    }
                    break;

                case "add_to_cart":
                    String product = response.optString("product");
                    if (!product.isEmpty()) {
                        voiceHelper.speak("Theek hai, " + product + " ko cart mein daal raha hoon.");
                        Toast.makeText(this, product + " added to cart", Toast.LENGTH_SHORT).show();
                    } else {
                        voiceHelper.speak("Kya add karna hai?");
                    }
                    break;

                case "add_product":
                    voiceHelper.speak("Theek hai, naya product add karne ke liye screen khol raha hoon.");
                    startActivity(new Intent(this, AddProductActivity.class));
                    break;

                case "open_profile":
                    voiceHelper.speak("Theek hai, aapki profile khol raha hoon.");
                    startActivity(new Intent(this, ProfileActivity.class));
                    break;

                case "open_cart":
                    voiceHelper.speak("Theek hai, aapki cart khol raha hoon.");
                    startActivity(new Intent(this, CartActivity.class));
                    break;

                case "open_home":
                    voiceHelper.speak("Theek hai, home screen par jaa raha hoon.");
                    // Already on home, but can refresh or scroll up
                    binding.productsRecyclerView.smoothScrollToPosition(0);
                    break;

                case "open_smart_tools":
                    voiceHelper.speak("Theek hai, smart AI tools khol raha hoon.");
                    startActivity(new Intent(this, SmartToolsActivity.class));
                    break;

                case "check_price":
                    String productPrice = response.optString("product");
                    if (!productPrice.isEmpty()) {
                        int dummyPrice = 20 + new Random().nextInt(40);
                        voiceHelper.speak(productPrice + " ka aaj ka daam lagbhag " + dummyPrice + " rupaye kilo hai.");
                    } else {
                        voiceHelper.speak("Aap kis crop ka daam jaanna chahte hain?");
                    }
                    break;

                case "detect_disease":
                    voiceHelper.speak("Theek hai, bimari check karne ke liye smart tool khol raha hoon. Kripya crop ki photo upload karein.");
                    startActivity(new Intent(this, SmartToolsActivity.class));
                    break;

                case "clarify":
                    String message = response.optString("message");
                    voiceHelper.speak(message);
                    break;

                default:
                    voiceHelper.speak("Maaf kijiye, mujhe samajh nahi aaya. Kripya phir se kahein.");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            voiceHelper.speak("Error processing command.");
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(FarmerDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceHelper != null) {
            voiceHelper.shutdown();
        }
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                updateLocationUI(location);
            }
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
                binding.locationTextView.setText(fullLocation);
                
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

    private void setupRecyclerViews() {
        // Setup Products RecyclerView
        binding.productsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        productAdapter = new DashboardProductAdapter(filteredProductList, new DashboardProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                Intent intent = new Intent(FarmerDashboardActivity.this, EditProductActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                startActivity(intent);
            }

            @Override
            public void onRemove(Product product) {
                db.collection("Products").document(product.getId()).delete()
                        .addOnSuccessListener(aVoid -> Toast.makeText(FarmerDashboardActivity.this, "Product removed", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(FarmerDashboardActivity.this, "Remove failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onClose(Product product) {
                boolean newStatus = !product.isClosed();
                db.collection("Products").document(product.getId())
                        .update("isClosed", newStatus)
                        .addOnSuccessListener(aVoid -> Toast.makeText(FarmerDashboardActivity.this, newStatus ? "Product Closed" : "Product Opened", Toast.LENGTH_SHORT).show());
            }
        });
        binding.productsRecyclerView.setAdapter(productAdapter);

        // Setup Orders RecyclerView
        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        orderAdapter = new DashboardOrderAdapter(orderList, new DashboardOrderAdapter.OnOrderActionListener() {
            @Override
            public void onAccept(Order order) {
                updateOrderStatus(order, "Accepted");
            }

            @Override
            public void onReject(Order order) {
                updateOrderStatus(order, "Rejected");
            }

            @Override
            public void onSelfDelivery(Order order) {
                updateOrderStatus(order, "Delivering (Self)");
            }

            @Override
            public void onAppDelivery(Order order) {
                updateOrderStatus(order, "Delivering (Rider)");
                // Logic to assign rider would go here
            }
        });
        binding.ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void updateOrderStatus(Order order, String status) {
        db.collection("Orders").document(order.getOrderId())
                .update("status", status)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Order " + status, Toast.LENGTH_SHORT).show());
    }

    private void loadProducts() {
        String uid = mAuth.getUid();
        if (uid != null) {
            db.collection("Products")
                    .whereEqualTo("farmerId", uid)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Toast.makeText(this, "Error loading products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (value != null) {
                            productList.clear();
                            for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                                Product product = doc.toObject(Product.class);
                                if (product != null) {
                                    productList.add(product);
                                }
                            }
                            filterProducts(binding.searchEditText.getText().toString());
                        }
                    });
        }
    }

    private void loadOrders() {
        String uid = mAuth.getUid();
        if (uid != null) {
            db.collection("Orders")
                    .whereEqualTo("farmerId", uid)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            // Order by might fail if index is not created
                            loadOrdersWithoutSort(uid);
                            return;
                        }
                        if (value != null) {
                            orderList.clear();
                            for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                                Order order = doc.toObject(Order.class);
                                if (order != null) {
                                    orderList.add(order);
                                }
                            }
                            orderAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    private void loadOrdersWithoutSort(String uid) {
        db.collection("Orders")
                .whereEqualTo("farmerId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        orderList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) {
                                orderList.add(order);
                            }
                        }
                        orderAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadFarmName() {
        String uid = mAuth.getUid();
        if (uid != null) {
            db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        if (user.getFarmName() != null) {
                            binding.farmNameTextView.setText(user.getFarmName());
                        }
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        } else if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            voiceHelper.startListening();
            Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
        }
    }
}