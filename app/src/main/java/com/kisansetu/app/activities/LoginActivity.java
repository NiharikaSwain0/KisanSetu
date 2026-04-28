package com.kisansetu.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kisansetu.app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.loginButton.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    checkUserRole(mAuth.getCurrentUser().getUid());
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.signupTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void checkUserRole(String uid) {
        db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            binding.progressBar.setVisibility(View.GONE);
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                Intent intent;
                if ("Farmer".equals(role)) {
                    intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
                } else if ("Customer".equals(role)) {
                    intent = new Intent(LoginActivity.this, CustomerDashboardActivity.class);
                } else if ("Rider".equals(role)) {
                    intent = new Intent(LoginActivity.this, RiderDashboardActivity.class);
                } else if ("Admin".equals(role)) {
                    intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                } else {
                    Toast.makeText(LoginActivity.this, "Unknown Role", Toast.LENGTH_SHORT).show();
                    binding.loginButton.setEnabled(true);
                    return;
                }
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                binding.loginButton.setEnabled(true);
            }
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.loginButton.setEnabled(true);
            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
