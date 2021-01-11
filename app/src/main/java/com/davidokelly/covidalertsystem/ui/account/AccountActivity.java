package com.davidokelly.covidalertsystem.ui.account;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.davidokelly.covidalertsystem.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class AccountActivity extends AppCompatActivity implements DialogChangeName.DialogChangeNameListener, DialogChangeEmail.DialogChangeEmailListener {
    public static final String TAG = "AccountActivity";
    FirebaseAuth fAuth;
    FirebaseFirestore database;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setBackgroundDrawable(new ColorDrawable(0xFF018786));
        //Displaying values
        final TextView name = findViewById(R.id.account_name);
        final TextView email = findViewById(R.id.account_email);
        final TextView UID = findViewById(R.id.account_uid);
        final TextView verified = findViewById(R.id.account_verified);
        final TextView address = findViewById(R.id.account_address);
        //Clickable Text
        final TextView changeName = findViewById(R.id.account_change_name);
        final TextView changeEmail = findViewById(R.id.account_change_email);
        final TextView changeAddress = findViewById(R.id.account_change_address);
        final TextView resendVeri = findViewById(R.id.account_resend_veri);
        final TextView deleteAccount = findViewById(R.id.account_delete);

        fAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        firebaseUser = fAuth.getCurrentUser();

        String userID = firebaseUser.getUid();
        String userEmail = getString(R.string.account_email) + " " + firebaseUser.getEmail();
        boolean userVerified = firebaseUser.isEmailVerified();

        String uidText = getString(R.string.account_uid) + " " + userID.trim();
        UID.setVisibility(View.INVISIBLE);
        UID.setText(uidText);
        UID.setVisibility(View.VISIBLE);
        email.setVisibility(View.INVISIBLE);
        email.setText(userEmail);
        email.setVisibility(View.VISIBLE);

        String text;
        if (userVerified) {
            text = getString(R.string.account_verification_status) + " Verified";
        } else {
            text = getString(R.string.account_verification_status) + " Not Verified";
        }
        verified.setVisibility(View.INVISIBLE);
        verified.setText(text);
        verified.setVisibility(View.VISIBLE);

        //Returning data
        DocumentReference userDocument = database.collection("Users").document(userID);
        userDocument.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userDocument.get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        DocumentSnapshot document = task1.getResult();
                        if (document.exists()) {
                            final String userName = getString(R.string.account_name) + " " + document.getData().get("name").toString() + " " + document.getData().get("surname").toString();
                            final String userAddress = getString(R.string.account_address) + " " + document.getData().get("address").toString();
                            name.setVisibility(View.INVISIBLE);
                            address.setVisibility(View.INVISIBLE);
                            name.setText(userName);
                            address.setText(userAddress);
                            name.setVisibility(View.VISIBLE);
                            address.setVisibility(View.VISIBLE);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task1.getException());
                    }
                });
            } else {
                Log.d(TAG, "Error: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });

        //************
        //Dialog Boxes
        //************

        changeName.setOnClickListener(v -> openChangeNameDialog());

        changeEmail.setOnClickListener(v -> {
            openChangeEmailDialog();
        });

        resendVeri.setOnClickListener(v -> {
            if (!userVerified) {
                firebaseUser.sendEmailVerification()
                        .addOnSuccessListener(aVoid -> Toast.makeText(AccountActivity.this, "Verification Email Sent", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e -> Toast.makeText(AccountActivity.this,"Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                Toast.makeText(AccountActivity.this,"User Already Verified", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void openChangeNameDialog() {
        DialogChangeName dialogChangeName = new DialogChangeName();
        dialogChangeName.show(getSupportFragmentManager(), "change name dialog");
    }

    @Override
    public void applyNameTexts(String firstName, String lastName) {
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        String userID = fAuth.getUid();
        DocumentReference usersDocument = database.collection("Users").document(userID);
        usersDocument.update("name", firstName, "surname", lastName)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: name changed for " + userID))
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.toString()));
        finish();
        startActivity(getIntent());
    }

    public void openChangeEmailDialog() {
        DialogChangeEmail dialogChangeEmail = new DialogChangeEmail();
        dialogChangeEmail.show(getSupportFragmentManager(), "change email dialog");
    }

    @Override
    public void applyEmailTexts(String email) {
        fAuth = FirebaseAuth.getInstance();
        firebaseUser = fAuth.getCurrentUser();

        firebaseUser.updateEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onSuccess: email address updated to: " + email);
                    Toast.makeText(AccountActivity.this, "Email Changed, Will take a few moments to update.", Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: " + e.toString());
            openLoginDialog(); //TODO wait for dialog response
            firebaseUser.updateEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AccountActivity.this, "Email Changed, Will take a few moments to update.", Toast.LENGTH_LONG).show();
                        firebaseUser.sendEmailVerification()
                                .addOnSuccessListener(aVoid1 -> Log.d(TAG,"onSuccess: Verification email sent"))
                                .addOnFailureListener(e1 -> Log.d(TAG,"onFailure: " + e1.toString()));
                    })
                    .addOnFailureListener(e1 -> Toast.makeText(AccountActivity.this, "Failed: " + e1.getMessage(), Toast.LENGTH_LONG).show());
        });

        finish();
        startActivity(getIntent());
    }

    public void openLoginDialog() {
        DialogLogin dialogLogin = new DialogLogin();
        dialogLogin.show(getSupportFragmentManager(), "login dialog");
    }
}