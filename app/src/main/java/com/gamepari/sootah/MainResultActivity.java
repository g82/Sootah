package com.gamepari.sootah;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import java.io.IOException;


public class MainResultActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener {

    private GoogleMap googleMap;

    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private View mCaptureView;

    private ProgressDialog progressDialog;
    private PhotoMetaData mPhotoMetaData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_result);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //turn on gps
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(i);
        }

        mLocationClient = new LocationClient(this, this, this);
        mLocationRequest = new LocationRequest();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);

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

        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        mLocationClient.disconnect();

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PhotoCommonMethods.REQ_CAMERA || requestCode == PhotoCommonMethods.REQ_GALLERY) {

            if (resultCode == RESULT_OK) {

                LatLng latLng = null;

                try {

                    mPhotoMetaData = PhotoCommonMethods.getMetaDataFromURI(this, requestCode, data.getData());
                    latLng = mPhotoMetaData.getLatLng();

                } catch (Exception e) {
                    if (mLocationClient.isConnected()) {

                        Location currentLocation = mLocationClient.getLastLocation();

                        if (currentLocation != null) {
                            double lat = currentLocation.getLatitude();
                            double lng = currentLocation.getLongitude();
                            latLng = new LatLng(lat, lng);
                        }
                    }
                } finally {
                    setResultAction(mPhotoMetaData);
                }

            }

        } else if (requestCode == GooglePlayServices.CONNECTION_FAILURE_RESOLUTION_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Try the request again.
            }
        }
    }

    private void setResultAction(PhotoMetaData photoMetaData) {
        LatLng latLng = photoMetaData.getLatLng();

        if (latLng != null) {

            CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(latLng, 16.f);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.animateCamera(cameraUpdate);

            PlaceholderFragment placeholderFragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentById(R.id.container);
            placeholderFragment.setImage(photoMetaData);
        }
    }

    /* Google Location APIs Callback */

    @Override
    public void onConnected(Bundle bundle) {
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        Location location = mLocationClient.getLastLocation();

        if (location == null) mLocationClient.requestLocationUpdates(mLocationRequest, this);

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
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLocationClient.removeLocationUpdates(this);
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

        progressDialog = ProgressDialog.show(MainResultActivity.this, null,"Saving...");

        googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                new ComposeBitmapTask().execute(mPhotoMetaData, bitmap);
            }
        });
    }

    private class ComposeBitmapTask extends AsyncTask<Object, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            PhotoMetaData photoMetaData = (PhotoMetaData) params[0];
            Bitmap mapBitmap = (Bitmap) params[1];

            Bitmap mainBitmap = PhotoCommonMethods.bitmapFromView(mCaptureView);
            Bitmap resultBitmap = BitmapCompose.composeBitmap(mainBitmap, mapBitmap, photoMetaData);

            boolean isSuccess = false;
            try {
                isSuccess = PhotoCommonMethods.saveImageFromBitmap(resultBitmap);
            } catch (IOException e) {
                e.printStackTrace();
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
                ivPhoto.setImageBitmap(bitmap);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
