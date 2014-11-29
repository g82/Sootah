package com.gamepari.sootah.location;

import android.content.Context;
import android.os.AsyncTask;

import com.gamepari.sootah.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by seokceed on 2014-11-30.
 *
 * https://maps.googleapis.com/maps/api/place/nearbysearch/json
 * ?location=36.9483674,127.9048024
 * &radius=30&sensor=true
 * &types=establishment
 * &key=AIzaSyCPg-IE7BRkZyCRJP1R264JfSV3IijXsw0
 *
 */
public class PlacesTask extends AsyncTask<LatLng, Integer, List<Places>> {

    private static final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final boolean USE_SENSOR = true;
    private static final int RADIUS = 30;
    private static final String TYPES = "establishment";
    private String api_key = null;

    public PlacesTask(Context context) {
        api_key = context.getString(R.string.google_maps_api_key_debug);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Places> doInBackground(LatLng... latLngs) {

        LatLng latLng = latLngs[0];

        String urlString = PLACES_API_URL
                + "?location=" + latLng.latitude + "," + latLng.longitude
                + "&radius=" + RADIUS
                + "&sensor=" + USE_SENSOR
                + "&types=" + TYPES
                + "&key=" + api_key;

        InputStream inputStream = null;

        try {
            inputStream = ParserUtil.downloadURL(urlString);
            String jsonString = ParserUtil.makeStringFromStream(inputStream);

            List<Places> placesList = new ArrayList<Places>();

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray arrResults = jsonObject.getJSONArray("results");

            JSONObject geometry = arrResults.getJSONObject(0);
            JSONObject location = geometry.getJSONObject("location");





            return placesList;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Places> placeses) {
        super.onPostExecute(placeses);
    }
}
