package com.gamepari.sootah;

/**
 * Created by gamepari on 12/2/14.
 */

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gamepari.sootah.images.BitmapCompose;
import com.gamepari.sootah.images.PhotoCommonMethods;
import com.gamepari.sootah.images.PhotoMetaData;

/**
 * A placeholder fragment containing a simple view.
 */
public class ImageFragment extends Fragment {

    private ImageView ivPhoto;
    private TextView tvTitle, tvAddress;

    public ImageFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_result_image, container, false);

        ivPhoto = (ImageView) rootView.findViewById(R.id.image);
        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        tvAddress = (TextView) rootView.findViewById(R.id.tv_address);

        return rootView;
    }

    public void setImage(PhotoMetaData photoMetaData) {

        new AdjustBitmapTask().execute(photoMetaData);

        if (photoMetaData.getAddressType() == PhotoMetaData.ADDRESS_FROM_PLACESAPI && photoMetaData.getConfirmedPlace() != null) {

            tvTitle.setText(photoMetaData.getConfirmedPlace().getName());
            tvAddress.setText(photoMetaData.getConfirmedPlace().getVicinity());

        }
    }

    private class AdjustBitmapTask extends AsyncTask<PhotoMetaData, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(PhotoMetaData... metaDatas) {
            Bitmap resultBitmap = BitmapCompose.adjustBitmap(metaDatas[0]);
            return resultBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (ivPhoto.getDrawable() != null) {

                Bitmap prevBitmap = ((BitmapDrawable) (ivPhoto.getDrawable())).getBitmap();

                if (prevBitmap != null) {
                    ivPhoto.setImageBitmap(null);
                    PhotoCommonMethods.recycleBitmap(prevBitmap);
                }

            }

            ivPhoto.setImageBitmap(bitmap);
        }
    }

}
