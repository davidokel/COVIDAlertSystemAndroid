package com.davidokelly.covidalertsystem.data.Geofence;

import android.util.Log;
import android.view.View;

import com.davidokelly.covidalertsystem.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.time.LocalTime;

public class ExitLog {

    private static final String TAG = "ExitLog";
    private static long totalLogCount = 0;
    private static final int range = 30; //Time range in minutes
    private LocalTime exitTime;
    private long exitCount;
    private long identifier;
    //TODO add Day value (1-7) and store in firebase document by day
    //TODO only show notifs if exitCount >= 5. Delete stored ExitLogs after two weeks of data not being set (maybe add a serverMgmt class)
    public ExitLog() {

        this.exitTime = LocalTime.now();
        this.exitCount = 1; //Adjust exit count
        this.identifier = totalLogCount;
        totalLogCount += 1;
        //TODO replace ID with firebase.push key
    }

    public void addToDatabase(CollectionReference collection) {
        DocumentReference database = collection.document(String.valueOf(this.identifier));

        database.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                DocumentSnapshot document = task1.getResult();
                if (document.exists()) {
                    Log.d(TAG, "Document Accessed");
                    //CHECK IF TIME IS WITHIN RANGE
                    //TODO check if time

                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task1.getException());
            }
        });


        database.set(this)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Data Added to database for LOG: " + this.identifier))
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.toString()));
    }

    public static long getTotalLogCount() {
        return totalLogCount;
    }

    public static void setTotalLogCount(int totalLogCount) {
        ExitLog.totalLogCount = totalLogCount;
    }

    public LocalTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalTime exitTime) {
        this.exitTime = exitTime;
    }

    public long getExitCount() {
        return exitCount;
    }

    public void setExitCount(int exitCount) {
        this.exitCount = exitCount;
    }

    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }
}
