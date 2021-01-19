package com.davidokelly.covidalertsystem.data.User;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

public class FirebaseHelper {

//    public int getDocumentSnapshot(String TAG, DocumentReference document, String fieldPath) {
//        document.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                document.get().addOnCompleteListener(task1 -> {
//                    if (task1.isSuccessful()) {
//                        DocumentSnapshot foundDocument = task1.getResult();
//                        if (foundDocument.exists()) {
//                            return foundDocument.get(fieldPath);
//                        } else {
//                            Log.d(TAG, "No such document");
//                        }
//                    } else {
//                        Log.d(TAG, "get failed with ", task1.getException());
//                    }
//                });
//            } else {
//                Log.d(TAG, "Error: " + Objects.requireNonNull(task.getException()).getMessage());
//            }
//        });
//    }
}
