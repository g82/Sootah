package com.gamepari.sootah.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.gamepari.sootah.images.PhotoMetaData;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by seokceed on 2014-11-30.
 */


public class GeoCodingTask extends AsyncTask<PhotoMetaData, Integer, PhotoMetaData> {

    private Context mContext;
    private OnPlaceTaskListener mOnPlaceTaskListener;

    public GeoCodingTask(Context context, OnPlaceTaskListener onPlaceTaskListener) {
        mContext = context;
        mOnPlaceTaskListener = onPlaceTaskListener;
    }

    public static Address getAddressFromLocation(Context context, LatLng latLng) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                return address;
            } else return null;

        } catch (IOException e) {
            return null;
        }

    }

    @Override
    protected PhotoMetaData doInBackground(PhotoMetaData... photoMetaDatas) {

        PhotoMetaData photoMetaData = photoMetaDatas[0];

        LatLng latLng = photoMetaData.getLatLng();

        Address address = getAddressFromLocation(mContext, latLng);

        photoMetaData.setAddress(address);

        return photoMetaData;
    }

    @Override
    protected void onPostExecute(PhotoMetaData photoMetaData) {
        mOnPlaceTaskListener.onGeoCodingFinished(photoMetaData);
    }

    public interface OnPlaceTaskListener {
        public void onGeoCodingFinished(PhotoMetaData photoMetaData);
    }

}
