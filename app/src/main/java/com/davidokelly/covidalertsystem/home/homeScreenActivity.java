package com.davidokelly.covidalertsystem.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
    private static final int LOCATION_ACCESS_REQUEST_CODE = 10001;
    private Bundle bundle;
    private GeofencingClient geofencingClient;
    private final String TAG = "homeScreenActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bundle = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF018786")));
        //TODO add location permission check here instead of maps and only enable map if TRUE
        checkPermissions();



        geofencingClient = LocationServices.getGeofencingClient(this);
    }

    private void openMap() {
        if (bundle == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapFragment, new MapsFragment())
                    .commit();
        }
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_ACCESS_REQUEST_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openMap();
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision. //TODO Add Replacement Page
                }
                return;
        }
    }
}

