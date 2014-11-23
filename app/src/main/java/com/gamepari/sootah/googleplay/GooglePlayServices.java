package com.gamepari.sootah.googleplay;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by seokceed on 2014-11-23.
 */

public class GooglePlayServices {

    public final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static boolean servicesConnected(Activity activity) {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);

        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        }

        else {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            if (errorDialog != null) {

                //ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();

                errorFragment.setDialog(errorDialog);

                errorFragment.show(((FragmentActivity)activity).getSupportFragmentManager(), "Location Updates");

            }
        }

        return true;
    }

    public static void showErrorDialog(Activity activity, int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                activity,
                CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(((FragmentActivity)activity).getSupportFragmentManager(), "Google Play Services");
        }
    }


    public static class ErrorDialogFragment extends DialogFragment {

        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //return super.onCreateDialog(savedInstanceState);
            return mDialog;
        }
    }


}
