package com.gamepari.sootah;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;


public class MainResultActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener, GeoCodeTask.OnTaskFinshListener {

    private LocationClient mLocationClient;

    private View mCaptureView;
    private ProgressDialog mLoadingProgress;

    private PhotoMetaData mPhotoMetaData;

    private static final int REQ_SETTINGS_GPS = 1233;

    private Uri savedUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_result);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ImageFragment())
                    .commit();

            MarkerMapFragment mapFragment = new MarkerMapFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.map, mapFragment)
                    .commit();

        }

        findViewById(R.id.gallery).setOnClickListener(this);
        findViewById(R.id.camera).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);

        mCaptureView = findViewById(R.id.capture_area);

        mLoadingProgress = new ProgressDialog(this);
        mLoadingProgress.setMessage("Loading...");
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PhotoCommonMethods.REQ_CAMERA || requestCode == PhotoCommonMethods.REQ_GALLERY) {

            if (resultCode == RESULT_OK) {

                new MetaDataTask().execute(requestCode, data);

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

            GeoCodeTask geoCodeTask = new GeoCodeTask(this, this);
            geoCodeTask.execute(latLng);

        }

    }

    @Override
    public void onLocationChanged(Location location) {

        mLocationClient.removeLocationUpdates(this);

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mPhotoMetaData.setLatLng(latLng);

        GeoCodeTask geoCodeTask = new GeoCodeTask(this, this);
        geoCodeTask.execute(latLng);

    }

    @Override
    public void onTaskFinish(Address address) {

        mLoadingProgress.dismiss();

        if (address != null) {
            mPhotoMetaData.setAddress(address);
            setResultAction(mPhotoMetaData);
        }
        else {
            Toast.makeText(this, "hing...", Toast.LENGTH_LONG).show();
        }

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
                saveAndShare();
                break;
        }
    }

    private void setResultAction(PhotoMetaData photoMetaData) {

        savedUri = null;
        mCaptureView.setDrawingCacheEnabled(false);

        MarkerMapFragment mapFragment = getMapFragment();
        mapFragment.highlightMapFromLatLng(photoMetaData);

        ImageFragment imageFragment = (ImageFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        imageFragment.setImage(photoMetaData);
    }

    private void initLocationClient() {

        mLoadingProgress.show();

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

    public void onMapMoved() {
        savedUri = null;
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

    private MarkerMapFragment getMapFragment() {
        MarkerMapFragment markerMapFragment = (MarkerMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        return markerMapFragment;
    }

    private void saveAndShare() {

        if (mPhotoMetaData == null) {
            Toast.makeText(this, R.string.choose_img_plz,Toast.LENGTH_LONG).show();
            return;
        }

        if (savedUri != null) {
            PhotoCommonMethods.sharePhotoFromUri(this, savedUri, mPhotoMetaData);
        }

        else {

            mLoadingProgress.show();

            OnSaveListener onSaveListener = new OnSaveListener() {
                @Override
                public void onSaveFinished(File savedFile) {

                    mLoadingProgress.dismiss();

                    if (savedFile != null) {
                        Uri fileUri = Uri.fromFile(savedFile);
                        savedUri = fileUri;
                        PhotoCommonMethods.sharePhotoFromUri(MainResultActivity.this, fileUri, mPhotoMetaData);
                    }
                }
            };
            saveBitmapFile(onSaveListener);
        }
    }


    public static interface OnSaveListener {
        public void onSaveFinished(File savedFile);
    }

    private void saveBitmapFile(final OnSaveListener onSaveListener) {

        MarkerMapFragment mapFragment = getMapFragment();

        mapFragment.requestSnapShot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                new WriteBitmapTask(onSaveListener).execute(mPhotoMetaData, bitmap);
            }
        });
    }

    private class MetaDataTask extends AsyncTask<Object, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            mLoadingProgress.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Object... objects) {

            mPhotoMetaData = null;

            int requestCode =  (Integer)(objects[0]);
            Intent data = (Intent)objects[1];

            LatLng latLng = null;

            // try extract metadata from image file.
            try {
                mPhotoMetaData = PhotoCommonMethods.getMetaDataFromURI(MainResultActivity.this, requestCode, data);
                latLng = mPhotoMetaData.getLatLng();

                if (latLng != null) {
                    Address address = GeoCodeTask.getAddressFromLocation(MainResultActivity.this, latLng);
                    mPhotoMetaData.setAddress(address);
                }
                return true;
            } catch (IOException e) {
                Log.d(this.toString(), e.getMessage());
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mLoadingProgress.dismiss();

            if (aBoolean) {
                if (mPhotoMetaData.getAddress() != null) {
                    setResultAction(mPhotoMetaData);
                }
                else {
                    showDialogSetLocation();
                }
            }
        }
    }


    private class WriteBitmapTask extends AsyncTask<Object, Integer, File> {

        private OnSaveListener mOnSaveListener;

        private WriteBitmapTask(OnSaveListener onSaveListener) {
            mOnSaveListener = onSaveListener;
        }

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
        protected File doInBackground(Object... params) {
            PhotoMetaData photoMetaData = (PhotoMetaData) params[0];

            Bitmap mapBitmap = (Bitmap) params[1];
            Bitmap workBitmap = PhotoCommonMethods.bitmapFromView(mCaptureView);
            BitmapCompose.composeBitmap(workBitmap, mapBitmap, photoMetaData);
            Bitmap scaledBitmap = BitmapCompose.resizeBitmap(workBitmap);

            File bitmapFile = null;
            boolean isSuccess = false;
            try {
                bitmapFile = PhotoCommonMethods.saveImageFromBitmap(scaledBitmap);
                if (bitmapFile != null) {

                    isSuccess = PhotoCommonMethods.setMetaDataToFile(bitmapFile, photoMetaData);

                    if (isSuccess) {
                        connectMediaScan(bitmapFile.getPath());
                    }
                    else {
                        bitmapFile.delete();
                    }
                }

            } catch (IOException e) {
                Log.d(this.toString(), e.getMessage());
            } finally {
                PhotoCommonMethods.recycleBitmap(mapBitmap);
                PhotoCommonMethods.recycleBitmap(workBitmap);
                PhotoCommonMethods.recycleBitmap(scaledBitmap);
            }

            return isSuccess?bitmapFile:null;

        }

        @Override
        protected void onPostExecute(File file) {
            mOnSaveListener.onSaveFinished(file);
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ImageFragment extends Fragment {

        private ImageView ivPhoto;

        public ImageFragment() {
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

                if (ivPhoto.getDrawable() != null) {

                    Bitmap prevBitmap = ((BitmapDrawable)(ivPhoto.getDrawable())).getBitmap();

                    if (prevBitmap != null) {
                        ivPhoto.setImageBitmap(null);
                        PhotoCommonMethods.recycleBitmap(prevBitmap);
                    }

                }

                ivPhoto.setImageBitmap(bitmap);
            }
        }

    }
}
