package com.kisansetu.app.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kisansetu.app.R;
import com.kisansetu.app.adapters.AddressAdapter;
import com.kisansetu.app.adapters.CartAdapter;
import com.kisansetu.app.databinding.ActivityCartBinding;
import com.kisansetu.app.models.AddressModel;
import com.kisansetu.app.models.CartItem;
import com.kisansetu.app.models.Order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<CartItem> cartItemList;
    private CartAdapter adapter;
    private List<AddressModel> addressList;
    private AddressAdapter addressAdapter;
    private double totalAmount = 0;
    private double discount = 0;
    private String appliedCoupon = ""; // "FIRST20" or "CARTDEAL50"
    private FusedLocationProviderClient fusedLocationClient;
    private static final int UPI_PAYMENT_REQUEST_CODE = 123;
    private static final int MAP_PICKER_REQUEST_CODE = 456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        cartItemList = new ArrayList<>();
        addressList = new ArrayList<>();

        setupCartList();
        setupAddressList();

        binding.paymentRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.radioOnline.getId()) {
                binding.upiAppsLayout.setVisibility(View.VISIBLE);
                loadUpiIcons();
            } else {
                binding.upiAppsLayout.setVisibility(View.GONE);
            }
        });

        binding.applyFirstOrderBtn.setOnClickListener(v -> {
            if (appliedCoupon.equals("FIRST20")) {
                removeCoupon();
            } else {
                applyFirstOrderCoupon();
            }
        });

        binding.applyCartDealBtn.setOnClickListener(v -> {
            if (appliedCoupon.equals("CARTDEAL50")) {
                removeCoupon();
            } else {
                applyCartDealCoupon();
            }
        });

        binding.addNewAddressBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, MapPickerActivity.class);
            startActivityForResult(intent, MAP_PICKER_REQUEST_CODE);
        });

        binding.checkoutButton.setOnClickListener(v -> {
            if (addressAdapter.getSelectedAddress() == null) {
                Toast.makeText(this, "Please select or add a delivery address", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (binding.radioOnline.isChecked()) {
                startUpiPayment();
            } else {
                placeOrder("COD");
            }
        });

        loadCartItems();
        loadSavedAddresses();
    }

    private void setupCartList() {
        adapter = new CartAdapter(cartItemList, new CartAdapter.OnCartItemClickListener() {
            @Override
            public void onDeleteClick(CartItem cartItem) {
                deleteCartItem(cartItem);
            }
        });
        binding.cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.cartRecyclerView.setAdapter(adapter);
    }

    private void setupAddressList() {
        addressAdapter = new AddressAdapter(addressList, address -> {
            // Address selected
        });
        binding.addressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.addressRecyclerView.setAdapter(addressAdapter);
    }

    private void applyFirstOrderCoupon() {
        if (!appliedCoupon.isEmpty()) {
            removeCoupon();
        }
        
        appliedCoupon = "FIRST20";
        discount = totalAmount * 0.20; // 20% Discount
        
        binding.applyFirstOrderBtn.setText("REMOVE");
        binding.discountLayout.setVisibility(View.VISIBLE);
        binding.discountTextView.setText("- ₹ " + String.format("%.2f", discount));
        updateBillSummary();
        Toast.makeText(this, "FIRST20 Applied! You saved 20%", Toast.LENGTH_SHORT).show();
    }

    private void applyCartDealCoupon() {
        if (totalAmount < 200) {
            Toast.makeText(this, "Add ₹" + (200 - totalAmount) + " more to use CARTDEAL50", Toast.LENGTH_LONG).show();
            return;
        }

        if (!appliedCoupon.isEmpty()) {
            removeCoupon();
        }

        appliedCoupon = "CARTDEAL50";
        discount = 50; // Flat ₹50 Discount
        
        binding.applyCartDealBtn.setText("REMOVE");
        binding.discountLayout.setVisibility(View.VISIBLE);
        binding.discountTextView.setText("- ₹ " + discount);
        updateBillSummary();
        Toast.makeText(this, "CARTDEAL50 Applied! You saved ₹50", Toast.LENGTH_SHORT).show();
    }

    private void removeCoupon() {
        binding.applyFirstOrderBtn.setText("APPLY");
        binding.applyCartDealBtn.setText("APPLY");
        
        appliedCoupon = "";
        discount = 0;
        binding.discountLayout.setVisibility(View.GONE);
        updateBillSummary();
    }

    private void showAddAddressDialog(String fullAddress, double lat, double lon) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        android.widget.TextView detectedLocText = dialogView.findViewById(R.id.detectedLocationText);
        TextInputEditText houseNoEt = dialogView.findViewById(R.id.houseNoEditText);
        TextInputEditText landmarkEt = dialogView.findViewById(R.id.landmarkEditText);
        TextInputEditText nameEt = dialogView.findViewById(R.id.receiverNameEditText);
        TextInputEditText phoneEt = dialogView.findViewById(R.id.receiverPhoneEditText);
        ChipGroup labelGroup = dialogView.findViewById(R.id.labelChipGroup);
        android.widget.Button saveBtn = dialogView.findViewById(R.id.saveAddressButton);

        detectedLocText.setText(fullAddress);

        saveBtn.setOnClickListener(v -> {
            String house = houseNoEt.getText().toString().trim();
            String name = nameEt.getText().toString().trim();
            String phone = phoneEt.getText().toString().trim();
            
            int checkedChipId = labelGroup.getCheckedChipId();
            String label = "Other";
            if (checkedChipId != View.NO_ID) {
                label = ((Chip) dialogView.findViewById(checkedChipId)).getText().toString();
            }

            if (house.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = UUID.randomUUID().toString();
            AddressModel newAddress = new AddressModel(id, label, fullAddress, house, landmarkEt.getText().toString(), name, phone, lat, lon);
            
            db.collection("Users").document(mAuth.getUid()).collection("Addresses").document(id).set(newAddress)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Address Saved", Toast.LENGTH_SHORT).show();
                        loadSavedAddresses();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void loadSavedAddresses() {
        db.collection("Users").document(mAuth.getUid()).collection("Addresses").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    addressList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        addressList.add(doc.toObject(AddressModel.class));
                    }
                    addressAdapter.notifyDataSetChanged();
                });
    }

    private void loadUpiIcons() {
        String phonePe = "https://w7.pngwing.com/pngs/462/313/png-transparent-phonepe-hd-logo-thumbnail.png";
        String gPay = "https://w7.pngwing.com/pngs/303/52/png-transparent-google-pay-hd-logo-thumbnail.png";
        String bhim = "https://w7.pngwing.com/pngs/129/615/png-transparent-bhim-hd-logo-thumbnail.png";

        Glide.with(this).load(phonePe).into((android.widget.ImageView) binding.upiAppsLayout.getChildAt(0));
        Glide.with(this).load(gPay).into((android.widget.ImageView) binding.upiAppsLayout.getChildAt(1));
        Glide.with(this).load(bhim).into((android.widget.ImageView) binding.upiAppsLayout.getChildAt(2));
    }

    private void startUpiPayment() {
        if (totalAmount - discount <= 0) return;

        String payeeVpa = "kisansetu@upi"; 
        String payeeName = "KisanSetu Admin";
        String amount = String.valueOf(totalAmount - discount);

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", payeeVpa)
                .appendQueryParameter("pn", payeeName)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        if (null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPI_PAYMENT_REQUEST_CODE) {
            // Simplified for hackathon demo
            placeOrder("Online");
        } else if (requestCode == MAP_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("address");
            double lat = data.getDoubleExtra("lat", 0);
            double lon = data.getDoubleExtra("lon", 0);
            showAddAddressDialog(address, lat, lon);
        }
    }

    private void loadCartItems() {
        db.collection("Cart").document(mAuth.getUid()).collection("Items").get().addOnSuccessListener(queryDocumentSnapshots -> {
            cartItemList.clear();
            totalAmount = 0;
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                CartItem item = document.toObject(CartItem.class);
                cartItemList.add(item);
                totalAmount += item.getPrice() * item.getQuantity();
            }
            adapter.notifyDataSetChanged();
            updateBillSummary();
        });
    }

    private void updateBillSummary() {
        binding.itemTotalTextView.setText("₹ " + totalAmount);
        binding.grandTotalTextView.setText("₹ " + (totalAmount - discount));
        binding.totalAmountTextView.setText("₹ " + (totalAmount - discount));
    }

    private void deleteCartItem(CartItem cartItem) {
        db.collection("Cart").document(mAuth.getUid()).collection("Items").document(cartItem.getProductId()).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
            loadCartItems();
        });
    }

    private void placeOrder(String paymentMode) {
        if (cartItemList.isEmpty()) return;
        
        AddressModel selectedAddress = addressAdapter.getSelectedAddress();
        if (selectedAddress == null) return;

        String orderId = UUID.randomUUID().toString();
        String farmerId = cartItemList.get(0).getFarmerId();
        
        Order order = new Order(orderId, mAuth.getUid(), new ArrayList<>(cartItemList), totalAmount - discount, "Pending", farmerId, selectedAddress.getFullAddress(), paymentMode);

        db.collection("Orders").document(orderId).set(order).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
            clearCart();
            finish();
        });
    }

    private void clearCart() {
        for (CartItem item : cartItemList) {
            db.collection("Cart").document(mAuth.getUid()).collection("Items").document(item.getProductId()).delete();
        }
    }
}
