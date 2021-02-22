package com.davidokelly.covidalertsystem.ui.account;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.davidokelly.covidalertsystem.R;
import com.davidokelly.covidalertsystem.home.MapsFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import static android.content.Context.LOCATION_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class ChangeAddressMainFragment extends Fragment {
    private static final String TAG = "ChangeAddressMainFrag";
    private Button buttonUpdate, buttonSetCurrent, buttonSave, buttonMoveToAddress;
    private EditText editAddress;
    private ProgressBar progressBar;
    private ChangeAddressFragmentMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public ChangeAddressMainFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        buttonUpdate = (Button) getView().findViewById(R.id.button_update);
        progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
        buttonSetCurrent = (Button) getView().findViewById(R.id.button_set_current);
        buttonSave = (Button) getView().findViewById(R.id.button_save);
        buttonMoveToAddress = (Button) getView().findViewById(R.id.button_upload);
        editAddress = (EditText) getView().findViewById(R.id.editTextAdddress);

        map = new ChangeAddressFragmentMap();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, map)
                .addToBackStack("ChangeAddressMap")
                .commit();

        buttonUpdate.setOnClickListener(v -> {
            String address = map.getHomeAddress();
            editAddress.setText(address);
        });

        buttonMoveToAddress.setOnClickListener(v -> {
            String address = editAddress.getText().toString();
            map.setHomeAddress(address);
        });

        buttonSetCurrent.setOnClickListener(v -> fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                map.setHomeLatLng(latLng);
            } else {
                Toast.makeText(getActivity(), "Location Is Null", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Cannot find current location", Toast.LENGTH_LONG).show()));

        buttonSave.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            LatLng latLng = map.getHomeLatLng();
            GeoPoint geoPoint = new GeoPoint(latLng.latitude,latLng.longitude);
            FirebaseAuth fAuth = FirebaseAuth.getInstance();
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            String userID = fAuth.getUid();
            DocumentReference usersDocument = database.collection("Users").document(userID);
            usersDocument.update("address", map.getHomeAddress(), "home", geoPoint)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "onSuccess: address changed for " + userID);
                        Toast.makeText(getActivity(),"Address changed. Please relaunch application.",Toast.LENGTH_LONG).show();
                        getChildFragmentManager().popBackStack();
                        getActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "onFailure: " + e.toString());
                        Toast.makeText(getActivity(),"Could not update data",Toast.LENGTH_LONG).show();
                    });
            progressBar.setVisibility(View.INVISIBLE);


            //TODO save
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_address_main, container, false);
    }
}