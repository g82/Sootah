package com.gamepari.sootah;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;

/**
 * Created by seokceed on 2014-12-02.
 */
public class InputDialogFragment extends DialogFragment {

    InputDialogListener inputDialogListener;
    private EditText etText;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ImageFragment imageFragment = (ImageFragment) (((ResultActivity) activity).getSupportFragmentManager().findFragmentById(R.id.container));
        inputDialogListener = imageFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputDialogListener.onDialogInputed(etText.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setTitle(getString(R.string.inputdialog_title));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        etText = (EditText) inflater.inflate(R.layout.inputdialog_edittext, null);
        builder.setView(etText);

        return builder.create();
    }

    public interface InputDialogListener {
        public void onDialogInputed(String text);
    }

}
