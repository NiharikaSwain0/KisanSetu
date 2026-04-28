package com.kisansetu.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kisansetu.app.R;
import com.kisansetu.app.databinding.ActivityProfileBinding;
import com.kisansetu.app.models.User;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private User currentUser;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.profileImageView.setImageURI(imageUri);
                    uploadImage();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        loadProfile();

        binding.profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.updateProfileButton.setOnClickListener(v -> {
            updateProfile();
        });

        binding.logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void uploadImage() {
        if (imageUri == null) return;

        String uid = mAuth.getUid();
        if (uid == null) return;

        StorageReference profilePicRef = storageRef.child("profile_pics/" + uid + ".jpg");

        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        profilePicRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                db.collection("Users").document(uid).update("profileImage", downloadUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                        });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProfile() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentUser = documentSnapshot.toObject(User.class);
                if (currentUser != null) {
                    binding.fullNameEditText.setText(currentUser.getFullName());
                    binding.phoneEditText.setText(currentUser.getPhone());
                    binding.addressEditText.setText(currentUser.getAddress());

                    if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                        Glide.with(this)
                                .load(currentUser.getProfileImage())
                                .placeholder(android.R.drawable.ic_menu_camera)
                                .into(binding.profileImageView);
                    }
                    
                    if ("Farmer".equals(currentUser.getRole())) {
                        binding.profileTitleTextView.setText("Farmer Profile");
                        binding.farmNameEditText.setText(currentUser.getFarmName());
                        binding.farmNameEditText.setVisibility(View.VISIBLE);
                        binding.addressEditText.setHint("Farm Address / Location");
                        binding.vehicleTypeLayout.setVisibility(View.GONE);
                        binding.vehicleNumberLayout.setVisibility(View.GONE);
                    } else if ("Rider".equals(currentUser.getRole())) {
                        binding.profileTitleTextView.setText("Rider's Profile");
                        binding.farmNameEditText.setVisibility(View.GONE);
                        binding.addressEditText.setHint("Home Address / Base Location");
                        binding.vehicleTypeLayout.setVisibility(View.VISIBLE);
                        binding.vehicleNumberLayout.setVisibility(View.VISIBLE);
                        binding.vehicleTypeEditText.setText(currentUser.getVehicleType());
                        binding.vehicleNumberEditText.setText(currentUser.getVehicleNumber());
                    } else {
                        binding.profileTitleTextView.setText("Customer Profile");
                        binding.farmNameEditText.setVisibility(View.GONE);
                        binding.addressEditText.setHint("Delivery Address");
                        binding.vehicleTypeLayout.setVisibility(View.GONE);
                        binding.vehicleNumberLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void updateProfile() {
        String name = binding.fullNameEditText.getText().toString().trim();
        String farmName = binding.farmNameEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        String address = binding.addressEditText.getText().toString().trim();
        String vehicleType = binding.vehicleTypeEditText.getText().toString().trim();
        String vehicleNumber = binding.vehicleNumberEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getUid();
        if (uid == null) return;

        if ("Farmer".equals(currentUser.getRole())) {
            db.collection("Users").document(uid)
                    .update("fullName", name, "farmName", farmName, "phone", phone, "address", address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else if ("Rider".equals(currentUser.getRole())) {
            db.collection("Users").document(uid)
                    .update("fullName", name, "phone", phone, "address", address, "vehicleType", vehicleType, "vehicleNumber", vehicleNumber)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            db.collection("Users").document(uid)
                    .update("fullName", name, "phone", phone, "address", address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
}
