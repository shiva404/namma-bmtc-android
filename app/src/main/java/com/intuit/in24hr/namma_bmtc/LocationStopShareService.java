package com.intuit.in24hr.namma_bmtc;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.in24hr.namma_bmtc.types.Location;
import com.intuit.in24hr.namma_bmtc.types.ReferenceToken;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDeleteHC4;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntityHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_ERROR;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_FINISHED;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_RUNNING;
import static com.intuit.in24hr.namma_bmtc.ServiceHelper.convertInputStreamToString;


public class LocationStopShareService extends IntentService {

    public LocationStopShareService() {
        super("LocationStopShareService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        String refToken = workIntent.getStringExtra("refToken");
        final ResultReceiver receiver = workIntent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();
        System.out.println("Stopping "+refToken);
        try {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            removeLocationOfUser(refToken);
            receiver.send(STATUS_FINISHED, bundle);
        } catch (Exception e) {
            e.printStackTrace();
            bundle.putString(Intent.EXTRA_TEXT, "Oops!! having connection problems..");
            receiver.send(STATUS_ERROR, bundle);
        }
    }
    //if refToken is null then insert else update
    private void removeLocationOfUser(String refToken) throws Exception {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        /* forming th java.net.URL object */
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
            //todo get values from UI and pass along
            URI uri = new URIBuilder("http://" + Constants.SERVER_HOSTNAME +"/test/v1/location/" + refToken).build();
            HttpDeleteHC4 httpDeleteHC4 = new HttpDeleteHC4(uri);
            httpDeleteHC4.addHeader("Content-Type", "application/json");
            httpDeleteHC4.addHeader("Accept", "application/json");
            System.out.println("Firing delete ");
            CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpDeleteHC4);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
//            httpDeleteHC4.setEntity(new Location(location[0], location[1]));
            if (statusCode != 200) {

                throw new Exception("Failed to save!");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        /* 200 represents HTTP OK */
    }

}
