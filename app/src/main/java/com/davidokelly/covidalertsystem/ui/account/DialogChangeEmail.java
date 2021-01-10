package com.davidokelly.covidalertsystem.ui.account;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.davidokelly.covidalertsystem.R;

public class DialogChangeEmail extends AppCompatDialogFragment {

    private EditText editTextEmail;
    private DialogChangeEmailListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_change_email, null);

        builder.setView(view)
                .setTitle(R.string.account_change_email)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String email = editTextEmail.getText().toString();
                    listener.applyEmailTexts(email);
                });
        editTextEmail = view.findViewById(R.id.change_email);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (DialogChangeEmailListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement DialogChangeNameListener");
        }
    }

    public interface DialogChangeEmailListener {
        void applyEmailTexts(String email);
    }
}
