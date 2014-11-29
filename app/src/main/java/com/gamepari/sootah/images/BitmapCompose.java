package com.gamepari.sootah.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by seokceed on 2014-11-23.
 */
public class BitmapCompose {

    public static final Bitmap composeBitmap(Bitmap mainBitmap, Bitmap mapBitmap, PhotoMetaData photoMetaData) {

        //overwrite on mainBitmap

        Canvas canvas = new Canvas(mainBitmap);

        int startMapX = 0;
        int startMapY = mainBitmap.getHeight() - mapBitmap.getHeight();

        Paint p = new Paint();

        canvas.drawBitmap(mapBitmap, (float)startMapX, (float)startMapY, p);

        return mainBitmap;
    }

    public static final Bitmap resizeBitmap(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = width / TARGET_WIDTH;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) (width / ratio), (int) (height / ratio), true);

        return scaledBitmap;
    }


    private static final float TARGET_WIDTH = 720.f;

    public static final Bitmap adjustBitmap(PhotoMetaData photoMetaData) {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;

        BitmapFactory.decodeFile(photoMetaData.getFilePath(), opts);

        int originalWidth = 0;
        if (photoMetaData.getOrientation_degree() == 90 || photoMetaData.getOrientation_degree() == 270) {
            originalWidth = opts.outHeight;
        }
        else originalWidth = opts.outWidth;

        float ratio = (float)originalWidth / TARGET_WIDTH;
        //TODO RATIO calculation
        opts.inSampleSize = (int)ratio;
        opts.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(photoMetaData.getFilePath(), opts);
        Bitmap rotatedBitmap = PhotoCommonMethods.setRotateBitmap(bitmap, photoMetaData.getOrientation_degree());

        if (rotatedBitmap != null) {
            PhotoCommonMethods.recycleBitmap(bitmap);
            return rotatedBitmap;
        }

        else {
            return bitmap;
        }
    }
}
