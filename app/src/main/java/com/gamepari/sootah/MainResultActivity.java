package com.gamepari.sootah;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.gamepari.sootah.googleplay.GooglePlayServices;
import com.gamepari.sootah.images.BitmapCompose;
import com.gamepari.sootah.images.PhotoCommonMethods;
import com.gamepari.sootah.images.PhotoMetaData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;


public class MainResultActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener {

    private GoogleMap googleMap;
    private LocationClient mLocationClient;

    private View mCaptureView;

    private ProgressDialog progressDialog;
    private ProgressDialog loadingDialog;

    private PhotoMetaData mPhotoMetaData;

    private static final int REQ_SETTINGS_GPS = 1233;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_result);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = mapFragment.getMap();

        findViewById(R.id.gallery).setOnClickListener(this);
        findViewById(R.id.camera).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);

        mCaptureView = findViewById(R.id.capture_area);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {

        if (mLocationClient != null) mLocationClient.disconnect();

        super.onStop();
    }

    private void initLocationClient() {

        loadingDialog = ProgressDialog.show(this, "", "Loading...");

        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

    }

    private void showDialogTurnOnGPS() {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.location_disabled)
                .setMessage(R.string.location_disalbed_text)
                .setNegativeButton(R.string.ignore, null)
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {

                        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(i, REQ_SETTINGS_GPS);

                    }
                })
                .show();

    }

    private void showDialogSetLocation() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.location_set)
                .setMessage(R.string.location_set_text)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {

                        LocationManager manager = (LocationManager) MainResultActivity.this.getSystemService(Context.LOCATION_SERVICE);

                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            // gps disabled.
                            showDialogTurnOnGPS();
                        } else {
                            // gps enabled.

                            initLocationClient();
                        }
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PhotoCommonMethods.REQ_CAMERA || requestCode == PhotoCommonMethods.REQ_GALLERY) {

            if (resultCode == RESULT_OK) {

                LatLng latLng = null;

                try {

                    mPhotoMetaData = null;

                    // try extract metadata from image file.
                    mPhotoMetaData = PhotoCommonMethods.getMetaDataFromURI(this, requestCode, data);

                    latLng = mPhotoMetaData.getLatLng();

                    if (latLng != null) {
                        setResultAction(mPhotoMetaData);
                    }
                    else {
                        showDialogSetLocation();
                    }

                } catch (IOException e) {

                    //exif read failed.

                }

            }

        } else if (requestCode == GooglePlayServices.CONNECTION_FAILURE_RESOLUTION_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Try the request again.
            }
        } else if (requestCode == REQ_SETTINGS_GPS) {

            LocationManager manager = (LocationManager) MainResultActivity.this.getSystemService(Context.LOCATION_SERVICE);

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                //gps enabled.
                initLocationClient();
            }

            else {
                //gps disabled.
                Toast.makeText(this, "setting failed...", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void setResultAction(PhotoMetaData photoMetaData) {
        LatLng latLng = photoMetaData.getLatLng();

        CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(latLng, 16.f);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);

        PlaceholderFragment placeholderFragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        placeholderFragment.setImage(photoMetaData);

    }

    /* Google Location APIs Callback */

    @Override
    public void onConnected(Bundle bundle) {

        Location location = mLocationClient.getLastLocation();

        if (location == null) {
            //keep going loading...
            LocationRequest locationRequest = new LocationRequest();

            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(1000);

            mLocationClient.requestLocationUpdates(locationRequest, this);
        }
        else {

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mPhotoMetaData.setLatLng(latLng);

            loadingDialog.dismiss();

            setResultAction(mPhotoMetaData);
        }

    }

    @Override
    public void onLocationChanged(Location location) {

        mLocationClient.removeLocationUpdates(this);

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mPhotoMetaData.setLatLng(latLng);

        loadingDialog.dismiss();

        setResultAction(mPhotoMetaData);

    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, GooglePlayServices.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            GooglePlayServices.showErrorDialog(this, connectionResult.getErrorCode());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gallery:
                PhotoCommonMethods.photoFromGallery(MainResultActivity.this, "select");
                break;

            case R.id.camera:
                PhotoCommonMethods.photoFromCamera(MainResultActivity.this);
                break;

            case R.id.share:
                share();
                break;
        }
    }



    private void share() {

        if (mPhotoMetaData == null) {
            Toast.makeText(this, R.string.choose_img_plz,Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog = ProgressDialog.show(MainResultActivity.this, null,"Saving...");

        googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                new ComposeBitmapTask().execute(mPhotoMetaData, bitmap);
            }
        });
    }



    private class ComposeBitmapTask extends AsyncTask<Object, Integer, Boolean> {

        private void connectMediaScan(final String filePath) {

            MediaScannerConnection.scanFile(MainResultActivity.this,
                    new String[]{filePath,}, new String[]{"image/jpg",},
                    new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    Log.d(this.toString(), s);
                }
            });

        }

        @Override
        protected Boolean doInBackground(Object... params) {
            PhotoMetaData photoMetaData = (PhotoMetaData) params[0];

            Bitmap mapBitmap = (Bitmap) params[1];
            Bitmap mainBitmap = PhotoCommonMethods.bitmapFromView(mCaptureView);
            Bitmap resultBitmap = BitmapCompose.composeBitmap(mainBitmap, mapBitmap, photoMetaData);
            Bitmap scaledBitmap = BitmapCompose.resizeBitmap(resultBitmap);

            boolean isSuccess = false;
            try {
                File bitmapFile = PhotoCommonMethods.saveImageFromBitmap(scaledBitmap);
                if (bitmapFile != null) {

                    isSuccess = PhotoCommonMethods.setMetaDataToFile(bitmapFile, photoMetaData);

                    if (isSuccess) {

                        connectMediaScan(bitmapFile.getPath());

                    }
                    else {
                        bitmapFile.delete();
                    }

                    mainBitmap.recycle();
                    mapBitmap.recycle();
                    resultBitmap.recycle();
                    scaledBitmap.recycle();

                }

            } catch (IOException e) {
                Log.d(this.toString(), e.getMessage());
            }

            return isSuccess;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();
            if (aBoolean) Toast.makeText(MainResultActivity.this, "success", Toast.LENGTH_LONG).show();
            else if (aBoolean) Toast.makeText(MainResultActivity.this, "noooo", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private ImageView ivPhoto;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_result, container, false);

            ivPhoto = (ImageView) rootView.findViewById(R.id.image);

            return rootView;
        }

        public void setImage(PhotoMetaData photoMetaData) {
            new AdjustBitmapTask().execute(photoMetaData);
        }

        private class AdjustBitmapTask extends AsyncTask<PhotoMetaData, Integer, Bitmap> {

            @Override
            protected Bitmap doInBackground(PhotoMetaData... metaDatas) {
                Bitmap resultBitmap = BitmapCompose.adjustBitmap(metaDatas[0]);
                return resultBitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                ivPhoto.setImageBitmap(null);
                ivPhoto.setImageBitmap(bitmap);
            }
        }

    }
}
