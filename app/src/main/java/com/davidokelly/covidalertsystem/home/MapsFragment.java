package com.davidokelly.covidalertsystem.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davidokelly.covidalertsystem.R;
import com.davidokelly.covidalertsystem.data.Geofence.GeofenceHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MapsFragment extends Fragment {
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private GoogleMap map;
    private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10000;
    public static final String TAG = "MapsFragment";
    private FirebaseAuth fAuth;
    private FirebaseFirestore database;
    private FirebaseUser firebaseUser;
    private double lat = 0, lng = 0;
    private final float GEOFENCE_RADIUS = 50;
    private String userID;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {


        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {


            map = googleMap;
            Log.d(TAG, "onMapReady: Used");

            geofencingClient = LocationServices.getGeofencingClient(getContext());
            geofenceHelper = new GeofenceHelper(getContext());
            fAuth = FirebaseAuth.getInstance();
            database = FirebaseFirestore.getInstance();
            firebaseUser = fAuth.getCurrentUser();
            userID = firebaseUser.getUid();

            DocumentReference userDocument = database.collection("Users").document(userID);
            userDocument.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userDocument.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document = task1.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "Lat and Long accessed for: " + userID);
                                lat = Double.parseDouble(document.get("latitude").toString());
                                lng = Double.parseDouble(document.get("longitude").toString());
                                Log.d(TAG, "Lat: " + lat + " Lng:" + lng);
                                LatLng home = new LatLng(lat, lng);
                                addGeofence(home);
                                map.addMarker(new MarkerOptions().position(home).title("Home Address"));
                                map.addCircle(Circle(home));
                                map.moveCamera(CameraUpdateFactory.newLatLng(home));
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task1.getException());
                        }
                    });
                } else {
                    Log.d(TAG, "Error: " + Objects.requireNonNull(task.getException()).getMessage());
                }
            });
            map.getUiSettings().setAllGesturesEnabled(true);
            enableUserLocation();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }


    private void enableUserLocation() {
        Log.d(TAG, "enableUserLocation: Called");
        if (ContextCompat.checkSelfPermission((getActivity()), Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission((getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            Log.d(TAG, "enableUserLocation: Permission Already Given");
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "enableUserLocation: Asked");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission
                            .ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission
                        .ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);

            }
        }
    }

    private CircleOptions Circle(LatLng latLng) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(GEOFENCE_RADIUS);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        return circleOptions;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE || requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                Log.d(TAG, "onRequestPermissionResult: Granted");
                map.setMyLocationEnabled(true);
                Log.d(TAG, "map: setMyLocationEnabled");
            } else {
                //no permission
                Log.d(TAG, "onRequestPermissionResult: Not Granted");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng latLng) {
        Geofence geofence = geofenceHelper.getGeofence(userID, latLng, GEOFENCE_RADIUS, Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onSuccess: Geofence Added...");
                }).addOnFailureListener(e -> {
            String errorMessage = geofenceHelper.getErrorString(e);
            Log.d(TAG, "onFailure: " + errorMessage);
        });

    }
}