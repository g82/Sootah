package com.gamepari.sootah;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.gamepari.sootah.images.PhotoCommonMethods;

/**
 * Created by seokceed on 2014-11-30.
 */
public class SelectActivity extends ActionBarActivity implements View.OnClickListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        findViewById(R.id.select_album).setOnClickListener(this);
        findViewById(R.id.select_camera).setOnClickListener(this);
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
