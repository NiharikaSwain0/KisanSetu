package com.kisansetu.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.kisansetu.app.databinding.ActivityMapPickerBinding;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity {

    private ActivityMapPickerBinding binding;
    private MapView mapView;
    private IMapController mapController;
    private LocationManager locationManager;

    private String selectedAddress = "";
    private double selectedLat = 0.0;
    private double selectedLon = 0.0;

    private static final int LOCATION_PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        setupMap();
        setupClickListeners();
        setupMapListener();
        loadCurrentLocation();
    }

    private void setupMap() {
        mapView = binding.mapView;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        mapController = mapView.getController();
        mapController.setZoom(17.0);
    }

    private void setupClickListeners() {

        binding.backBtn.setOnClickListener(v -> finish());

        binding.recenterBtn.setOnClickListener(v -> loadCurrentLocation());

        binding.confirmLocationBtn.setOnClickListener(v -> {

            if (selectedAddress.isEmpty()) {
                Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("address", selectedAddress);
            intent.putExtra("lat", selectedLat);
            intent.putExtra("lon", selectedLon);

            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void setupMapListener() {

        mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                handleMapInteraction();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                handleMapInteraction();
                return true;
            }
        });
    }

    private void handleMapInteraction() {

        IGeoPoint center = mapView.getMapCenter();

        if (center != null) {
            selectedLat = center.getLatitude();
            selectedLon = center.getLongitude();

            fetchAddressFromCoords(selectedLat, selectedLon);
        }
    }

    private void loadCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {

            GeoPoint point = new GeoPoint(
                    location.getLatitude(),
                    location.getLongitude()
            );

            mapController.setCenter(point);

            fetchAddressFromCoords(
                    location.getLatitude(),
                    location.getLongitude()
            );

        } else {
            setDelhiAsDefault();
        }
    }

    private void setDelhiAsDefault() {

        GeoPoint point = new GeoPoint(28.6139, 77.2090);

        mapController.setCenter(point);

        fetchAddressFromCoords(28.6139, 77.2090);
    }

    private void fetchAddressFromCoords(double lat, double lon) {

        binding.currentAddressText.setText("Locating...");

        new Thread(() -> {
            try {

                Geocoder geocoder =
                        new Geocoder(MapPickerActivity.this, Locale.getDefault());

                List<Address> list =
                        geocoder.getFromLocation(lat, lon, 1);

                if (list != null && !list.isEmpty()) {

                    String address = list.get(0).getAddressLine(0);

                    runOnUiThread(() -> {
                        selectedAddress = address;
                        selectedLat = lat;
                        selectedLon = lon;

                        binding.currentAddressText.setText(address);
                    });

                } else {

                    runOnUiThread(() ->
                            binding.currentAddressText.setText(
                                    lat + ", " + lon
                            ));
                }

            } catch (Exception e) {

                runOnUiThread(() ->
                        binding.currentAddressText.setText(
                                lat + ", " + lon
                        ));
            }

        }).start();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            loadCurrentLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mapView != null) {
            mapView.onDetach();
        }
    }
}