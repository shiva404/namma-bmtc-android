package com.intuit.in24hr.namma_bmtc;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

/**
 * Created by sn1 on 5/19/16.
 */
public class LocationUpdateResultReciever extends ResultReceiver {

    private Receiver mReceiver;

    public LocationUpdateResultReciever(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }


    public interface Receiver {
        void onReceiveUpdateLocationShareResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveUpdateLocationShareResult(resultCode, resultData);
        }
    }

}
