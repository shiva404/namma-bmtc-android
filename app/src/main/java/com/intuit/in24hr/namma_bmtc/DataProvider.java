package com.intuit.in24hr.namma_bmtc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.in24hr.namma_bmtc.types.BusRoute;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDeleteHC4;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import static com.intuit.in24hr.namma_bmtc.ServiceHelper.convertInputStreamToString;

/**
 * Created by sn1 on 5/20/16.
 */
public class DataProvider {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static BusRoute getBusRoute(String routeNumber) throws Exception {
        InputStream inputStream = null;
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
            //todo get values from UI and pass along
            URI uri = new URIBuilder("http://" + Constants.SERVER_HOSTNAME +"/test/v1/busroute/" + routeNumber).build();
            HttpGetHC4 httpGetHC4 = new HttpGetHC4(uri);
            httpGetHC4.addHeader("Content-Type", "application/json");
            httpGetHC4.addHeader("Accept", "application/json");
            CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpGetHC4);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                inputStream = httpResponse.getEntity().getContent();
                String response = convertInputStreamToString(inputStream);
                return objectMapper.readValue(response, BusRoute.class);
            } else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}
