package com.gamepari.sootah.location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by seokceed on 2014-11-30.
 */
public class Places {

    private String status;
    private LatLng location;
    private String name;
    private String[] types;
    private String vicinity;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
}
