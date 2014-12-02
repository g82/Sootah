package com.gamepari.sootah.images;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

/**
 * Created by seokceed on 2014-11-23.
 */
public class PhotoMetaData {

    private int orientation_degree;
    private String filePath;

    private LatLng latLng;
    private Address address;

    private String placeName;
    private String addressText;

    public int getOrientation_degree() {
        return orientation_degree;
    }

    public void setOrientation_degree(int orientation_degree) {
        this.orientation_degree = orientation_degree;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getAddressText() {
        return addressText;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
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

    public String convertAddressString() {

        Locale userLocale = Locale.getDefault();
        String addressStr = address.getAdminArea();

        if (userLocale.equals(Locale.KOREA)) {

            if (address.getLocality() != null) {
                addressStr += " " + address.getLocality();
                if (address.getThoroughfare() != null && !address.getThoroughfare().equals("Unnamed Rd")) {
                    addressStr += " " + address.getThoroughfare();
                    if (address.getSubThoroughfare() != null && !address.getSubThoroughfare().equals("Unnamed Rd")) {
                        addressStr += " " + address.getSubThoroughfare();
                    }
                }
            }

        } else {

            if (address.getLocality() != null) {
                addressStr = address.getLocality() + ", " + addressStr;
                if (address.getThoroughfare() != null && !address.getThoroughfare().equals("Unnamed Rd")) {
                    addressStr = address.getThoroughfare() + ", " + addressStr;
                    if (address.getSubThoroughfare() != null && !address.getSubThoroughfare().equals("Unnamed Rd")) {
                        addressStr = address.getSubThoroughfare() + " " + addressStr;
                    }
                }
            }

        }

        return addressStr;
    }

    public void clearLocationData() {
        latLng = null;
        address = null;
        addressText = null;
    }

}
