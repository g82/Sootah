package com.gamepari.sootah.images;

/**
 * Created by gamepari on 12/2/14.
 */

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.AsyncTask;

import com.gamepari.sootah.location.GeoCodingTask;
import com.google.android.gms.maps.model.LatLng;

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

        try {
            metaData = PhotoCommonMethods.getMetaDataFromURI(mContext, requestCode, data);

            latLng = metaData.getLatLng();

            if (latLng != null) {
                //more insert place data.

                Address address = GeoCodingTask.getAddressFromLocation(mContext, latLng);

                if (address != null) {
                    metaData.setAddress(address);

                }
            }

        } catch (IllegalStateException e) {
            //camera photo not exist.
            return null;
        }

        return metaData;

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
