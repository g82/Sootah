package com.gamepari.sootah;

import android.location.Address;
import android.widget.Toast;

import com.gamepari.sootah.images.PhotoMetaData;
import com.gamepari.sootah.location.Places;
import com.gamepari.sootah.location.PlacesTask;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

/**
 * Created by seokceed on 2014-11-29.
 */
public class MarkerMapFragment extends SupportMapFragment implements
        GoogleMap.OnMarkerDragListener {

    private Marker marker;
    private PhotoMetaData mPhotoMetaData;
    private ResultActivity.OnMapPinMovedListener mapPinMovedListener;


    public void setMapPinMovedListener(ResultActivity.OnMapPinMovedListener mapPinMovedListener) {
        this.mapPinMovedListener = mapPinMovedListener;
    }

    @Override
    public void onStart() {
        super.onStart();
        getMap().setOnMarkerDragListener(this);
    }

    public void requestSnapShot(GoogleMap.SnapshotReadyCallback snapshotReadyCallback) {
        getMap().snapshot(snapshotReadyCallback);
    }

    public void highlightMapFromLatLng(PhotoMetaData photoMetaData) {

        mPhotoMetaData = photoMetaData;

        final LatLng latLng = mPhotoMetaData.getLatLng();

        CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(latLng, 16.f);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        getMap().animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

                if (marker != null) {
                    marker.remove();
                }

                marker = getMap().addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .draggable(true)
                );

                if (mapPinMovedListener!=null) mapPinMovedListener.onPinMoved(mPhotoMetaData);

            }

            @Override
            public void onCancel() {}

        });

        Toast.makeText(getActivity(), R.string.drag_pin_plz, Toast.LENGTH_LONG).show();

    }

    private String makeTitleText(Address address) {

        String addrStr = address.getAdminArea();

        String name = address.getFeatureName();

        Locale currentLocale = Locale.getDefault();

        if (currentLocale.equals(Locale.KOREA)) {

            if (address.getLocality() != null) {
                addrStr += " " + address.getLocality();
                if (address.getThoroughfare() != null && !address.getThoroughfare().equals("Unnamed Rd")) {
                    addrStr += " " + address.getThoroughfare();
                    if (address.getSubThoroughfare() != null && !address.getSubThoroughfare().equals("Unnamed Rd")) {
                        addrStr += " " + address.getSubThoroughfare();
                    }
                }
            }
        }

        else {

            if (address.getLocality() != null) {
                addrStr = address.getLocality() + ", " + addrStr;
                if (address.getThoroughfare() != null && !address.getThoroughfare().equals("Unnamed Rd")) {
                    addrStr = address.getThoroughfare() + ", " + addrStr;
                    if (address.getSubThoroughfare() != null && !address.getSubThoroughfare().equals("Unnamed Rd")) {
                        addrStr = address.getSubThoroughfare() + " " + addrStr;
                    }
                }
            }

        }
        return addrStr;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.hideInfoWindow();
    }

    @Override
    public void onMarkerDrag(Marker marker) {}

    @Override
    public void onMarkerDragEnd(final Marker marker) {

        PlacesTask placesTask = new PlacesTask(getActivity(), new PlacesTask.OnPlaceTaskListener() {
            @Override
            public void onParseFinished(int addressType, PhotoMetaData photoMetaData) {

                mPhotoMetaData = photoMetaData;

                CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(marker.getPosition(), getMap().getCameraPosition().zoom);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

                getMap().animateCamera(cameraUpdate);

                if (mapPinMovedListener !=null) mapPinMovedListener.onPinMoved(mPhotoMetaData);

            }
        });

        placesTask.execute(mPhotoMetaData);

        ((ResultActivity)getActivity()).onMapMoved();
    }


}
