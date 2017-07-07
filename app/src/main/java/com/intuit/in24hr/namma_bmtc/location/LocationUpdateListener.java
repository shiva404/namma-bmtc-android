package com.intuit.in24hr.namma_bmtc.location;


import android.location.Location;

/**
 * Created by sn1 on 7/6/17.
 */

public interface LocationUpdateListener {
    void gotLocationUpdated(Location location);
}
