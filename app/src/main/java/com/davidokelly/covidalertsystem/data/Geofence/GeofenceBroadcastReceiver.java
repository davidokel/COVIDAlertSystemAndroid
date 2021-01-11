package com.davidokelly.covidalertsystem.data.Geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.davidokelly.covidalertsystem.data.Notifications.NotificationHelper;
import com.davidokelly.covidalertsystem.home.MapsFragment;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "GeofenceBR";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // TODO: Implement notifications alongside learning (Notification Helper from yoursTRULY (yt)
        // an Intent broadcast.
        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();
        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence...");
            return;
        }

        notificationHelper.sendReminderNotification("You are leaving the house", "Do you have your mask?", MapsFragment.class);

    }
}