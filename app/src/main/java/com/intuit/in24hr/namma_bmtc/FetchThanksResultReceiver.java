package com.intuit.in24hr.namma_bmtc;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

/**
 * Created by sn1 on 7/7/17.
 */

public class FetchThanksResultReceiver extends ResultReceiver {
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public FetchThanksResultReceiver(Handler handler) {
        super(handler);
    }

    private Receiver mReceiver;

    public interface Receiver {
        void onFetchThanksCountResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onFetchThanksCountResult(resultCode, resultData);
        }
    }
}
