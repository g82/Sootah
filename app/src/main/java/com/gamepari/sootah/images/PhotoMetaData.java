package com.gamepari.sootah.images;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by seokceed on 2014-11-23.
 */
public class PhotoMetaData {

    private LatLng latLng;
    private int orientation_degree;
    private String filePath;

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
}
