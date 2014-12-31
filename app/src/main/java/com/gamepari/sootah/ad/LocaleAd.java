package com.gamepari.sootah.ad;

import android.app.Activity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.gamepari.sootah.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Locale;

/**
 * Created by seokceed on 2014-12-04.
 */
public class LocaleAd implements OnActivityListener {

    public static final int AD_FULLSCREEN = 324312;
    public static final int AD_BANNER = 345432;


    Locale userLocale;
    Activity mActivity;

//    private CaulyCloseAd mCaulyCloseAd;
//    private CaulyAdView mCaulyAdView;

    private InterstitialAd mInterstitialAd;
    private AdView mAdView;

    public LocaleAd(Activity activity, Locale locale, int adType) {

        userLocale = locale;
        mActivity = activity;

        if (userLocale.equals(Locale.KOREA)) {

            /*
            CaulyAdInfo caulyAdInfo = new CaulyAdInfoBuilder(activity.getString(R.string.cauly_key))
                    .allowcall(false)
                    .build();

            if (adType == AD_FULLSCREEN) {
                mCaulyCloseAd = new CaulyCloseAd();
                mCaulyCloseAd.setAdInfo(caulyAdInfo);
                mCaulyCloseAd.setDescriptionText(activity.getString(R.string.question_app_finish));
                mCaulyCloseAd.setCloseAdListener(this);
                mCaulyCloseAd.disableBackKey();

            } else if (adType == AD_BANNER) {

                mCaulyAdView = new CaulyAdView(mActivity);
                mCaulyAdView.setAdInfo(caulyAdInfo);
                FrameLayout frameLayout = (FrameLayout) activity.findViewById(R.id.fl_select_main);
                frameLayout.addView(mCaulyAdView);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                mCaulyAdView.setLayoutParams(params);
            }*/

            AdRequest adRequest = new AdRequest.Builder()
                    .build();

            if (adType == AD_FULLSCREEN) {
                mInterstitialAd = new InterstitialAd(activity);
                mInterstitialAd.setAdUnitId(activity.getString(R.string.admob_insert_key));
                mInterstitialAd.loadAd(adRequest);
            } else if (adType == AD_BANNER) {
                mAdView = new AdView(mActivity);
                mAdView.setAdUnitId(activity.getString(R.string.admob_banner_key));
                mAdView.setAdSize(AdSize.SMART_BANNER);
                FrameLayout frameLayout = (FrameLayout) activity.findViewById(R.id.fl_select_main);
                frameLayout.addView(mAdView);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                mAdView.setLayoutParams(params);
                mAdView.loadAd(adRequest);
            }


        } else {

            AdRequest adRequest = new AdRequest.Builder()
                    .build();

            if (adType == AD_FULLSCREEN) {
                mInterstitialAd = new InterstitialAd(activity);
                mInterstitialAd.setAdUnitId(activity.getString(R.string.admob_insert_key));
                mInterstitialAd.loadAd(adRequest);
            } else if (adType == AD_BANNER) {
                mAdView = new AdView(mActivity);
                mAdView.setAdUnitId(activity.getString(R.string.admob_banner_key));
                mAdView.setAdSize(AdSize.SMART_BANNER);
                FrameLayout frameLayout = (FrameLayout) activity.findViewById(R.id.fl_select_main);
                frameLayout.addView(mAdView);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                mAdView.setLayoutParams(params);
                mAdView.loadAd(adRequest);
            }

        }

    }

    public void showFullScreenAd() {

        if (userLocale.equals(Locale.KOREA)) {

            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }

//            if (mCaulyCloseAd.isModuleLoaded()) {
//                mCaulyCloseAd.show(mActivity);
//            }
        } else {

            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }

        }
    }

    @Override
    public void onActivityPause() {

        if (mAdView != null) {
            mAdView.pause();
        }

    }

    @Override
    public void onActivityDestroy() {

        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    @Override
    public void onActivityResume() {

        if (mAdView != null) mAdView.resume();

//        if (mCaulyCloseAd != null) {
//            mCaulyCloseAd.resume(mActivity);
//        } else if (mAdView != null) {
//            mAdView.resume();
//        }
    }

    /*
    @Override
    public void onReceiveCloseAd(CaulyCloseAd caulyCloseAd, boolean b) {
    }

    @Override
    public void onShowedCloseAd(CaulyCloseAd caulyCloseAd, boolean b) {
    }

    @Override
    public void onFailedToReceiveCloseAd(CaulyCloseAd caulyCloseAd, int i, String s) {
    }

    @Override
    public void onLeftClicked(CaulyCloseAd caulyCloseAd) {
    }

    @Override
    public void onRightClicked(CaulyCloseAd caulyCloseAd) {
    }

    @Override
    public void onLeaveCloseAd(CaulyCloseAd caulyCloseAd) {
    }
    */
}
