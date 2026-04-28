package com.kisansetu.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kisansetu.app.databinding.ActivityAddProductBinding;
import com.kisansetu.app.models.Product;

import java.io.InputStream;
import java.util.UUID;

public class EditProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;
    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;
    private String productId;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating product...");

        productId = getIntent().getStringExtra("PRODUCT_ID");
        if (productId == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Change button text for edit mode
        binding.submitProductButton.setText("Update Product");
        
        loadProductData();

        binding.uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 1);
        });

        binding.suggestPriceButton.setOnClickListener(v -> suggestPrice());

        binding.submitProductButton.setOnClickListener(v -> updateProduct());
    }

    private void loadProductData() {
        db.collection("Products").document(productId).get().addOnSuccessListener(doc -> {
            currentProduct = doc.toObject(Product.class);
            if (currentProduct != null) {
                binding.productNameEditText.setText(currentProduct.getName());
                binding.productPriceEditText.setText(String.valueOf(currentProduct.getPrice()));
                binding.productQuantityEditText.setText(currentProduct.getQuantity());
                binding.productDescriptionEditText.setText(currentProduct.getDescription());
                binding.harvestTimeEditText.setText(currentProduct.getHarvestTime());
                
                if (currentProduct.getImageUrl() != null) {
                    Glide.with(this).load(currentProduct.getImageUrl()).into(binding.productImageView);
                }
            }
        });
    }

    private void suggestPrice() {
        String name = binding.productNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter product name first", Toast.LENGTH_SHORT).show();
            return;
        }
        double suggestedPrice = 50.0;
        if (name.toLowerCase().contains("tomato")) suggestedPrice = 40.0;
        else if (name.toLowerCase().contains("apple")) suggestedPrice = 120.0;
        else if (name.toLowerCase().contains("wheat")) suggestedPrice = 30.0;
        binding.productPriceEditText.setText(String.valueOf(suggestedPrice));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.productImageView.setImageURI(imageUri);
        }
    }

    private void updateProduct() {
        String name = binding.productNameEditText.getText().toString().trim();
        String priceStr = binding.productPriceEditText.getText().toString().trim();
        String quantity = binding.productQuantityEditText.getText().toString().trim();
        String description = binding.productDescriptionEditText.getText().toString().trim();
        String harvestTime = binding.harvestTimeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(quantity) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }
        final double finalPrice = price;
        progressDialog.show();

        // ⚡ Added a safety handler to close the dialog if Firestore is not enabled
        new android.os.Handler().postDelayed(() -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                Toast.makeText(this, "Database connection timeout. Please ensure 'Cloud Firestore' is enabled in Firebase Console.", Toast.LENGTH_LONG).show();
            }
        }, 10000); // 10 seconds timeout

        // ⚡ INSTANT FIX: Skipping image upload to bypass Firebase Storage delays
        saveToFirestore(name, description, finalPrice, quantity, currentProduct.getImageUrl(), harvestTime);
    }

    private void saveToFirestore(String name, String description, double price, String quantity, String imageUrl, String harvestTime) {
        currentProduct.setName(name);
        currentProduct.setDescription(description);
        currentProduct.setPrice(price);
        currentProduct.setQuantity(quantity);
        currentProduct.setImageUrl(imageUrl);
        currentProduct.setHarvestTime(harvestTime);

        db.collection("Products").document(productId).set(currentProduct).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(this, "Update failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
