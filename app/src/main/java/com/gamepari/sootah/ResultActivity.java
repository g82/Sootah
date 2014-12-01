package com.gamepari.sootah;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.gamepari.sootah.googleplay.GooglePlayServices;
import com.gamepari.sootah.images.CaptureBitmapTask;
import com.gamepari.sootah.images.MetaDataTask;
import com.gamepari.sootah.images.PhotoCommonMethods;
import com.gamepari.sootah.images.PhotoMetaData;
import com.gamepari.sootah.location.Places;
import com.gamepari.sootah.location.PlacesTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;


public class ResultActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GoogleMap.OnCameraChangeListener, MarkerMapFragment.MarkerDragListener,
        LocationListener,
        PlacesTask.OnPlaceTaskListener, CaptureBitmapTask.OnSaveListener,
        PlaceListDialogFragment.OnPlaceClickListener, MetaDataTask.OnMetaTaskListener {

    private static final int REQ_SETTINGS_GPS = 1233;
    private LocationClient mLocationClient;
    private View mCaptureView;
    private ProgressDialog mLoadingProgress;
    private PhotoMetaData mPhotoMetaData;
    private Uri savedUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int requestCode = getIntent().getIntExtra("requestCode", -1);

        if (requestCode == PhotoCommonMethods.REQ_CAMERA) {
            PhotoCommonMethods.photoFromCamera(this);
        }

        else if (requestCode == PhotoCommonMethods.REQ_GALLERY) {
            PhotoCommonMethods.photoFromGallery(this, getString(R.string.pick_image));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (savedInstanceState == null) {

            ImageFragment imageFragment = new ImageFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, imageFragment)
                    .commit();

            MarkerMapFragment mapFragment = new MarkerMapFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.map, mapFragment)
                    .commit();

        }

        mCaptureView = findViewById(R.id.capture_area);

        mLoadingProgress = new ProgressDialog(this);
        mLoadingProgress.setMessage(getString(R.string.loading));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_result_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_camera:
                PhotoCommonMethods.photoFromCamera(this);
                break;

            case R.id.action_gallery:
                PhotoCommonMethods.photoFromGallery(this, getString(R.string.pick_image));
                break;

            case R.id.action_share:
                saveAndShare();
                break;
        }
        return super.onOptionsItemSelected(item);
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

                new MetaDataTask(this, this).execute(requestCode, data);

            }

        } else if (requestCode == REQ_SETTINGS_GPS) {

            LocationManager manager = (LocationManager) ResultActivity.this.getSystemService(Context.LOCATION_SERVICE);

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                //gps enabled.
                initLocationClient();
            }

            else {
                //gps disabled.
            }

        } else if (requestCode == GooglePlayServices.CONNECTION_FAILURE_RESOLUTION_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Try the request again.
            }
        }
    }


    /**
     * ------------------------------------------------------------
     * first process!! get image, read MetaData..
     * ------------------------------------------------------------
     */

    @Override
    public void onMetaDataTaskStarted() {
        mLoadingProgress.show();
    }

    @Override
    public void onMetaDataTaskFinished(PhotoMetaData photoMetaData) {

        mLoadingProgress.dismiss();

        mPhotoMetaData = photoMetaData;

        int addressType = photoMetaData.getAddressType();

        if (addressType == PhotoMetaData.ADDRESS_FROM_PLACESAPI || addressType == PhotoMetaData.ADDRESS_FROM_GEOCODE) {
            setResultAction(photoMetaData);
        } else {
            //get Location Data Failed.
            showDialogSetLocation();
        }

    }

    /**
     * ------------------------------------------------------------
     * second process!! no metadata, turn on GPS, find address or places.
     * make metadata.
     * ------------------------------------------------------------
     */


    @Override
    public void onPlacesTaskFinished(int addressType, PhotoMetaData photoMetaData) {
        mLoadingProgress.dismiss();
        mPhotoMetaData = photoMetaData;

        setResultAction(photoMetaData);

        /*switch (addressType) {

            case PhotoMetaData.ADDRESS_FROM_GEOCODE:
                setResultAction(photoMetaData);
                break;

            case PhotoMetaData.ADDRESS_FROM_PLACESAPI:

                PlaceListDialogFragment dialogFragment = new PlaceListDialogFragment();
                dialogFragment.setPlacesList(photoMetaData.getPlacesList());
                dialogFragment.show(getSupportFragmentManager(), "dialog");
                break;

            case PhotoMetaData.ADDRESS_NONE:
                setResultAction(photoMetaData);
                break;

        }*/

    }


    /**
     *
     * ------------------------------------------------------------
     * Third process!! setResultAction
     * ------------------------------------------------------------
     *
     */

    private void setResultAction(PhotoMetaData photoMetaData) {

        deleteSavedFile();

        //mCaptureView.setDrawingCacheEnabled(false);

        MarkerMapFragment mapFragment = getMapFragment();
        mapFragment.highlightMapFromLatLng(photoMetaData);

        ImageFragment imageFragment = (ImageFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        imageFragment.setImage(photoMetaData);

        findViewById(R.id.rl_none).setVisibility(View.GONE);
    }

    // google map camera changed, savedUri = null.
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        deleteSavedFile();
    }

    @Override
    public void onMarkerPositionChanged(LatLng latLng) {
        deleteSavedFile();

        mPhotoMetaData.clearPlaceData();
        mPhotoMetaData.setLatLng(latLng);
    }


    @Override
    public void onPlaceSelect(Places o) {
        mPhotoMetaData.setConfirmedPlace(o);
        mPhotoMetaData.setAddressType(PhotoMetaData.ADDRESS_FROM_PLACESAPI);
        setResultAction(mPhotoMetaData);
    }

    @Override
    public void onPlaceCancel() {

    }


    private void deleteSavedFile() {
        savedUri = null;
        if (savedUri != null) PhotoCommonMethods.deleteFileFromUri(savedUri);
    }

    /* Google Location APIs Callback */

    private void initLocationClient() {

        mLoadingProgress.show();
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

    }

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
        } else {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            //PhotoMetaData metaData = new PhotoMetaData();
            mPhotoMetaData.setLatLng(latLng);

            PlacesTask placesTask = new PlacesTask(this, this);
            placesTask.execute(mPhotoMetaData);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationClient.removeLocationUpdates(this);

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //PhotoMetaData metaData = new PhotoMetaData();
        mPhotoMetaData.setLatLng(latLng);

        PlacesTask placesTask = new PlacesTask(this, this);
        placesTask.execute(mPhotoMetaData);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        mLoadingProgress.dismiss();

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
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    private void showDialogSetLocation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.location_set)
                .setMessage(R.string.location_set_text)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {

                        LocationManager manager = (LocationManager) ResultActivity.this.getSystemService(Context.LOCATION_SERVICE);

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

    private void showDialogTurnOnGPS() {

        new AlertDialog.Builder(this)
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

    private MarkerMapFragment getMapFragment() {
        MarkerMapFragment markerMapFragment = (MarkerMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        return markerMapFragment;
    }

    /**
     *
     * ------------------------------------------------------------
     * Capture ImageView & MapView, and Composing, Write File.
     * ------------------------------------------------------------
     *
     */

    private void saveAndShare() {

        if (mPhotoMetaData == null) {
            Toast.makeText(this, R.string.choose_img_plz, Toast.LENGTH_LONG).show();
            return;
        }

        if (savedUri != null) {
            PhotoCommonMethods.sharePhotoFromUri(this, savedUri, mPhotoMetaData);
        } else {

            mLoadingProgress.show();

            MarkerMapFragment mapFragment = getMapFragment();

            mapFragment.requestSnapShot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {

                    new CaptureBitmapTask(ResultActivity.this, ResultActivity.this).execute(mPhotoMetaData, bitmap, mCaptureView);
                }
            });
        }
    }

    @Override
    public void onSaveFinished(File savedFile) {

        mLoadingProgress.dismiss();

        if (savedFile != null) {
            Uri fileUri = Uri.fromFile(savedFile);
            savedUri = fileUri;
            PhotoCommonMethods.sharePhotoFromUri(ResultActivity.this, fileUri, mPhotoMetaData);
        }

    }


}
