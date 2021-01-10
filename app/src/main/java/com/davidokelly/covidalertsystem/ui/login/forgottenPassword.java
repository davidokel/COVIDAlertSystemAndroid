package com.davidokelly.covidalertsystem.ui.login;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.davidokelly.covidalertsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgottenPassword extends AppCompatActivity {
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotten_password);
        Button button = findViewById(R.id.button_send_email);
        EditText emailText = findViewById(R.id.forgotten_email_address);
        fAuth = FirebaseAuth.getInstance();

        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setBackgroundDrawable(new ColorDrawable(0xFF018786));

        button.setOnClickListener(v -> {
            String email = emailText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || !email.contains("@")) {
                emailText.setError("Please Enter Valid Email");
            } else {

                fAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String reset = "Password reset email has been sent";
                        Toast.makeText(forgottenPassword.this,reset,Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(forgottenPassword.this,"Error: "  + e.getMessage(),Toast.LENGTH_LONG).show());

            }
        });


    }
}