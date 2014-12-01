package com.gamepari.sootah.images;

/**
 * Created by gamepari on 12/2/14.
 */

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;

import com.gamepari.sootah.location.Places;
import com.gamepari.sootah.location.PlacesTask;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Read MetaData exist Photo from SDCard
 */

public class MetaDataTask extends AsyncTask<Object, Integer, PhotoMetaData> {

    private OnMetaTaskListener mOnMetaTaskListener;
    private Context mContext;

    public MetaDataTask(Context context, OnMetaTaskListener mOnMetaTaskListener) {
        this.mOnMetaTaskListener = mOnMetaTaskListener;
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mOnMetaTaskListener.onMetaDataTaskStarted();
    }

    @Override
    protected PhotoMetaData doInBackground(Object... objects) {

        PhotoMetaData metaData = null;

        int requestCode = (Integer) (objects[0]);
        Intent data = (Intent) objects[1];

        LatLng latLng = null;

        // try extract metadata from image file.
        try {
            metaData = PhotoCommonMethods.getMetaDataFromURI(mContext, requestCode, data);
            if (metaData == null) return null;

            latLng = metaData.getLatLng();

            if (latLng != null) {
                //more insert place data.

                List<Places> placesList = PlacesTask.getPlacesFromLocation(mContext, latLng);

                if (placesList != null && placesList.size() > 0) {
                    metaData.setAddressType(PhotoMetaData.ADDRESS_FROM_PLACESAPI);
                    metaData.setPlacesList(placesList);
                }

                Address address = PlacesTask.getAddressFromLocation(mContext, latLng);

                if (address != null) {
                    if (metaData.getAddressType() != PhotoMetaData.ADDRESS_FROM_PLACESAPI) {
                        metaData.setAddressType(PhotoMetaData.ADDRESS_FROM_GEOCODE);
                    }
                    metaData.setAddress(address);
                }
            }

            return metaData;

        } catch (IOException e) {
            Log.d(this.toString(), e.getMessage());
            return null;
        } catch (IllegalStateException e) {
            return null;
        }

    }

    @Override
    protected void onPostExecute(PhotoMetaData metaData) {

        mOnMetaTaskListener.onMetaDataTaskFinished(metaData);

    }

    public interface OnMetaTaskListener {
        public void onMetaDataTaskStarted();

        public void onMetaDataTaskFinished(PhotoMetaData photoMetaData);
    }
}
