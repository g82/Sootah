package com.gamepari.sootah.images;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by seokceed on 2014-11-22.
 */
public class PhotoCommonMethods {

    public static final int REQ_GALLERY = 10000;
    public static final int REQ_CAMERA = 10100;

    public static final int MEDIA_TYPE_IMAGE = 20001;
    public static final int MEDIA_TYPE_VIDEO = 20002;

    private static final String PREFIX = "Sootah_";
    private static final String DIRECTORY_NAME = "Sootah";

    public static Uri CAMERA_URI = Uri.EMPTY;

    /*
    gallery
    return (Intent) data.getDataString();
    content://media/external/images/media/12446

    camera
    return CAMERA_URI file:///storage/emulated/0/Sootah/Sootah_20141122_214427.png
     */

    public static void photoFromGallery(Activity activity, String chooserTitle) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(Intent.createChooser(intent, chooserTitle), REQ_GALLERY);
    }

    public static void photoFromCamera(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        CAMERA_URI = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, CAMERA_URI);
        activity.startActivityForResult(intent, REQ_CAMERA);
    }

    public static void sharePhotoFromUri(Activity activity, Uri fileUri, PhotoMetaData photoMetaData) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, photoMetaData.getAddressString());
        shareIntent.setType("image/*");
        activity.startActivity(Intent.createChooser(shareIntent, "choose"));
    }

    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("file error", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timeStamp = format.format(new Date());

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + PREFIX + timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + PREFIX + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private static String getFilePathFromURI(Context context, Uri uri) throws IllegalStateException{
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            cursor.close();
            return filePath;
        }
        else {
            throw new IllegalStateException("Column does not exist.");
        }
    }

    public static boolean setMetaDataToFile(File bitmapFile, PhotoMetaData photoMetaData) {

        try {
            ExifInterface exif = new ExifInterface(bitmapFile.getPath());

            LatLng latLng = photoMetaData.getLatLng();

            int num1Lat = (int) Math.floor(latLng.latitude);
            int num2Lat = (int) Math.floor((latLng.latitude - num1Lat) * 60);
            double num3Lat = (latLng.latitude - ((double)num1Lat+((double)num2Lat/60))) * 3600000;

            int num1Lon = (int)Math.floor(latLng.longitude);
            int num2Lon = (int)Math.floor((latLng.longitude - num1Lon) * 60);
            double num3Lon = (latLng.longitude - ((double)num1Lon+((double)num2Lon/60))) * 3600000;

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat+"/1,"+num2Lat+"/1,"+num3Lat+"/1000");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon+"/1,"+num2Lon+"/1,"+num3Lon+"/1000");


            if (latLng.latitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            }

            if (latLng.longitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            }

            exif.saveAttributes();

            return true;

        } catch (IOException e) {
            return false;
        }
    }



    public static PhotoMetaData getMetaDataFromURI(Context context, int requestCode, Intent imageData) throws IOException {

        PhotoMetaData photoMetaData = null;
        String filePath = null;

        if (requestCode == REQ_CAMERA) filePath = CAMERA_URI.getPath();
        else if (requestCode == REQ_GALLERY) filePath = getFilePathFromURI(context, imageData.getData());

        ExifInterface exif = new ExifInterface(filePath);

        if (exif != null) {

            photoMetaData = new PhotoMetaData();
            photoMetaData.setFilePath(filePath);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

            int degree = 0;

            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

                photoMetaData.setOrientation_degree(degree);
            }

            float[] output = {0.f, 0.f};

            if (exif.getLatLong(output)) {
                LatLng latLng = new LatLng(output[0],output[1]);
                photoMetaData.setLatLng(latLng);
                return photoMetaData;
            }

            else {
                return photoMetaData;
            }
        }

        else {
            return photoMetaData;
        }
    }


    public static Bitmap bitmapFromView(View view) {

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap captureBitmap = view.getDrawingCache(true);

        return captureBitmap;
    }

    public static void recycleBitmap(Bitmap bitmap) {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
            bitmap.recycle();
        }
        else {
            bitmap = null;
        }
    }

    public static File saveImageFromBitmap(Bitmap bitmap) throws IOException {

        FileOutputStream fos;

        File bitmapFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

        fos = new FileOutputStream(bitmapFile);

        boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        fos.close();

        return isSuccess ? bitmapFile : null;
    }

    public static Bitmap setRotateBitmap(Bitmap bitmap, int degrees) {

        Bitmap rotatedBitmap = null;

        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float)bitmap.getWidth()/2, (float)bitmap.getHeight()/2);

            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

        }

        return rotatedBitmap;
    }



}
