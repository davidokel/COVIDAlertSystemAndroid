package com.davidokelly.covidalertsystem.ui.account;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DialogDeleteAccount extends AppCompatDialogFragment {

    private static final String TAG = "DialogDeleteAcc";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = fAuth.getCurrentUser();
        String UID = fAuth.getUid();
        DocumentReference userDocument = database.collection("Users").document(UID);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Please choose your answer below.");
        builder.setTitle("Are you sure you want to delete your account?");
        builder.setPositiveButton("Confirm", (dialog, which) ->
                userDocument.delete()
                        .addOnFailureListener(e -> Toast.makeText(getActivity(),"Error: " + e.getLocalizedMessage(),Toast.LENGTH_LONG).show())
                        .addOnSuccessListener(aVoid1 -> {
                            Log.d(TAG, "onSuccess: userDocument deleted successfully for " + UID);
                            firebaseUser.delete().addOnSuccessListener(aVoid -> {
                                Toast.makeText(getActivity(), "Account has been deleted successfully", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getActivity(), com.davidokelly.covidalertsystem.ui.login.LoginActivity.class));
                                //TODO delete exitlogs
                            }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                        }));
        builder.setNegativeButton("Cancel", (dialog, which) -> {
        });
        return builder.create();
    }
}
