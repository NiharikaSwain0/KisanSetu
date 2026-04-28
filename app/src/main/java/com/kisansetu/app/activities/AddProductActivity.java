package com.kisansetu.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kisansetu.app.databinding.ActivityAddProductBinding;
import com.kisansetu.app.models.Product;

import java.io.InputStream;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding product...");

        setupCategoryDropdown();

        // Image Selection
        binding.uploadImageButton.setOnClickListener(v -> pickImage());

        // AI Suggest Price
        binding.suggestPriceButton.setOnClickListener(v -> suggestPrice());

        // Submit Button
        binding.submitProductButton.setOnClickListener(v -> uploadProduct());
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Vegetables", "Fruits", "Fish", "Egg", "Chicken", "Mutton"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.categoryDropdown.setAdapter(adapter);
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.productImageView.setImageURI(imageUri);
        }
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

        Toast.makeText(this, "AI Suggested Price: ₹" + suggestedPrice, Toast.LENGTH_SHORT).show();
    }

    private void uploadProduct() {

        String name = binding.productNameEditText.getText().toString().trim();
        String category = binding.categoryDropdown.getText().toString().trim();
        String priceStr = binding.productPriceEditText.getText().toString().trim();
        String quantity = binding.productQuantityEditText.getText().toString().trim();
        String description = binding.productDescriptionEditText.getText().toString().trim();
        String harvestTime = binding.harvestTimeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(category) ||
                TextUtils.isEmpty(priceStr) ||
                TextUtils.isEmpty(quantity) ||
                TextUtils.isEmpty(description)) {

            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getUid();
        if (userId == null) {
            Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_SHORT).show();
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

        // ⚡ RE-ENABLED: Image upload is now active
        if (imageUri != null) {
            uploadImageAndProduct(name, category, description, finalPrice, quantity, harvestTime, userId);
        } else {
            saveProductToFirestore(name, category, description, finalPrice, quantity, "", harvestTime, userId);
        }
    }

    private void uploadImageAndProduct(String name, String category, String description, double price, String quantity, String harvestTime, String userId) {
        String imageName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("product_images/" + imageName);

        ref.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String imageUrl = task.getResult().toString();
                        saveProductToFirestore(name, category, description, price, quantity, imageUrl, harvestTime, userId);
                    } else {
                        progressDialog.dismiss();
                        String error = task.getException() != null ? task.getException().getMessage() : "Upload failed";
                        Toast.makeText(this, "Image Upload Error: " + error, Toast.LENGTH_SHORT).show();
                        // Save without image as fallback if user wants
                        saveProductToFirestore(name, category, description, price, quantity, "", harvestTime, userId);
                    }
                });
    }

    private void saveProductToFirestore(String name, String category, String description, double price, String quantity, String imageUrl, String harvestTime, String userId) {
        String id = db.collection("Products").document().getId();

        Product product = new Product(
                id,
                name,
                description,
                price,
                quantity,
                imageUrl,
                userId
        );

        product.setCategory(category);
        product.setHarvestTime(harvestTime);

        // Use addOnCompleteListener for more reliability
        db.collection("Products")
                .document(id)
                .set(product)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Firestore Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}