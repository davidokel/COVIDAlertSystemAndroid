package com.davidokelly.covidalertsystem.ui.account;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.davidokelly.covidalertsystem.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DialogLogin extends AppCompatDialogFragment {

    private static final String TAG = "LoginDialog";
    private EditText editTextPassword;
    private Context context;

    public DialogLogin(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login_again, null);

        builder.setView(view)
                .setTitle(R.string.login_again)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String password = editTextPassword.getText().toString();
                    reAuthenticate(password);
                });

        editTextPassword = view.findViewById(R.id.password);
        CheckBox checkBox = view.findViewById(R.id.show_password);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        return builder.create();
    }

    private void reAuthenticate(String password) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onSuccess: User Re-authenticated");
                    Toast.makeText(this.context, "Logged in successfully.", Toast.LENGTH_LONG).show();

                })
                .addOnFailureListener(e -> {
                    Log.d(TAG,"onFailure: " + e.getMessage());
                    Toast.makeText(this.context, "Error: " + e.getMessage() + " Please try again.", Toast.LENGTH_LONG).show();
                    reAuthenticate(password);
                });
    }
}
