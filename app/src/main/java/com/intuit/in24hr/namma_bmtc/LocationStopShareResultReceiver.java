package com.intuit.in24hr.namma_bmtc;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

/**
 * Created by sn1 on 5/19/16.
 */
public class LocationStopShareResultReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public LocationStopShareResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        void onReceiveLStopLocationShareResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveLStopLocationShareResult(resultCode, resultData);
        }
    }
}
