package com.intuit.in24hr.namma_bmtc;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.in24hr.namma_bmtc.types.Location;
import com.intuit.in24hr.namma_bmtc.types.ReferenceToken;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.SerializableEntityHC4;
import org.apache.http.entity.StringEntityHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import static com.intuit.in24hr.namma_bmtc.Constants.*;
import static com.intuit.in24hr.namma_bmtc.ServiceHelper.convertInputStreamToString;

public class LocationShareService extends IntentService {

    ObjectMapper objectMapper = new ObjectMapper();

    public LocationShareService() {
        super("LocationShareService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Location location = (Location) workIntent.getSerializableExtra("location");
        final ResultReceiver receiver = workIntent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();
        try {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            String refToken = saveLocationOfUser(location);
            bundle.putString("refToken", refToken);
            receiver.send(STATUS_FINISHED, bundle);
        } catch (Exception e) {
            e.printStackTrace();
            bundle.putString(Intent.EXTRA_TEXT, "Oops!! having connection problems..");
            receiver.send(STATUS_ERROR, bundle);
        }
    }
    //if refToken is null then insert else update
    private String saveLocationOfUser(Location location) throws Exception {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        /* forming th java.net.URL object */
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
            //todo get values from UI and pass along
            URI uri = new URIBuilder("http://" + Constants.SERVER_HOSTNAME +"/test/v1/location").build();
            HttpPostHC4 httpPostHC4 = new HttpPostHC4(uri);
            httpPostHC4.addHeader("Content-Type", "application/json");
            httpPostHC4.addHeader("Accept", "application/json");
            httpPostHC4.setEntity(new StringEntityHC4(objectMapper.writeValueAsString(location), "UTF-8"));

            CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpPostHC4);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
//            httpPostHC4.setEntity(new Location(location[0], location[1]));
            if (statusCode == 200) {
                inputStream = new BufferedInputStream(httpResponse.getEntity().getContent());
                String response = convertInputStreamToString(inputStream);
                ReferenceToken referenceToken = objectMapper.readValue(response, ReferenceToken.class);
                return referenceToken.getRefToken();
            } else {
                throw new Exception("Failed to save!");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;

        }
        /* 200 represents HTTP OK */
    }
}
