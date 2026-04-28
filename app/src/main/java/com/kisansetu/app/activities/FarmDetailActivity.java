package com.kisansetu.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kisansetu.app.adapters.ProductAdapter;
import com.kisansetu.app.databinding.ActivityFarmDetailBinding;
import com.kisansetu.app.models.Product;
import java.util.ArrayList;
import java.util.List;

public class FarmDetailActivity extends AppCompatActivity {

    private ActivityFarmDetailBinding binding;
    private FirebaseFirestore db;
    private List<Product> productList;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFarmDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();

        String farmerId = getIntent().getStringExtra("farmerId");
        String farmName = getIntent().getStringExtra("farmName");
        
        if (farmName != null) {
            binding.farmNameTitle.setText(farmName);
        }

        adapter = new ProductAdapter(productList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onDeleteClick(Product product) {}

            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(FarmDetailActivity.this, ProductDetailActivity.class);
                intent.putExtra("productId", product.getId());
                startActivity(intent);
            }
        });

        binding.farmProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.farmProductsRecyclerView.setAdapter(adapter);

        loadFarmerProducts(farmerId);
    }

    private void loadFarmerProducts(String farmerId) {
        if (farmerId == null) return;
        
        db.collection("Products")
                .whereEqualTo("farmerId", farmerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        productList.add(product);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
