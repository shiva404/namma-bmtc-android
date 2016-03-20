package com.intuit.in24hr.namma_bmtc;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.intuit.in24hr.namma_bmtc.types.Location;
import com.intuit.in24hr.namma_bmtc.types.LocationPage;
import com.intuit.in24hr.namma_bmtc.types.ReferenceToken;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.HttpEntityWrapperHC4;
import org.apache.http.entity.SerializableEntityHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import static com.intuit.in24hr.namma_bmtc.Constants.*;
import static com.intuit.in24hr.namma_bmtc.ServiceHelper.convertInputStreamToString;

public class LocationUpdateService extends IntentService {

    ObjectMapper objectMapper = new ObjectMapper();

    public LocationUpdateService() {
        super("LocationUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Location location = (Location) workIntent.getSerializableExtra("location");
        final ResultReceiver receiver = workIntent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();
        try {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            String refToken = getLocationPage(location);
            bundle.putString("refToken", refToken);
            receiver.send(STATUS_FINISHED, bundle);
        } catch (Exception e) {
            e.printStackTrace();
            bundle.putString(Intent.EXTRA_TEXT, "Oops!! Something went wrong!!");
            receiver.send(STATUS_ERROR, bundle);
        }
    }

    private String getLocationPage(Location location) throws Exception {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        /* forming th java.net.URL object */
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
            //todo get values from UI and pass along
            URI uri = new URIBuilder("http://lb-1816851115.ap-southeast-1.elb.amazonaws.com/test/v1/location").build();
            HttpPostHC4 httpPostHC4 = new HttpPostHC4(uri);
            httpPostHC4.addHeader("Content-Type", "application/json");
            httpPostHC4.addHeader("Accept", "application/json");
            httpPostHC4.setEntity(new SerializableEntityHC4(location));

            CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpPostHC4);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
//            httpPostHC4.setEntity(new Location(location[0], location[1]));
            if (statusCode == 200) {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);
                ReferenceToken referenceToken = objectMapper.readValue(response, ReferenceToken.class);
                return referenceToken.getReferenceToken();
            } else {
                throw new Exception("Failed to fetch data!!");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;

        }
        /* 200 represents HTTP OK */

    }
}
