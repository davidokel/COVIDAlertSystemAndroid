package com.davidokelly.covidalertsystem.ui.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.davidokelly.covidalertsystem.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int LOCATION_ACCESS_REQUEST_CODE = 100111;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //TODO lrequest permissions
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.register);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        final TextView forgottenPassword = findViewById(R.id.login_forgotten_password);
        final CheckBox checkBox = findViewById(R.id.login_checkBox);
        getSupportActionBar().hide();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        checkLocationPermissions();
        if (fAuth.getCurrentUser() != null) {
            Intent homeIntent = new Intent(LoginActivity.this, com.davidokelly.covidalertsystem.home.homeScreenActivity.class);
            startActivity(homeIntent);
            finish();
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        forgottenPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, com.davidokelly.covidalertsystem.ui.login.forgottenPassword.class)));


        //Login Button
        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();


            if (TextUtils.isEmpty(email) || !email.contains("@")) {
                usernameEditText.setError("Please Enter Valid Email");
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Please Enter Password");
            }

            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
            forgottenPassword.setClickable(false);
            loadingProgressBar.setVisibility(View.VISIBLE);

            //authenticate user
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userID = fAuth.getCurrentUser().getUid();
                    DocumentReference userDocument = database.collection("Users").document(userID);
                    userDocument.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document = task1.getResult();
                            if (document.exists()) {
                                String name = document.getData().get("name").toString();
                                updateUiWithUser(name);
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task1.getException());
                        }
                    });

                } else {
                    Toast.makeText(LoginActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
                loadingProgressBar.setVisibility(View.INVISIBLE);
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
                forgottenPassword.setClickable(true);
            });

        });

        //Register Button
        registerButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(LoginActivity.this, com.davidokelly.covidalertsystem.data.User.RegisterActivity.class);
            startActivity(settingsIntent);
        });
    }


    private void updateUiWithUser(String name) {
        String welcome = getString(R.string.welcome) + name;
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        Intent homeIntent = new Intent(LoginActivity.this, com.davidokelly.covidalertsystem.home.homeScreenActivity.class);
        LoginActivity.this.startActivity(homeIntent);
        finish();
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

    public void checkLocationPermissions(){
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
        }
    }

}