package com.davidokelly.covidalertsystem.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.davidokelly.covidalertsystem.R;
import com.davidokelly.covidalertsystem.ui.account.AccountActivity;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

public class homeScreenActivity extends AppCompatActivity {
    private static final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10000;
    private GeofencingClient geofencingClient;
    private final String TAG = "homeScreenActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF018786")));
        //TODO add location permission check here instead of maps and only enable map if TRUE
        if (savedInstanceState == null) {
            enableUserLocation();
        }

        geofencingClient = LocationServices.getGeofencingClient(this);
    }

    private void openMap() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mapFragment, new MapsFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_home:
//                Intent homeIntent = new Intent(homeScreenActivity.this, com.davidokelly.covidalertsystem.home.homeScreenActivity.class);
//                homeScreenActivity.this.startActivity(homeIntent);
                return true;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(homeScreenActivity.this, com.davidokelly.covidalertsystem.settings.SettingsActivity.class);
                homeScreenActivity.this.startActivity(settingsIntent);
                return true;
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut(); //logout
                closeFragment();
                finish();
                homeScreenActivity.this.startActivity(new Intent(getApplicationContext(), com.davidokelly.covidalertsystem.ui.login.LoginActivity.class));
            case R.id.menu_account:
                startActivity(new Intent(getApplicationContext(), AccountActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void closeFragment() {
        this.getSupportFragmentManager().popBackStack();
    }

    private void enableUserLocation() {
        Log.d(TAG, "enableUserLocation: Called");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission((this), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "enableUserLocation: Permission Already Given");
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) && ContextCompat.checkSelfPermission((this), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "enableUserLocation: Granted");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                            .ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                            .ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                }
            } else {
                Log.d(TAG, "enableUserLocation: Asked");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                            .ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                            .ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE || requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                Log.d(TAG, "onRequestPermissionResult: Granted");
                openMap();
            } else {
                //no permission
                Log.d(TAG, "onRequestPermissionResult: Not Granted");
                closeFragment();
            }
        }
    }
}