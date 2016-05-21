package com.intuit.in24hr.namma_bmtc;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.in24hr.namma_bmtc.types.LocationPage;
import com.intuit.in24hr.namma_bmtc.types.ReferenceToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_ERROR;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_FINISHED;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_RUNNING;
import static com.intuit.in24hr.namma_bmtc.ServiceHelper.convertInputStreamToString;

/**
 *
 */
public class LocationPullService extends IntentService {


    ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public LocationPullService() {
        super("LocationPull");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        double[] location = workIntent.getDoubleArrayExtra("location");
        final ResultReceiver receiver = workIntent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();
        try {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            LocationPage locationPage = getLocationPage(location);
            bundle.putSerializable("locations", locationPage);
            receiver.send(STATUS_FINISHED, bundle);
        } catch (Exception e) {
            e.printStackTrace();
            bundle.putString(Intent.EXTRA_TEXT, "Oops!! Something went wrong!!");
            receiver.send(STATUS_ERROR, bundle);
        }
    }

    private LocationPage getLocationPage(double[] location) throws Exception {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        /* forming th java.net.URL object */

        URL url = new URL("http://" + Constants.SERVER_HOSTNAME + "/test/v1/location?lat=" + location[0] + "&long="+ location[1]);
        urlConnection = (HttpURLConnection) url.openConnection();

        /* optional request header */
        urlConnection.setRequestProperty("Content-Type", "application/json");

        /* optional request header */
        urlConnection.setRequestProperty("Accept", "application/json");

        /* for Get request */
        urlConnection.setRequestMethod("GET");
        int statusCode = urlConnection.getResponseCode();

        /* 200 represents HTTP OK */
        if (statusCode == 200) {
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            String response = convertInputStreamToString(inputStream);
            return objectMapper.readValue(response, LocationPage.class);

        } else {
            throw new Exception("Failed to fetch data!!");
        }
    }
}
