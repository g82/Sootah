package com.gamepari.sootah.images;

import android.app.Activity;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
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
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + PREFIX + timeStamp + ".png");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + PREFIX + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static LatLng getLatLngFromImage(String filePath) throws IOException, NoSuchFieldException {

        ExifInterface exif = new ExifInterface(filePath);

        float[] output = {0.f, 0.f};

        if (exif.getLatLong(output)) {
            LatLng latLng = new LatLng(output[0],output[1]);
            return latLng;
        }
        else {
            throw new NoSuchFieldException("no data");
        }
    }

}
