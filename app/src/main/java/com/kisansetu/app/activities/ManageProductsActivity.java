package com.kisansetu.app.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kisansetu.app.adapters.ProductAdapter;
import com.kisansetu.app.databinding.ActivityManageProductsBinding;
import com.kisansetu.app.models.Product;
import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends AppCompatActivity {

    private ActivityManageProductsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Product> productList;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        productList = new ArrayList<>();

        adapter = new ProductAdapter(productList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onDeleteClick(Product product) {
                deleteProduct(product);
            }

            @Override
            public void onItemClick(Product product) {
                // Future implementation: Edit product
            }
        });

        binding.productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.productsRecyclerView.setAdapter(adapter);

        loadProducts();
    }

    private void loadProducts() {
        db.collection("Products")
                .whereEqualTo("farmerId", mAuth.getUid())
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

    private void deleteProduct(Product product) {
        db.collection("Products").document(product.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                    loadProducts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting product", Toast.LENGTH_SHORT).show();
                });
    }
}
