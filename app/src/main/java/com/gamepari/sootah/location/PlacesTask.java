package com.gamepari.sootah.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.gamepari.sootah.R;
import com.gamepari.sootah.images.PhotoMetaData;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by seokceed on 2014-11-30.
 *
 * https://maps.googleapis.com/maps/api/place/nearbysearch/json
 * ?location=36.9483674,127.9048024
 * &radius=30&sensor=true
 * &types=establishment
 * &language=en
 * &key=AIzaSyCPg-IE7BRkZyCRJP1R264JfSV3IijXsw0
 *
 *
 * first Google Places API.
 *
 * if failed api,
 *
 * second Google Geocoder.
 *
 */


public class PlacesTask extends AsyncTask<PhotoMetaData, Integer, PhotoMetaData> {

    private static final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final boolean USE_SENSOR = true;
    private static final int RADIUS = 30;
    private static final String TYPES = "establishment";

    private Context mContext;
    private OnPlaceTaskListener mOnPlaceTaskListener;

    public PlacesTask(Context context, OnPlaceTaskListener onPlaceTaskListener) {
        mContext = context;
        mOnPlaceTaskListener = onPlaceTaskListener;
    }

    @Override
    protected PhotoMetaData doInBackground(PhotoMetaData... photoMetaDatas) {

        PhotoMetaData photoMetaData = photoMetaDatas[0];

        LatLng latLng = photoMetaData.getLatLng();

        List<Places> placesList = getPlacesFromLocation(mContext, latLng);

        if (placesList != null && placesList.size() > 0) {
            photoMetaData.setAddressType(PhotoMetaData.ADDRESS_FROM_PLACESAPI);
            photoMetaData.setPlacesList(placesList);
        }

        Address address = getAddressFromLocation(mContext, latLng);

        if (address != null) {
            if (placesList == null) photoMetaData.setAddressType(PhotoMetaData.ADDRESS_FROM_GEOCODE);
            photoMetaData.setAddress(address);
        }
        else if (address == null && photoMetaData.getAddressType() != PhotoMetaData.ADDRESS_FROM_PLACESAPI) {
            return null;
        }

        return photoMetaData;
    }

    public static Address getAddressFromLocation(Context context, LatLng latLng) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                return address;
            }
            else return null;

        } catch (IOException e) {
            return null;
        }

    }

    public static List<Places> getPlacesFromLocation(Context context, LatLng latLng) {

        String urlString = PLACES_API_URL
                + "?location=" + latLng.latitude + "," + latLng.longitude
                + "&radius=" + RADIUS
                + "&sensor=" + USE_SENSOR
                + "&types=" + TYPES
                + "&language=" + Locale.getDefault().getLanguage()
                + "&key=" + context.getString(R.string.google_maps_api_key_debug);

        InputStream inputStream = null;

        try {
            inputStream = ParserUtil.downloadURL(urlString);
            String jsonString = ParserUtil.makeStringFromStream(inputStream);

            JSONObject jsonObject = new JSONObject(jsonString);

            String status = jsonObject.getString("status");

            if (status.equals("OK")) {

                List<Places> placesList = new ArrayList<>();

                //using Places API.

                JSONArray arrResults = jsonObject.getJSONArray("results");

                for (int i = 0; i < arrResults.length(); i++) {

                    JSONObject jsonResult = arrResults.getJSONObject(i);

                    JSONObject jsonGeometry = jsonResult.getJSONObject("geometry");
                    JSONObject jsonLocation = jsonGeometry.getJSONObject("location");

                    JSONArray jsonTypes = jsonResult.getJSONArray("types");

                    Places place = new Places();

                    place.setLocation(new LatLng(jsonLocation.getDouble("lat"), jsonLocation.getDouble("lng")));
                    place.setName(jsonResult.getString("name"));
                    place.setVicinity(jsonResult.getString("vicinity"));
                    place.setTypes(jsonTypes.toString().split(","));

                    placesList.add(place);

                }

                return placesList;

            }
            else {
                return null;
            }

        } catch (IOException e) {
            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(PhotoMetaData photoMetaData) {
        if (mOnPlaceTaskListener != null) mOnPlaceTaskListener.onParseFinished(photoMetaData.getAddressType(), photoMetaData);
    }

    public interface OnPlaceTaskListener {
        public void onParseFinished(int addressType, PhotoMetaData photoMetaData);
    }
}
