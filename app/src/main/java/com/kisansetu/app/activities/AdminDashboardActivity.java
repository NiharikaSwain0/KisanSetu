package com.kisansetu.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.kisansetu.app.databinding.ActivityAdminDashboardBinding;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.manageUsersCard.setOnClickListener(v -> {
            // Future implementation: Manage users
        });

        binding.manageOrdersCard.setOnClickListener(v -> {
            // Future implementation: Manage orders
        });
    }
}
