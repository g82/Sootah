package com.gamepari.sootah.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by seokceed on 2014-11-29.
 */
public class GeoCodeTask extends AsyncTask<LatLng, Integer, Address> {

    private OnTaskFinshListener mOnTaskFinshListener;
    private Context mContext;

    public GeoCodeTask(Context context, OnTaskFinshListener onTaskFinshListener) {
        mOnTaskFinshListener = onTaskFinshListener;
        mContext = context;
    }

    @Override
    protected Address doInBackground(LatLng... latLngs) {
        try {
            Address address = getAddressFromLocation(mContext, latLngs[0]);
            return (address != null)? address : null;
        } catch (IOException e) {
            return null;
        }

    }

    @Override
    protected void onPostExecute(Address address) {

        if (mOnTaskFinshListener != null) {
            mOnTaskFinshListener.onTaskFinish(address);
        }

    }

    public static Address getAddressFromLocation(Context context, LatLng latLng) throws IOException {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;

        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            return address;
        }
        return null;
    }

    public static interface OnTaskFinshListener {
        public void onTaskFinish(Address address);
    }
}
