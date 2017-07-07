package com.intuit.in24hr.namma_bmtc;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.in24hr.namma_bmtc.types.LocationPage;
import com.intuit.in24hr.namma_bmtc.types.ThanksCount;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_ERROR;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_FINISHED;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_RUNNING;
import static com.intuit.in24hr.namma_bmtc.ServiceHelper.convertInputStreamToString;

/**
 * Created by sn1 on 7/7/17.
 */

public class FetchThanksCountService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */

    public FetchThanksCountService() {
        super("FetchThanksCountService");
    }

    public FetchThanksCountService(String name) {
        super("FetchThanksCountService");
    }

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void onHandleIntent(@Nullable Intent workIntent) {
        String refToken = workIntent.getStringExtra("refToken");
        final ResultReceiver receiver = workIntent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();
        System.out.println("Fetching thanks count-- for "+refToken);
        try {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            ThanksCount thanksCount = getThanksCount(refToken);
            bundle.putSerializable("thanksCount", thanksCount);
            receiver.send(STATUS_FINISHED, bundle);
        } catch (Exception e) {
            e.printStackTrace();
            bundle.putString(Intent.EXTRA_TEXT, "Oops!! Something went wrong!!");
            receiver.send(STATUS_ERROR, bundle);
        }
    }

    private ThanksCount getThanksCount(String refToken) throws Exception {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        /* forming th java.net.URL object */

        URL url = new URL("http://" + Constants.SERVER_HOSTNAME + "/test/v1/location/" + refToken +"/thanks");
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
            return objectMapper.readValue(response, ThanksCount.class);
        } else {
            throw new Exception("Failed to fetch data!!");
        }
    }
}
