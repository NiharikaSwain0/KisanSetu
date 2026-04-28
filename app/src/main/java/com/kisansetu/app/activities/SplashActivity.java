package com.kisansetu.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kisansetu.app.R;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                checkUserRole(currentUser.getUid());
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, 2000);
    }

    private void checkUserRole(String uid) {
        db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                Intent intent;
                if ("Farmer".equals(role)) {
                    intent = new Intent(SplashActivity.this, FarmerDashboardActivity.class);
                } else if ("Customer".equals(role)) {
                    intent = new Intent(SplashActivity.this, CustomerDashboardActivity.class);
                } else if ("Rider".equals(role)) {
                    intent = new Intent(SplashActivity.this, RiderDashboardActivity.class);
                } else if ("Admin".equals(role)) {
                    intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }).addOnFailureListener(e -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        });
    }
}
