package com.kisansetu.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.kisansetu.app.databinding.ActivitySmartToolsBinding;

import java.util.Random;

public class SmartToolsActivity extends AppCompatActivity {

    private ActivitySmartToolsBinding binding;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.diseaseImageView.setImageURI(imageUri);
                    simulateDiseaseDetection();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySmartToolsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.predictPriceButton.setOnClickListener(v -> predictPrice());
        
        binding.uploadCropButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void predictPrice() {
        String cropName = binding.cropNameEditText.getText().toString().trim();
        String location = binding.locationEditText.getText().toString().trim();

        if (TextUtils.isEmpty(cropName)) {
            Toast.makeText(this, "Enter crop name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulate AI logic
        Random random = new Random();
        int suggestedPrice = 20 + random.nextInt(60);
        int localMandiPrice = suggestedPrice - (5 + random.nextInt(10));

        binding.predictionResultTextView.setVisibility(View.VISIBLE);
        binding.predictionResultTextView.setText("Suggested selling price: ₹" + suggestedPrice + "/kg");
        
        binding.mandiComparisonCard.setVisibility(View.VISIBLE);
        binding.localMandiPriceTextView.setText("₹" + localMandiPrice + "/kg");
        binding.suggestedPriceTextView.setText("₹" + suggestedPrice + "/kg");

        Toast.makeText(this, "AI Analysis Complete", Toast.LENGTH_SHORT).show();
    }

    private void simulateDiseaseDetection() {
        binding.diseaseResultLayout.setVisibility(View.VISIBLE);
        
        String[] diseases = {"Leaf Rust", "Powdery Mildew", "Bacterial Blight", "Root Rot"};
        String[] descriptions = {
            "Symptoms: Small orange spots on leaves. Treatment: Use fungicides and increase ventilation.",
            "Symptoms: White powdery coating on stems and leaves. Treatment: Apply sulfur-based sprays.",
            "Symptoms: Water-soaked spots that turn brown. Treatment: Remove infected plants, use copper sprays.",
            "Symptoms: Yellowing leaves and wilting. Treatment: Improve drainage, reduce watering."
        };

        int index = new Random().nextInt(diseases.length);
        binding.diseaseNameTextView.setText(diseases[index]);
        binding.diseaseDescriptionTextView.setText(descriptions[index]);

        Toast.makeText(this, "AI Scan Complete: " + diseases[index], Toast.LENGTH_LONG).show();
    }
}