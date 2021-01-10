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

public class DialogChangeName extends AppCompatDialogFragment {

    private EditText editTextName;
    private EditText editTextSurname;
    private DialogChangeNameListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_change_name, null);

        builder.setView(view)
                .setTitle(R.string.account_change_name)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String firstName = editTextName.getText().toString();
                    String surname = editTextSurname.getText().toString();
                    listener.applyNameTexts(firstName,surname);
                });
        editTextName = view.findViewById(R.id.change_first_name);
        editTextSurname = view.findViewById(R.id.change_last_name);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (DialogChangeNameListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement DialogChangeNameListener");
        }
    }

    public interface DialogChangeNameListener {
        void applyNameTexts(String firstName, String lastName);
    }
}
