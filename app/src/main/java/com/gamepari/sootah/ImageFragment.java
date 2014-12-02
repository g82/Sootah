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

import com.gamepari.sootah.images.ComposeBitmap;
import com.gamepari.sootah.images.PhotoCommonMethods;
import com.gamepari.sootah.images.PhotoMetaData;

/**
 * A placeholder fragment containing a simple view.
 */
public class ImageFragment extends Fragment {

    private ImageView ivPhoto;
    private TextView tvPlace, tvAddress;
    private View llModify;

    public ImageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_result_image, container, false);

        ivPhoto = (ImageView) rootView.findViewById(R.id.image);
        tvPlace = (TextView) rootView.findViewById(R.id.tv_title);
        tvAddress = (TextView) rootView.findViewById(R.id.tv_address);
        llModify = rootView.findViewById(R.id.ll_modify);
        llModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputDialogFragment dialogFragment = new InputDialogFragment();
                dialogFragment.show(getFragmentManager(), "dialog");
            }
        });

        return rootView;
    }

    public void setImageAndText(PhotoMetaData photoMetaData) {

        if (photoMetaData.getPlaceName() != null && !photoMetaData.getPlaceName().equals("")) {
            tvPlace.setVisibility(View.VISIBLE);
            tvPlace.setText(photoMetaData.getPlaceName());
        } else {
            tvPlace.setVisibility(View.GONE);
        }

        if (photoMetaData.getAddressText() != null && !photoMetaData.getAddressText().equals("")) {
            tvAddress.setVisibility(View.VISIBLE);
            tvAddress.setText(photoMetaData.getAddressText());
        } else {
            tvAddress.setVisibility(View.GONE);
        }

        if (tvPlace.getVisibility() == View.GONE && tvAddress.getVisibility() == View.GONE) {
            llModify.setVisibility(View.GONE);
        } else {
            llModify.setVisibility(View.VISIBLE);
        }

        new AdjustBitmapTask().execute(photoMetaData);
    }

    private class AdjustBitmapTask extends AsyncTask<PhotoMetaData, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(PhotoMetaData... metaDatas) {
            Bitmap resultBitmap = ComposeBitmap.adjustBitmap(metaDatas[0]);
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
