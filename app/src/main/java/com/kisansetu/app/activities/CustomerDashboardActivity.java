package com.kisansetu.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kisansetu.app.adapters.FarmAdapter;
import com.kisansetu.app.adapters.ProductAdapter;
import com.kisansetu.app.databinding.ActivityCustomerDashboardBinding;
import com.kisansetu.app.databinding.ItemCategoryBinding;
import com.kisansetu.app.models.Product;
import com.kisansetu.app.models.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerDashboardActivity extends AppCompatActivity {

    private ActivityCustomerDashboardBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Product> productList;
    private List<Product> filteredProductList;
    private ProductAdapter productAdapter;
    private List<User> farmList;
    private FarmAdapter farmAdapter;
    private String selectedCategory = "All";
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        productList = new ArrayList<>();
        filteredProductList = new ArrayList<>();
        farmList = new ArrayList<>();

        setupRecyclerViews();
        setupCategories();
        setupSearch();
        loadCustomerInfo();
        loadFarms();
        loadProducts();
        requestLocation();

        binding.cartFab.setOnClickListener(v -> {
            startActivity(new Intent(CustomerDashboardActivity.this, CartActivity.class));
        });

        binding.profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(CustomerDashboardActivity.this, ProfileActivity.class));
        });
    }

    private void setupRecyclerViews() {
        // Products RecyclerView
        productAdapter = new ProductAdapter(filteredProductList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onDeleteClick(Product product) {}

            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(CustomerDashboardActivity.this, ProductDetailActivity.class);
                intent.putExtra("productId", product.getId());
                startActivity(intent);
            }
        });
        binding.productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.productsRecyclerView.setAdapter(productAdapter);

        // Farms RecyclerView
        farmAdapter = new FarmAdapter(farmList, farm -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, FarmDetailActivity.class);
            intent.putExtra("farmerId", farm.getUid());
            intent.putExtra("farmName", farm.getFarmName());
            startActivity(intent);
        });
        binding.farmsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.farmsRecyclerView.setAdapter(farmAdapter);
    }

    private void setupCategories() {
        String[] categories = {"All", "Vegetables", "Fruits", "Fish", "Egg", "Chicken", "Mutton"};
        String[] categoryImages = {
                "https://cdn-icons-png.flaticon.com/512/1261/1261163.png", // All
                "https://cdn-icons-png.flaticon.com/512/2329/2329903.png", // Vegetables
                "https://cdn-icons-png.flaticon.com/512/3194/3194591.png", // Fruits
                "https://cdn-icons-png.flaticon.com/512/2970/2970030.png", // Fish
                "https://cdn-icons-png.flaticon.com/512/837/837560.png",  // Egg
                "https://i.pinimg.com/736x/83/8a/95/838a95728639252988365445f1f7063d.jpg", // Chicken (Hen photo from user)
                "https://cdn-icons-png.flaticon.com/512/1998/1998660.png"  // Mutton (Goat photo)
        };

        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            String imageUrl = categoryImages[i];
            
            ItemCategoryBinding catBinding = ItemCategoryBinding.inflate(LayoutInflater.from(this), binding.categoryLayout, false);
            catBinding.categoryNameTextView.setText(category);
            
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(catBinding.categoryImageView);

            catBinding.getRoot().setOnClickListener(v -> {
                selectedCategory = category;
                filterProducts();
                updateCategoryUI();
            });
            binding.categoryLayout.addView(catBinding.getRoot());
        }
    }

    private void updateCategoryUI() {
        for (int i = 0; i < binding.categoryLayout.getChildCount(); i++) {
            View view = binding.categoryLayout.getChildAt(i);
            ItemCategoryBinding catBinding = ItemCategoryBinding.bind(view);
            if (catBinding.categoryNameTextView.getText().toString().equals(selectedCategory)) {
                catBinding.categoryCard.setStrokeColor(getResources().getColor(com.google.android.material.R.color.material_dynamic_primary0));
                catBinding.categoryCard.setStrokeWidth(4);
            } else {
                catBinding.categoryCard.setStrokeWidth(0);
            }
        }
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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
                binding.locationTextView.setText("Delivering to: " + fullLocation);
                
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            }
        }
    }

    private void loadCustomerInfo() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    binding.customerWelcomeTextView.setText("Namaste, " + user.getFullName());
                    binding.locationTextView.setText("Delivering to: " + user.getAddress());

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

    private void loadFarms() {
        db.collection("Users").whereEqualTo("role", "Farmer").limit(10).get().addOnSuccessListener(queryDocumentSnapshots -> {
            farmList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                User farm = document.toObject(User.class);
                farmList.add(farm);
            }
            farmAdapter.notifyDataSetChanged();
        });
    }

    private void loadProducts() {
        db.collection("Products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            productList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Product product = document.toObject(Product.class);
                productList.add(product);
            }
            filterProducts();
        });
    }

    private void filterProducts() {
        String query = binding.searchEditText.getText().toString().toLowerCase().trim();
        filteredProductList.clear();
        for (Product product : productList) {
            String productCategory = product.getCategory() != null ? product.getCategory() : "";
            boolean matchesCategory = selectedCategory.equals("All") || productCategory.equalsIgnoreCase(selectedCategory);
            boolean matchesSearch = product.getName().toLowerCase().contains(query);
            if (matchesCategory && matchesSearch) {
                filteredProductList.add(product);
            }
        }
        productAdapter.notifyDataSetChanged();
    }
}
