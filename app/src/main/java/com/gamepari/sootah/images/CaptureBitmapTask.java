package com.gamepari.sootah.images;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;

/**
 * Created by gamepari on 12/2/14.
 */
public class CaptureBitmapTask extends AsyncTask<Object, Integer, File> {

    private OnSaveListener mOnSaveListener;
    private Context mContext;

    public CaptureBitmapTask(Context context, OnSaveListener onSaveListener) {
        mOnSaveListener = onSaveListener;
        mContext = context;
    }

    private void connectMediaScan(final String filePath) {

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected File doInBackground(Object... params) {

        PhotoMetaData photoMetaData = (PhotoMetaData) params[0];
        Bitmap mapBitmap = (Bitmap) params[1];
        View captureView = (View) params[2];

        Bitmap workBitmap = PhotoCommonMethods.bitmapFromView(captureView);
        ComposeBitmap.composeBitmap(workBitmap, mapBitmap, photoMetaData);
        Bitmap scaledBitmap = ComposeBitmap.resizeBitmap(workBitmap);

        File bitmapFile = null;
        boolean isSuccess = false;
        try {
            bitmapFile = PhotoCommonMethods.saveImageFromBitmap(scaledBitmap);
            if (bitmapFile != null) {

                isSuccess = PhotoCommonMethods.setMetaDataToFile(bitmapFile, photoMetaData);

                if (isSuccess) {
                    connectMediaScan(bitmapFile.getPath());
                } else {
                    bitmapFile.delete();
                }
            }

        } catch (IOException e) {
            Log.d(this.toString(), e.getMessage());
        } finally {
            captureView.setDrawingCacheEnabled(false);
            PhotoCommonMethods.recycleBitmap(mapBitmap);
            PhotoCommonMethods.recycleBitmap(workBitmap);
            PhotoCommonMethods.recycleBitmap(scaledBitmap);
        }

        return isSuccess ? bitmapFile : null;

    }

    @Override
    protected void onPostExecute(File file) {
        mOnSaveListener.onSaveFinished(file);
    }

    public static interface OnSaveListener {
        public void onSaveFinished(File savedFile);
    }
}
