package com.intuit.in24hr.namma_bmtc;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

/**
 * Created by sn1 on 5/21/16.
 */
public class BusDetailsServiceResultReceiver extends ResultReceiver {
    private Receiver mReceiver;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public BusDetailsServiceResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        void onReceiveBusDetailsResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveBusDetailsResult(resultCode, resultData);
        }
    }
}
