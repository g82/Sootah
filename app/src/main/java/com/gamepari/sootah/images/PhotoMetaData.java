package com.gamepari.sootah.images;

import android.location.Address;
import android.location.Location;

import com.gamepari.sootah.location.Places;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by seokceed on 2014-11-23.
 */
public class PhotoMetaData {

    private LatLng latLng;
    private int orientation_degree;
    private String filePath;

    private Address address;

    private Places places;

    public Places getPlaces() {
        return places;
    }

    public void setPlaces(Places places) {
        this.places = places;
    }

    public int getOrientation_degree() {
        return orientation_degree;
    }

    public void setOrientation_degree(int orientation_degree) {
        this.orientation_degree = orientation_degree;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getAddressString() {

        String result = "";

        if (address != null) {

            result += address.getAdminArea() + " " + address.getLocality() + " "
                    + address.getThoroughfare() + " " + address.getSubThoroughfare();
        }

        return result;

    }


}
