package com.intuit.in24hr.namma_bmtc;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;

/**
 * Created by sn1 on 7/7/17.
 */

public class FetchThanksCountService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchThanksCountService(String name) {
        super("FetchThanksCountService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent workIntent) {
        String refToken = workIntent.getStringExtra("refToken");
        final ResultReceiver receiver = workIntent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();
        System.out.println("Fetching thanks count-- for "+refToken);

    }
}
