package com.gamepari.sootah;

import android.location.Address;

import com.gamepari.sootah.images.PhotoMetaData;
import com.gamepari.sootah.location.GeoCodeTask;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

/**
 * Created by seokceed on 2014-11-29.
 */
public class MarkerMapFragment extends SupportMapFragment implements GoogleMap.OnMarkerDragListener {

    @Override
    public void onStart() {
        super.onStart();
        getMap().setOnMarkerDragListener(this);
    }

    public void requestSnapShot(GoogleMap.SnapshotReadyCallback snapshotReadyCallback) {
        getMap().snapshot(snapshotReadyCallback);
    }

    private Marker marker;

    public void highlightMapFromLatLng(final PhotoMetaData photoMetaData) {

        final LatLng latLng = photoMetaData.getLatLng();

        final Address address = photoMetaData.getAddress();


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
                                .title(makeTitleText(address))
                                .draggable(true)
                );

                marker.showInfoWindow();

            }

            @Override
            public void onCancel() {}

        });

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

        GeoCodeTask geoCodeTask = new GeoCodeTask(getActivity(), new GeoCodeTask.OnTaskFinshListener() {
            @Override
            public void onTaskFinish(Address address) {

                CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(marker.getPosition(), getMap().getCameraPosition().zoom);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

                getMap().animateCamera(cameraUpdate);

                if (address != null) {
                    marker.setTitle(makeTitleText(address));
                }
                marker.showInfoWindow();

            }
        });
        geoCodeTask.execute(marker.getPosition());

        ((MainResultActivity)getActivity()).onMapMoved();
    }


}
