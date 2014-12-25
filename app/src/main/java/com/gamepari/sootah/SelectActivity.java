package com.gamepari.sootah;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.gamepari.sootah.ad.LocaleAd;
import com.gamepari.sootah.ad.OnActivityListener;
import com.gamepari.sootah.images.PhotoCommonMethods;

import java.util.Locale;

/**
 * Created by seokceed on 2014-11-30.
 */
public class SelectActivity extends ActionBarActivity implements View.OnClickListener {

    private OnActivityListener onActivityListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#DE5553")));

        findViewById(R.id.select_album).setOnClickListener(this);
        findViewById(R.id.select_camera).setOnClickListener(this);

        //TODO ADD ADBANNER
        onActivityListener = new LocaleAd(this, Locale.getDefault(), LocaleAd.AD_BANNER);

    }

    @Override
    protected void onResume() {
        super.onResume();
        onActivityListener.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onActivityListener.onActivityPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onActivityListener.onActivityDestroy();
    }

    private void requestImage(int requestCode) {

        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("requestCode", requestCode);

        startActivity(i);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_album:
                requestImage(PhotoCommonMethods.REQ_GALLERY);
                break;

            case R.id.select_camera:
                requestImage(PhotoCommonMethods.REQ_CAMERA);
                break;
        }
    }
}
