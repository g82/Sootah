package com.gamepari.sootah;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.gamepari.sootah.images.PhotoMetaData;

/**
 * Created by seokceed on 2014-12-02.
 */
public class InputDialogFragment extends DialogFragment {

    InputDialogListener inputDialogListener;
    private EditText etPlace, etAddress;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        inputDialogListener = (InputDialogListener) activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputDialogListener.onDialogInputed(etPlace.getText().toString(), etAddress.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setTitle(getString(R.string.inputdialog_title));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_inputdialog, null);

        etPlace = (EditText) v.findViewById(R.id.et_place);
        etAddress = (EditText) v.findViewById(R.id.et_address);

        PhotoMetaData metaData = ((ResultActivity) getActivity()).getPhotoMetaData();
        if (metaData.getPlaceName() != null) {
            etPlace.setText(metaData.getPlaceName());
        }
        etAddress.setText(metaData.convertAddressString());

        builder.setView(v);
        builder.setCancelable(false);

        return builder.create();
    }

    public interface InputDialogListener {
        public void onDialogInputed(String place, String address);
    }

}
