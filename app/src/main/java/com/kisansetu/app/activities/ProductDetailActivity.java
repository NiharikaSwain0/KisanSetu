package com.kisansetu.app.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kisansetu.app.databinding.ActivityProductDetailBinding;
import com.kisansetu.app.models.CartItem;
import com.kisansetu.app.models.Product;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Product product;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        String productId = getIntent().getStringExtra("productId");
        loadProductDetails(productId);

        binding.plusButton.setOnClickListener(v -> {
            quantity++;
            binding.quantityTextView.setText(String.valueOf(quantity));
        });

        binding.minusButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.quantityTextView.setText(String.valueOf(quantity));
            }
        });

        binding.addToCartButton.setOnClickListener(v -> {
            addToCart();
        });
    }

    private void loadProductDetails(String productId) {
        db.collection("Products").document(productId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                product = documentSnapshot.toObject(Product.class);
                binding.productNameTextView.setText(product.getName());
                binding.productPriceTextView.setText("₹ " + product.getPrice());
                binding.productQuantityTextView.setText("Quantity: " + product.getQuantity());
                binding.productDescriptionTextView.setText(product.getDescription());

                Glide.with(this)
                        .load(product.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.productImageView);
            }
        });
    }

    private void addToCart() {
        if (product == null) return;

        CartItem cartItem = new CartItem(product.getId(), product.getName(), product.getPrice(), quantity, product.getFarmerId());
        db.collection("Cart").document(mAuth.getUid()).collection("Items").document(product.getId())
                .set(cartItem).addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProductDetailActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
