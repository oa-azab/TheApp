package com.aey.theapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;

    private Location startLocation;
    private Location endLocation;

    private boolean isInTrip;

    @BindView(R.id.btn_start)
    Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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

        Log.d(TAG, "[onMapReady] map is read");
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        getDeviceLocation();

    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    Log.d(TAG, "[getDeviceLocation] isTaskSuccessful: " + task.isSuccessful());
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = (Location) task.getResult();
                        Log.d(TAG, "[getDeviceLocation] location: " + mLastKnownLocation);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), 15));
                    } else {
                        Log.d(TAG, "Current location is null.");
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @OnClick(R.id.btn_start)
    public void start() {
        if (!isInTrip) {
            // start
            startBtn.setText("STOP");
            startLocation = mLastKnownLocation;
            Log.d(TAG, "[start] start location: " + startLocation);
            isInTrip = true;
        } else {
            // end
            getCurrentLocation(new LocationCallback() {
                @Override
                public void location(Location location) {
                    endLocation = location;
                    Log.d(TAG, "[End] end location: " + endLocation);
                    Log.d(TAG, "[End] distance: " + startLocation.distanceTo(endLocation));
                    startBtn.setText("START");
                    isInTrip = false;
                    showTripDetails();
                }
            });
        }
    }

    private void getCurrentLocation(final LocationCallback callback) {
        try {
            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    Log.d(TAG, "[getDeviceLocation] isTaskSuccessful: " + task.isSuccessful());
                    if (task.isSuccessful()) {
                        callback.location((Location) task.getResult());
                    } else {
                        Log.d(TAG, "Current location is null.");
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void showTripDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Trip Details");
        StringBuilder sbMessage = new StringBuilder();
        sbMessage.append("Start Location: ");
        sbMessage.append(startLocation.getLongitude() + ", " + startLocation.getLatitude());
        sbMessage.append("\n");
        sbMessage.append("End Location: ");
        sbMessage.append(endLocation.getLongitude() + ", " + endLocation.getLatitude());
        sbMessage.append("\n");
        sbMessage.append("Distance: ");
        sbMessage.append(startLocation.distanceTo(endLocation));
        builder.setMessage(sbMessage.toString());
        builder.create().show();
    }

    interface LocationCallback {
        void location(Location location);
    }
}
