package com.intuit.in24hr.namma_bmtc;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.intuit.in24hr.namma_bmtc.types.BusRoute;

import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_ERROR;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_FINISHED;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_RUNNING;

/**
 * Created by sn1 on 5/21/16.
 */
public class BusDetailsService extends IntentService {

    public BusDetailsService() {
        super("BusDetailsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String busRoute = intent.getStringExtra("busRouteNo");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();
        try {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            BusRoute result = DataProvider.getBusRoute(busRoute);
            bundle.putSerializable("busRoute", result);
            receiver.send(STATUS_FINISHED, bundle);
        } catch (Exception e) {
            e.printStackTrace();
            bundle.putString(Intent.EXTRA_TEXT, "Oops!! having connection problems..");
            receiver.send(STATUS_ERROR, bundle);
        }
    }
}
