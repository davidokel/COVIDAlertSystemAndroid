package com.davidokelly.covidalertsystem.ui.account;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.davidokelly.covidalertsystem.R;
import com.davidokelly.covidalertsystem.data.User.User;
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

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class ChangeAddressFragmentMap extends Fragment {
    private static final String TAG = "ChangeAddressFlag";
    private GoogleMap map;
    private FirebaseAuth fAuth;
    private FirebaseFirestore database;
    private FirebaseUser firebaseUser;
    private String userID;
    private double lat, lng;
    private double GEOFENCE_RADIUS = 75;
    final Locale current = Locale.ENGLISH;//getResources().getConfiguration().locale; //TODO get locale
    private LatLng homeLatLng;
    private String homeAddress;

    public ChangeAddressFragmentMap() {
    }

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
        @SuppressLint("MissingPermission")
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            Log.d(TAG, "onMapReady: Used");
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
                                lat = document.getGeoPoint("home").getLatitude();
                                lng = document.getGeoPoint("home").getLongitude();
                                Log.d(TAG, "Lat: " + lat + " Lng:" + lng);
                                LatLng home = new LatLng(lat, lng);
                                map.addMarker(new MarkerOptions().position(home).title("Current Home Address"));
                                map.addCircle(Circle(home));
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 16));
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
            map.setMyLocationEnabled(true);

            map.setOnMapLongClickListener(latLng -> {
                map.clear();
                Geocoder geocoder = new Geocoder(getActivity(), current);
                try {
                    Address address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                    homeAddress = new User().addressToString(address);
                } catch (IOException e) {
                    homeAddress = "Error finding address";
                    e.printStackTrace();
                }
                setLatLng(latLng);
            });
        }
    };

    public LatLng getHomeLatLng() {
        return homeLatLng;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {

        Geocoder geocoder = new Geocoder(getActivity(), current);
        try {
            Address address = geocoder.getFromLocationName(homeAddress, 1).get(0);
            setLatLng(new LatLng(address.getLatitude(), address.getLongitude()));
            this.homeAddress = homeAddress;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"Could not find location from address", Toast.LENGTH_LONG).show();

        }
    }

    private void setLatLng(LatLng homeLatLng) {
        map.clear();
        map.addMarker(new MarkerOptions().title(homeAddress).position(homeLatLng));
        map.addCircle(Circle(homeLatLng));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 16));
        this.homeLatLng = homeLatLng;
    }

    public void setHomeLatLng(LatLng homeLatLng) {
        setLatLng(homeLatLng);
        Geocoder geocoder = new Geocoder(getActivity(), current);
        try {
            Address address = geocoder.getFromLocation(homeLatLng.latitude, homeLatLng.longitude, 1).get(0);
            this.homeAddress = new User().addressToString(address);
        } catch (IOException e) {
            Toast.makeText(getActivity(),"Could not find address from location", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_address_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
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
}