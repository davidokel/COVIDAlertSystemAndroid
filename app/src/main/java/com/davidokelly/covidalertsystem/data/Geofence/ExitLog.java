package com.davidokelly.covidalertsystem.data.Geofence;

import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.davidokelly.covidalertsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExitLog {

    private static final String TAG = "ExitLog";
    private static final int range = 30; //Time range in minutes
    private final LocalTime exitTime;
    private final long exitCount;
    private final String identifier;
    private final int day;

    //TODO only show notifs if exitCount >= 5. Delete stored ExitLogs after two weeks of data not being set (maybe add a serverMgmt class)
    public ExitLog() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        this.exitTime = LocalTime.now();
        this.exitCount = 1; //Adjust exit count
        this.identifier = UUID.randomUUID().toString();
        this.day = calendar.get(Calendar.DAY_OF_WEEK);
    }

    public void addToDatabase(DocumentReference document) {
        CollectionReference database = document.collection(String.valueOf(this.day));

        database.get().addOnCompleteListener(task -> {
            QuerySnapshot querySnapshot = task.getResult();
            if (!querySnapshot.isEmpty()) {
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                for (int i = 0; i < documents.size(); i++) {
                    LocalTime docTime = getTimeFromDocument(documents.get(i));
                    long timeDiff = Math.abs(this.exitTime.until(docTime, ChronoUnit.MINUTES));

                    if (timeDiff <= range) {
                        int time1 = this.exitTime.toSecondOfDay();
                        int time2 = docTime.toSecondOfDay();
                        long exitCount = documents.get(i).getLong("exitCount");
                        long newT = (time2 * exitCount + time1) / (exitCount + 1);
                        LocalTime newTime = LocalTime.ofSecondOfDay(newT);
                        String ID = documents.get(i).getId();
                        DocumentReference doc = database.document(ID);

                        Map<String, Object> data = new HashMap<>();
                        data.put("exitCount",exitCount + 1);
                        data.put("exitTime",newTime);

                        doc.set(data).addOnSuccessListener(aVoid -> Log.d(TAG, "addToDatabase: Change Successful"))
                                .addOnFailureListener(e -> Log.d(TAG, "addToDatabase: Change Error - " + e.getMessage()));
                        return;
                    }
                }
                // IF NOT IN DATABASE
                DocumentReference doc = database.document(this.identifier);

                Map<String, Object> data = new HashMap<>();
                data.put("exitCount",this.exitCount);
                data.put("exitTime",this.exitTime);

                doc.set(data).addOnSuccessListener(aVoid -> Log.d(TAG, "addToDatabase: Add Successful"))
                        .addOnFailureListener(e -> Log.d(TAG, "addToDatabase: Add Error - " + e.getMessage()));
            } else {
                //DOCUMENT IS EMPTY
                Log.d(TAG, "onComplete: Collection Accessed but is empty");
                DocumentReference doc = database.document(this.identifier);

                Map<String, Object> data = new HashMap<>();
                data.put("exitCount",this.exitCount);
                data.put("exitTime",this.exitTime);

                doc.set(data).addOnSuccessListener(aVoid -> Log.d(TAG, "addToDatabase: Add Successful"))
                        .addOnFailureListener(e -> Log.d(TAG, "addToDatabase: Add Error - " + e.getMessage()));

            }
        }).addOnFailureListener(e -> Log.d(TAG, "addToDatabase: Failure - " + e.getMessage()));
    }

    private LocalTime getTimeFromDocument(DocumentSnapshot doc) {
        Log.d(TAG, "getTimeFromDocument: Used");
        Map<String, Long> map = (Map<String, Long>) doc.get("exitTime");
        int hour,min,sec;
        hour = map.get("hour").intValue();
        min = map.get("minute").intValue();
        sec = map.get("second").intValue();

        return LocalTime.of(hour,min,sec);
    }

}
