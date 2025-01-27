package com.apex.sunrisesunsetapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient locationClient;
    private TextView sunriseText, sunsetText;
    private MaterialButton refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        sunriseText = findViewById(R.id.sunriseText);
        sunsetText = findViewById(R.id.sunsetText);
        refreshButton = findViewById(R.id.refreshButton);

        // Initialize location client
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            // Permission granted, fetch location and data
            fetchLocationAndData();
        }

        // Set up refresh button to fetch data when clicked
        refreshButton.setOnClickListener(v -> fetchLocationAndData());
    }

    private void fetchLocationAndData() {
        // Check permission before getting location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        locationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Use the cached location
                fetchSunriseSunsetData(task.getResult());
            } else {
                // Log and handle errors
                Log.e("Havan", "Failed to get cached location. Error: " + (task.getException() != null ? task.getException().getMessage() : "No exception"));
                fetchLiveLocation(); // Fallback to live location
            }
        });
    }

    private void fetchLiveLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    fetchSunriseSunsetData(location);
                    locationClient.removeLocationUpdates(this); // Stop updates after location is retrieved
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Unable to get live location", Toast.LENGTH_SHORT).show());
                }
            }
        }, getMainLooper());
    }

    private void fetchSunriseSunsetData(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Log.i("Havan", "Latitude: " + latitude + ", Longitude: " + longitude);

        // Build API URL with latitude and longitude
//        String url = "https://api.sunrisesunset.io/json?lat=" + latitude + "&lng=" + longitude + "&timezone=UTC";
        String url = "https://api.sunrisesunset.io/json?lat=" + latitude + "&lng=" + longitude;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Make API call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Parse the JSON response
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject results = jsonObject.getJSONObject("results");

                        String sunrise = results.getString("sunrise");
                        String sunset = results.getString("sunset");

                        runOnUiThread(() -> {
                            // Update the UI with sunrise and sunset times
                            sunriseText.setText("Sunrise: " + sunrise);
                            sunsetText.setText("Sunset: " + sunset);
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch location and data
                fetchLocationAndData();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Permission Denied. Please enable location permissions.", Toast.LENGTH_SHORT).show();

                // Optionally, redirect user to location settings
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }
    }
}
