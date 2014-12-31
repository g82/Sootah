package com.gamepari.sootah;

import android.app.Activity;
import android.widget.Toast;

import com.gamepari.sootah.images.PhotoMetaData;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by seokceed on 2014-11-29.
 */
public class MarkerMapFragment extends SupportMapFragment implements
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMapLoadedCallback {

    private Marker marker;
    private MapLongClickedListener mapLongClickListener;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mapLongClickListener = (MapLongClickedListener) activity;
    }

    @Override
    public void onMapLoaded() {
        Toast.makeText(getActivity(), R.string.pin_move, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getMap() != null) {
            getMap().setOnMapLongClickListener(this);
            getMap().setOnCameraChangeListener((GoogleMap.OnCameraChangeListener) getActivity());
            getMap().setOnMapLoadedCallback(this);
        }

    }

    public void requestSnapShot(GoogleMap.SnapshotReadyCallback snapshotReadyCallback) {
        getMap().snapshot(snapshotReadyCallback);
    }

    public void highlightMap(final PhotoMetaData photoMetaData) {

        final LatLng latLng = photoMetaData.getLatLng();

        if (marker != null) {
            marker.remove();
        }

        marker = getMap().addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .draggable(true)
        );

        CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(marker.getPosition(), 16.f);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        getMap().animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {
            }

        });

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mapLongClickListener.onMarkerPositionChanged(latLng);
    }

    /*
    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(final Marker marker) {

        //mapLongClickListener.onMarkerPositionChanged(marker.getPosition());
    }
    */

    public interface MapLongClickedListener {

        public void onMarkerPositionChanged(LatLng latLng);

    }

}
