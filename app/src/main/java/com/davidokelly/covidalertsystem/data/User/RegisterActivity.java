package com.davidokelly.covidalertsystem.data.User;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.davidokelly.covidalertsystem.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    public static final String TAG = "RegisterActivity";
    private FirebaseAuth fAuth;
    private FirebaseFirestore database;
    private final int LOCATION_ACCESS_REQUEST_CODE = 10001;
    private LocationManager locationManager;
    String userID;
    private double lat, lng;

    private SwitchCompat currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setBackgroundDrawable(new ColorDrawable(0xFF018786));

        final EditText nameEditText = findViewById(R.id.register_input_name);
        final EditText surnameEditText = findViewById(R.id.register_input_surname);
        final EditText emailEditText = findViewById(R.id.register_input_email);
        final EditText passwordEditText = findViewById(R.id.register_input_password);
        final EditText passwordConfirmEditText = findViewById(R.id.register_input_password_confirm);
        final EditText addressEditText = findViewById(R.id.register_input_address);
        final EditText addressEditText2 = findViewById(R.id.register_input_address2);
        final EditText addressEditText3 = findViewById(R.id.register_input_address3);
        final EditText addressEditText4 = findViewById(R.id.register_input_address4);
        final EditText addressEditText5 = findViewById(R.id.register_input_address5);
        final ProgressBar progressBar = findViewById(R.id.register_progressBar);
        final Button registerButton = findViewById(R.id.register_button);
        final CheckBox checkBox = findViewById(R.id.register_checkBox);
        currentLocation = findViewById(R.id.current_location_switch);

        final boolean[] currentLocationOption = {false};
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();


        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordConfirmEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordConfirmEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        currentLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentLocationOption[0] = true;
                addressEditText.setEnabled(false);
                addressEditText2.setEnabled(false);
                addressEditText3.setEnabled(false);
                addressEditText4.setEnabled(false);
                addressEditText5.setEnabled(false);
            } else {
                currentLocationOption[0] = false;
                addressEditText.setEnabled(true);
                addressEditText2.setEnabled(true);
                addressEditText3.setEnabled(true);
                addressEditText4.setEnabled(true);
                addressEditText5.setEnabled(true);
            }
        });


        registerButton.setOnClickListener(v -> {

            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String passwordConfirm = passwordConfirmEditText.getText().toString();
            String name = nameEditText.getText().toString();
            String surname = surnameEditText.getText().toString();


            String streetNum = addressEditText.getText().toString();
            String streetName = addressEditText2.getText().toString();
            String town = addressEditText3.getText().toString();
            String county = addressEditText4.getText().toString();
            String postCode = addressEditText5.getText().toString().toUpperCase();
            boolean correctInput = true;

            if (TextUtils.isEmpty(name)) {
                nameEditText.setError("Please Enter Name");
                correctInput = false;
            }
            if (TextUtils.isEmpty(surname)) {
                nameEditText.setError("Please Enter Surname");
                correctInput = false;
            }

            if (TextUtils.isEmpty(email) || !email.contains("@")) {
                emailEditText.setError("Please Enter Valid Email");
                correctInput = false;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Please Enter Password");
                correctInput = false;
            }

            if (password.length() < 7) {
                passwordEditText.setError("Password must be > 6 characters long");
                correctInput = false;
            }

            if (!password.equals(passwordConfirm)) {
                passwordConfirmEditText.setError("Password does not match");
                correctInput = false;
            }
            //****************************
            //Use Current Location enabled
            //****************************

            if (currentLocationOption[0]) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                checkPermissions();
                //TODO: create firebaseHelper class
                if (correctInput) {
                    progressBar.setVisibility(View.VISIBLE);
                    registerButton.setEnabled(false);
                    // Register User in firebase
                    fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userID = fAuth.getCurrentUser().getUid();
                            FirebaseUser firebaseUser = fAuth.getCurrentUser();
                            firebaseUser.sendEmailVerification().addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: email verification sent for " + userID)).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.toString()));

                            DocumentReference usersDocument = database.collection("Users").document(userID);
                            User account = new User(name, surname, email, lat, lng);
                            Map<String, Object> user = new HashMap<>();

                            user.put("name", account.getFirstName());
                            user.put("surname", account.getSurname());
                            user.put("email", account.getEmail());
                            user.put("streetNum", account.getStreetNum());
                            user.put("streetName", account.getStreetName());
                            user.put("town", account.getTown());
                            user.put("county", account.getCounty());
                            user.put("postcode", account.getPostcode());
                            user.put("address", account.getAddress());
                            user.put("latitude", account.getLatitude());
                            user.put("longitude", account.getLongitude());

                            usersDocument.set(user)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: user Profile is created for " + userID))
                                    .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.toString()));

                            Toast.makeText(RegisterActivity.this, "User account created, verification email sent.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), com.davidokelly.covidalertsystem.home.homeScreenActivity.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    registerButton.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                }

            } else {
                //****************************
                //Use Current Location disabled
                //****************************
                //TODO request Internet

                if (TextUtils.isEmpty(streetName)) {
                    addressEditText2.setError("Please Enter Valid Address");
                    correctInput = false;
                }

                if (TextUtils.isEmpty(town)) {
                    addressEditText3.setError("Please Enter Valid Address");
                    correctInput = false;
                }

                if (TextUtils.isEmpty(postCode)) {
                    addressEditText5.setError("Please Enter Valid Address"); //TODO add valid address check
                    correctInput = false;
                }

                if (correctInput) {
                    progressBar.setVisibility(View.VISIBLE);
                    registerButton.setEnabled(false);
                    // Register User in firebase
                    fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userID = fAuth.getCurrentUser().getUid();
                            FirebaseUser firebaseUser = fAuth.getCurrentUser();
                            firebaseUser.sendEmailVerification().addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: email verification sent for " + userID)).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.toString()));

                            DocumentReference usersDocument = database.collection("Users").document(userID);
                            User account = new User(streetNum, streetName, town, county, postCode, name, surname, email);
                            Map<String, Object> user = new HashMap<>();

                            user.put("name", account.getFirstName());
                            user.put("surname", account.getSurname());
                            user.put("email", account.getEmail());
                            user.put("streetNum", account.getStreetNum());
                            user.put("streetName", account.getStreetName());
                            user.put("town", account.getTown());
                            user.put("county", account.getCounty());
                            user.put("postcode", account.getPostcode());
                            user.put("address", account.getAddress());
                            user.put("latitude", account.getLatitude());
                            user.put("longitude", account.getLongitude());

                            usersDocument.set(user)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: user Profile is created for " + userID))
                                    .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.toString()));

                            Toast.makeText(RegisterActivity.this, "User account created, verification email sent.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), com.davidokelly.covidalertsystem.home.homeScreenActivity.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    registerButton.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });


    }

    private void enableUserLocation() {
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        } else {
            Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            currentLocation.setChecked(false);
        }
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void checkPermissions(){
        String[] PERMISSIONS;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            PERMISSIONS = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
        } else {
            PERMISSIONS = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
        }
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_ACCESS_REQUEST_CODE);
        } else {
            enableUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_ACCESS_REQUEST_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableUserLocation();
                }  else {
                    Toast.makeText(this,"Please enable location services to use current location",Toast.LENGTH_SHORT).show();
                    currentLocation.setChecked(false);
                }
                return;
        }
    }
}