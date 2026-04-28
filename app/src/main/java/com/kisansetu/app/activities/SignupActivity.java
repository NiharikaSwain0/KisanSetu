package com.kisansetu.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kisansetu.app.databinding.ActivitySignupBinding;
import com.kisansetu.app.models.User;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.profileImageView.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        binding.profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = parent.getItemAtPosition(position).toString();
                if ("Farmer".equals(selectedRole)) {
                    binding.farmNameInputLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.farmNameInputLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.signupButton.setOnClickListener(v -> {
            String name = binding.nameEditText.getText().toString().trim();
            String email = binding.emailEditText.getText().toString().trim();
            String phone = binding.phoneEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String address = binding.addressEditText.getText().toString().trim();
            String role = binding.roleSpinner.getSelectedItem().toString();
            String farmName = binding.farmNameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(password) || TextUtils.isEmpty(address)) {
                Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("Farmer".equals(role) && TextUtils.isEmpty(farmName)) {
                Toast.makeText(SignupActivity.this, "Please enter your Farm Name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(SignupActivity.this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignupActivity.this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.signupButton.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();
                    if (imageUri != null) {
                        uploadImageAndSaveUser(uid, name, email, phone, role, address, farmName);
                    } else {
                        User user = new User(uid, name, email, phone, role, address, "Farmer".equals(role) ? farmName : null);
                        saveUserToFirestore(user);
                    }
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.signupButton.setEnabled(true);
                    Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });
    }

    private void uploadImageAndSaveUser(String uid, String name, String email, String phone, String role, String address, String farmName) {
        StorageReference profilePicRef = storageRef.child("profile_pics/" + uid + ".jpg");
        profilePicRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                User user = new User(uid, name, email, phone, role, address, "Farmer".equals(role) ? farmName : null);
                user.setProfileImage(downloadUrl);
                saveUserToFirestore(user);
            });
        }).addOnFailureListener(e -> {
            // Even if upload fails, we still save the user without an image
            User user = new User(uid, name, email, phone, role, address, "Farmer".equals(role) ? farmName : null);
            saveUserToFirestore(user);
            Toast.makeText(SignupActivity.this, "Image upload failed, profile created without photo.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserToFirestore(User user) {
        db.collection("Users").document(user.getUid()).set(user).addOnSuccessListener(aVoid -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(SignupActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
            Intent intent;
            if ("Farmer".equals(user.getRole())) {
                intent = new Intent(SignupActivity.this, FarmerDashboardActivity.class);
            } else if ("Customer".equals(user.getRole())) {
                intent = new Intent(SignupActivity.this, CustomerDashboardActivity.class);
            } else if ("Rider".equals(user.getRole())) {
                intent = new Intent(SignupActivity.this, RiderDashboardActivity.class);
            } else if ("Admin".equals(user.getRole())) {
                intent = new Intent(SignupActivity.this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(SignupActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.signupButton.setEnabled(true);
            Toast.makeText(SignupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
