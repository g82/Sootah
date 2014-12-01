package com.gamepari.sootah.images;

import android.location.Address;

import com.gamepari.sootah.location.Places;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by seokceed on 2014-11-23.
 */
public class PhotoMetaData {

    public static final int ADDRESS_FROM_PLACESAPI = 1234;
    public static final int ADDRESS_FROM_GEOCODE = 4543;

    private int addressType;

    private LatLng latLng;
    private int orientation_degree;
    private String filePath;

    private Address address;

    private List<Places> placesList;

    private Places confirmedPlace;

    public Places getConfirmedPlace() {
        return confirmedPlace;
    }

    public void setConfirmedPlace(Places confirmedPlace) {
        this.confirmedPlace = confirmedPlace;
    }

    public int getAddressType() {
        return addressType;
    }

    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }

    public List<Places> getPlacesList() {
        return placesList;
    }

    public void setPlacesList(List<Places> placesList) {
        this.placesList = placesList;
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
