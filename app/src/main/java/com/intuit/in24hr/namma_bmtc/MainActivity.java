package com.intuit.in24hr.namma_bmtc;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.intuit.in24hr.namma_bmtc.types.BusRoute;
import com.intuit.in24hr.namma_bmtc.types.LocationPage;

import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_ERROR;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_FINISHED;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_RUNNING;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationShareResultReceiver.Receiver,
        LocationPullResultReceiver.Receiver,
        LocationStopShareResultReceiver.Receiver,
        BusDetailsServiceResultReceiver.Receiver,
        GoogleMap.OnMarkerClickListener,
        ShareLocationDialogFragment.ShareLocationDialogListener,
        StopSharingDialogFragment.StopSharingDialogListener{

    private GoogleMap mMap;
    private Location currentLocation;
    public static String referenceToken;
    LocationShareResultReceiver locationUpdateReceiver;
    LocationStopShareResultReceiver locationStopShareResultReceiver;
    BusDetailsServiceResultReceiver busDetailsServiceResultReceiver;

    FloatingActionButton stopShareButton;
    FloatingActionButton shareButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        locationUpdateReceiver = new LocationShareResultReceiver(new Handler());
        locationUpdateReceiver.setReceiver(MainActivity.this);
        busDetailsServiceResultReceiver = new BusDetailsServiceResultReceiver(new Handler());
        busDetailsServiceResultReceiver.setReceiver(MainActivity.this);
        locationStopShareResultReceiver = new LocationStopShareResultReceiver(new Handler());
        locationStopShareResultReceiver.setReceiver(MainActivity.this);

        stopShareButton = (FloatingActionButton) findViewById(R.id.stop_share);
        stopShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopSharingDialogFragment dialog = StopSharingDialogFragment.newInstance();
                Bundle args = new Bundle();
                args.putSerializable("refToken", referenceToken); //get location
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "stop_sharing");
            }
        });
        stopShareButton.setVisibility(View.GONE);
        shareButton = (FloatingActionButton) findViewById(R.id.share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null) {
                    ShareLocationDialogFragment dialog = ShareLocationDialogFragment.newInstance();
                    Bundle args = new Bundle();
                    args.putSerializable("location", new com.intuit.in24hr.namma_bmtc.types.Location(referenceToken,
                            currentLocation.getLatitude(), currentLocation.getLongitude(), null, null)); //get location
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), "location_share");
                } else {
                    Toast.makeText(MainActivity.this,
                            "Oops!! Failed to locate you.. Please wait for sometime..", Toast.LENGTH_LONG).show();
                }
            }
        });
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        final boolean dontResetZoomLevel = false;
        // Add a marker in Sydney and move the camera
        final LocationHelper myLocation = new LocationHelper(getApplicationContext());
        final LocationPullResultReceiver locationPullReciever = new LocationPullResultReceiver(new Handler());
        locationPullReciever.setReceiver(MainActivity.this);

        LocationHelper.LocationResult locationResult = new LocationHelper.LocationResult() {

            @Override
            public void gotLocation(final android.location.Location loc) {
                try{
                    if(loc == null){
//                      Toast.makeText(getApplicationContext(), "Couldn't get your current location, please check your network.", Toast.LENGTH_SHORT).show();

                        myLocation.getLocation(this, 20000);
                    } else {
                        currentLocation = loc;

                        final Handler mHandler = new Handler(Looper.getMainLooper()) {
                            @Override
                            public void handleMessage(Message message) {
                                LatLng latLang = new LatLng(loc.getLatitude(), loc.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(latLang).title("Here u are"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, 14));
                            }
                        };
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Message message = mHandler.obtainMessage(0, "Nothing");
                                message.sendToTarget();
                            }
                        });
                        //get locations in the range of 3km
                        System.out.println("Sending req for location ----");
                        Intent locationPullService = new Intent(getApplicationContext(), LocationPullService.class);
                        assert currentLocation != null;
                        locationPullService.putExtra("receiver", locationPullReciever);
                        locationPullService.putExtra("location", new double[]{currentLocation.getLatitude(), currentLocation.getLongitude()});
                        getApplicationContext().startService(locationPullService);
                        myLocation.getLocation(this, 10000);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        };
        myLocation.getLocation(locationResult, 10000);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LatLng latLang = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                return true;
            }
        });
    }

    @Override
    public void onReceiveBusDetailsResult(int resultCode, Bundle resultData) {
        switch (resultCode){
            case STATUS_RUNNING:
                setProgressBarIndeterminateVisibility(true);
                break;
            case STATUS_FINISHED:
                ShowBusDetailsDialogFragment dialog;
                BusRoute busRoute = (BusRoute) resultData.getSerializable("busRoute");
                if(busRoute != null){
                    dialog = ShowBusDetailsDialogFragment.newInstance(busRoute.getBusRoute(), busRoute.getFrom(), busRoute.getTo(), busRoute.getDetails());
                } else {
                    dialog = ShowBusDetailsDialogFragment.newInstance("Not found", "Not found", "Not found", "No data");
                }
                Bundle args = new Bundle();
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "bus_details");
                //Trigger UI thread to start sending location
                break;
            case STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, "Something went wrong!! Location did not get shared.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onReceiveLStopLocationShareResult(int resultCode, Bundle resultData) {
        switch (resultCode){
            case STATUS_RUNNING:
                setProgressBarIndeterminateVisibility(true);
                break;
            case STATUS_FINISHED:
                Toast.makeText(this, "Sharing stopped", Toast.LENGTH_LONG).show();
                stopShareButton.setVisibility(View.GONE);
                shareButton.setVisibility(View.VISIBLE);
                //Trigger UI thread to start sending location
                break;
            case STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, "Something went wrong!! Location did not get shared.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onReceiveLocationShareResult(int resultCode, Bundle resultData) {
        //todo: update buttons to [stop sharing/update bus crowd] and disable share button
        switch (resultCode){
            case STATUS_RUNNING:
                setProgressBarIndeterminateVisibility(true);
                break;
            case STATUS_FINISHED:
                referenceToken = resultData.getString("refToken");
                Toast.makeText(this, "Location shared successfully", Toast.LENGTH_LONG).show();
                shareButton.setVisibility(View.GONE);
                stopShareButton.setVisibility(View.VISIBLE);
                //Trigger UI thread to start sending location
                break;
            case STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, "Something went wrong!! Location did not get shared.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onReceiveLocationPullResult(int resultCode, Bundle resultData) {
        //draw map
        IconGenerator iconFactory = new IconGenerator(getApplicationContext());

        switch (resultCode) {
            case STATUS_RUNNING:
                setProgressBarIndeterminateVisibility(true);
                break;
            case STATUS_FINISHED:
                mMap.clear();
                LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng));
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                LocationPage locationPage = (LocationPage) resultData.getSerializable("locations");
                for(com.intuit.in24hr.namma_bmtc.types.Location location : locationPage.getLocations()){
                    if(location.getCrowdLevel().equals("STAND"))
                        iconFactory.setColor(Color.YELLOW);
                    else if(location.getCrowdLevel().equals("CROWDED"))
                        iconFactory.setColor(Color.RED);
                    else if(location.getCrowdLevel().equals("SEAT"))
                        iconFactory.setColor(Color.CYAN);
                    else
                        iconFactory.setColor(Color.YELLOW);

                    LatLng latLang = new LatLng(location.getLatitude(), location.getLongitude());
                    addIcon(iconFactory, location.getRouteNumber(), latLang);
                }
                break;
            case STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                snippet(text.toString()).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        mMap.addMarker(markerOptions);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Intent mServiceIntent = new Intent(this, BusDetailsService.class);
//        mServiceIntent.putExtra("refToken", referenceToken);
        mServiceIntent.putExtra("busRouteNo", marker.getSnippet());
        mServiceIntent.putExtra("receiver", busDetailsServiceResultReceiver);
        startService(mServiceIntent);
        return true;
    }

    @Override
    public void onDialogPositiveClick(ShareLocationDialogFragment dialog) {

        EditText routeNumberValue = dialog.routeNumberValue;
        String routeNumber = routeNumberValue.getText().toString();

        RadioGroup busCrowdedButtonGroup = dialog.busCrowdedButtonGroup;

        System.out.println("%%%%%%%% route value " + routeNumber);

        int checkedRadioButtonId = busCrowdedButtonGroup.getCheckedRadioButtonId();

        RadioButton crowdSelection = (RadioButton) busCrowdedButtonGroup.findViewById(checkedRadioButtonId);
        String crowdString = null;
        switch (crowdSelection.getId()){
            case R.id.radioBusCrowd:
                crowdString = "CROWDED";
            case R.id.radioSeats:
                crowdString = "SEAT";
            case R.id.radioStanding:
                crowdString = "STAND";
        }

//        String crowdString = "STAND";
        Intent mServiceIntent = new Intent(this, LocationShareService.class);
        mServiceIntent.putExtra("location", new com.intuit.in24hr.namma_bmtc.types.Location(referenceToken, currentLocation.getLatitude(),
                currentLocation.getLongitude(), routeNumber, crowdString));
        mServiceIntent.putExtra("receiver", locationUpdateReceiver);
        startService(mServiceIntent);
    }

    @Override
    public void onDialogNegativeClick(ShareLocationDialogFragment dialog) {

    }

    @Override
    public void onStopShareDialogPositiveClick(StopSharingDialogFragment dialog) {
        System.out.println("Delete location *******************");
        Intent mServiceIntent = new Intent(this, LocationStopShareService.class);
        mServiceIntent.putExtra("refToken", referenceToken);
        mServiceIntent.putExtra("receiver", locationStopShareResultReceiver);
        startService(mServiceIntent);
    }

}
