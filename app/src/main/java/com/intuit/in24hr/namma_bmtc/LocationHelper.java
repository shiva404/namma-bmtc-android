package com.intuit.in24hr.namma_bmtc;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


import java.util.Timer;
import java.util.TimerTask;

public class LocationHelper {

    public LocationHelper(Context context){
        if(locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try{
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        try{
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        if(!gps_enabled && !network_enabled) {
            System.err.println("GPS and network disabled");
        }

        if(gps_enabled) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
                System.out.println("GPS enabled");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        if(network_enabled) {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
                System.out.println("network enabled");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private static LocationManager locationManager;
    boolean gps_enabled=false;
    boolean network_enabled=false;
    LocationResult locationResult;
    Timer timer1;

    public boolean getLocation(LocationResult result, int delay){
        locationResult=result;
        timer1=new Timer();
        timer1.schedule(new GetLastLocation(), delay);
        return true;
    }

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            try {
                locationManager.removeUpdates(locationListenerGps);
                locationManager.removeUpdates(locationListenerNetwork);

                Location net_loc=null, gps_loc=null;
                if(gps_enabled)
                    gps_loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                if(network_enabled)
//                    net_loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                //if there are both values use the latest one
                if(gps_loc!=null && net_loc!=null){
                    if(gps_loc.getTime()>net_loc.getTime())
                        locationResult.gotLocation(gps_loc);
                    else
                        locationResult.gotLocation(net_loc);
                    return;
                }

                if(gps_loc!=null){
                    System.out.println("got some gps location");
                    locationResult.gotLocation(gps_loc);

                    return;
                }
                if(net_loc!=null){
                    System.out.println("got some n/w location");
                    locationResult.gotLocation(net_loc);

                    return;
                }
                System.out.println("got nothing !!!!!");
                locationResult.gotLocation(null);

            } catch (SecurityException e) {
                e.printStackTrace();
            }


        }
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            try {
                timer1.cancel();
                locationResult.gotLocation(location);
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            try {
                timer1.cancel();
                locationResult.gotLocation(location);
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGps);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}
