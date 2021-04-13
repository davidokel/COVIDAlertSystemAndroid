package com.davidokelly.covidalertsystem.data.Geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.davidokelly.covidalertsystem.data.Notifications.NotificationHelper;
import com.davidokelly.covidalertsystem.home.MapsFragment;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBR";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive Triggered");
        // TODO: Implement notifications alongside learning (Notification Helper from yoursTRULY (yt)
        // an Intent broadcast.
        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();
        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence...");
            return;
        }

        notificationHelper.sendReminderNotification("Do you have your mask?", "You appear to be leaving your home. Please be safe.", MapsFragment.class);
        logTime();

        //Background task to keep receiver running
        Log.d(TAG, "onReceive: BOOT Action");
        PendingResult pendingResult = goAsync();
        new Task(pendingResult, intent).execute();
    }

    private void logTime() {
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        ExitLog exitLog = new ExitLog();
        String UID = fAuth.getUid();

        DocumentReference exitTimes = database.collection("ExitTimes").document(UID);
        exitLog.addToDatabase(exitTimes);
    }

    private static class Task extends AsyncTask<Void, Void, Void> {
        PendingResult pendingResult;
        Intent intent;

        public Task(PendingResult pendingResult, Intent intent) {
            this.pendingResult = pendingResult;
            this.intent = intent;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Log.d(TAG, "doInBackground: Work started");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: Work Finished");
            pendingResult.finish();
        }
    }
}