package com.davidokelly.covidalertsystem.home;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentContainerView;

import com.davidokelly.covidalertsystem.R;
import com.davidokelly.covidalertsystem.data.Geofence.GeofenceHelper;
import com.davidokelly.covidalertsystem.data.Notifications.ReminderBroadcastReciever;
import com.davidokelly.covidalertsystem.ui.account.AccountActivity;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.intellij.lang.annotations.JdkConstants;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class homeScreenActivity extends AppCompatActivity {
    private static final int LOCATION_ACCESS_REQUEST_CODE = 10001;
    private Bundle bundle;
    private GeofencingClient geofencingClient;
    private final String TAG = "homeScreenActivity";
    private FragmentContainerView mapFragment;
    private ListView listView;
    private TextView enableLocationText;
    private MapsFragment map;
    private boolean hasPermission = false;
    private ArrayList<String> array;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bundle = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF018786")));
        checkLocationPermissions();
        mapFragment = findViewById(R.id.mapFragment);
        listView = findViewById(R.id.listView);
        enableLocationText = findViewById(R.id.text_enable_location);

        geofencingClient = LocationServices.getGeofencingClient(this);

        array = new ArrayList<>();
        getTimesArray();
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.list_entries,array);
        listView.setAdapter(arrayAdapter);
    }

    private void openMap() {
        if (bundle == null) {
            map = new MapsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapFragment, map)
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
                geofencingClient.removeGeofences(new GeofenceHelper(getApplicationContext()).getPendingIntent())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Geofences Removed"))
                .addOnFailureListener(e -> Log.d(TAG, "onOptionsItemSelected: Geofence Removal Failed"));
                homeScreenActivity.this.startActivity(new Intent(getApplicationContext(), com.davidokelly.covidalertsystem.ui.login.LoginActivity.class));
                finish();
                //TODO Clear Geofences on log out
                return true;
            case R.id.menu_account:
                startActivity(new Intent(getApplicationContext(), AccountActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_ACCESS_REQUEST_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openMap();
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    mapFragment.setVisibility(View.INVISIBLE);
                    enableLocationText.setVisibility(View.VISIBLE);
                }
                return;
        }
    }

    private void getTimesArray() {
        String UID = FirebaseAuth.getInstance().getUid();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference userCollection;
        for (int i = 1; i < 8; i++) {
            userCollection = database.collection("ExitTimes").document(UID).collection(String.valueOf(i));
            int finalI = i;
            userCollection.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "getTimesArray: Task Successful for day - " + finalI);
                    QuerySnapshot querySnapshot = task.getResult();
                    if (!querySnapshot.isEmpty()) {
                        List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                        for (int j = 0; j < docs.size(); j++) {
                            String day = getDay(finalI);
                            LocalTime timeObj = getTimeFromDocument(docs.get(j));
                            long exitCount = docs.get(j).getLong("exitCount");
                            String time = timeObj.toString();
                            String count = String.valueOf(exitCount);
                            String entry = day + " - " + time + " : Count " + count;
                            array.add(entry);
                            Log.d(TAG, "getTimesArray: Time Added for :" + entry);
                            ArrayAdapter arrayAdapter2 = new ArrayAdapter(this,R.layout.list_entries,array);
                            listView.setAdapter(arrayAdapter2);

                            if (exitCount >= 5) {
                                createTimedAlarm(timeObj);
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "getTimesArray: Task Error - " + task.getException().getMessage());
                }
            }).addOnCanceledListener(() -> {
                Log.d(TAG, "onCanceled: Cancelled Get");
            });
        }
    }

    private LocalTime getTimeFromDocument(DocumentSnapshot doc) {
        Log.d(TAG, "getTimeFromDocument: Used");
        Map<String, Long> map = (Map<String, Long>) doc.get("exitTime");
        int hour,min,sec;
        hour = Objects.requireNonNull(map.get("hour")).intValue();
        min = Objects.requireNonNull(map.get("minute")).intValue();
        sec = Objects.requireNonNull(map.get("second")).intValue();

        return LocalTime.of(hour,min,sec);
    }

    private String getDay(int num) {
        String day;
        switch (num) {
            case 1:
                day = "Sunday";
                break;
            case 2:
                day = "Monday";
                break;
            case 3:
                day = "Tuesday";
                break;
            case 4:
                day = "Wednesday";
                break;
            case 5:
                day = "Thursday";
                break;
            case 6:
                day = "Friday";
                break;
            case 7:
                day = "Saturday";
                break;
            default:
                day = "Unknown Day: " + num;
        }
        return day;
    }
    private void createTimedAlarm(LocalTime time) {
        Intent intent =  new Intent(homeScreenActivity.this, ReminderBroadcastReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(homeScreenActivity.this,-30,intent,0);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long lTime = (time.toNanoOfDay() / 1000) - 30*60*60;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,lTime,1000*60*60*24*7,pendingIntent);
    }

    private void clearAlarms() {
        Intent intent =  new Intent(homeScreenActivity.this, ReminderBroadcastReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(homeScreenActivity.this,-30,intent,0);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}

