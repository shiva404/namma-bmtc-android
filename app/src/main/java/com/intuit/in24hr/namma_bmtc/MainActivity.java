package com.intuit.in24hr.namma_bmtc;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
import com.intuit.in24hr.namma_bmtc.location.GPSTracker;
import com.intuit.in24hr.namma_bmtc.location.LocationUpdateListener;
import com.intuit.in24hr.namma_bmtc.types.BusRoute;
import com.intuit.in24hr.namma_bmtc.types.LocationPage;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_ERROR;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_FINISHED;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_RUNNING;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationShareResultReceiver.Receiver,
        LocationPullResultReceiver.Receiver,
        LocationStopShareResultReceiver.Receiver,
        BusDetailsServiceResultReceiver.Receiver,
        LocationUpdateResultReciever.Receiver,
        GoogleMap.OnMarkerClickListener,
        ShareLocationDialogFragment.ShareLocationDialogListener,
        StopSharingDialogFragment.StopSharingDialogListener,
        LocationUpdateListener, BottomSheet3DialogFragment.BottomSheetActionsListener {

    private final int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    private GoogleMap mMap;
    private Location currentLocation;
    public static String referenceToken;
    LocationShareResultReceiver locationShareReceiver;
    LocationStopShareResultReceiver locationStopShareResultReceiver;
    BusDetailsServiceResultReceiver busDetailsServiceResultReceiver;
    LocationPullResultReceiver locationPullReciever;
    LocationUpdateResultReciever locationUpdateResultReciever;
    GPSTracker gps;

    String routeNumber = null;
    String crowdString = null;
    FloatingActionButton dropPageButton;
    FloatingActionButton shareButton;
    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    ScheduledFuture<?> pullLocationScheduledTask;

    BottomSheetDialogFragment bottomSheetDialogFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        locationShareReceiver = new LocationShareResultReceiver(new Handler());
        locationShareReceiver.setReceiver(MainActivity.this);
        busDetailsServiceResultReceiver = new BusDetailsServiceResultReceiver(new Handler());
        busDetailsServiceResultReceiver.setReceiver(MainActivity.this);
        locationStopShareResultReceiver = new LocationStopShareResultReceiver(new Handler());
        locationStopShareResultReceiver.setReceiver(MainActivity.this);

        locationPullReciever = new LocationPullResultReceiver(new Handler());
        locationPullReciever.setReceiver(MainActivity.this);

        locationUpdateResultReciever = new LocationUpdateResultReciever(new Handler());
        locationUpdateResultReciever.setReceiver(MainActivity.this);


        dropPageButton = (FloatingActionButton) findViewById(R.id.drop_sheet_btn);
        dropPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetDialogFragment != null)
                    bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });
        dropPageButton.setVisibility(View.GONE);
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        gps = new GPSTracker(MainActivity.this, this, this);
        startPollingForLocations();
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
                dropPageButton.setVisibility(View.GONE);
                shareButton.setVisibility(View.VISIBLE);
                referenceToken = null;
                startPollingForLocations();
                bottomSheetDialogFragment.dismiss();
                //Trigger UI thread to start sending location
                break;
            case STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, "Something went wrong!! Location did not get shared.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void startPollingForLocations() {
        System.out.println("starting pulling resulsts");
        currentLocation = gps.getLocation();
        //to keep pulling statu of the newly available bus
        pullLocationScheduledTask = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (gps.canGetLocation()) {
                    currentLocation = gps.getLocation();
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    Intent locationPullService = new Intent(getApplicationContext(), LocationPullService.class);
                    locationPullService.putExtra("receiver", locationPullReciever);
                    locationPullService.putExtra("location", new double[]{latitude, longitude});
                    getApplicationContext().startService(locationPullService);

                } else {
                    gps.showSettingsAlert();
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
        mMap.setOnMarkerClickListener(MainActivity.this);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LatLng latLang = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                return true;
            }
        });
        final Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                LatLng latLang = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLang).title("Here u are"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLang));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
            }
        };
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Message message = mHandler.obtainMessage(0, "Nothing");
                message.sendToTarget();
            }
        });
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
                dropPageButton.setVisibility(View.VISIBLE);
                System.out.println("Stopping pulling resulsts");
                pullLocationScheduledTask.cancel(true); //stop pulling locations
                if(bottomSheetDialogFragment == null){
                    bottomSheetDialogFragment = new BottomSheet3DialogFragment(MainActivity.this);
                }
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

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
                System.out.println("Redrawing map with points !! ");
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


    //Share location
    @Override
    public void onDialogPositiveClick(ShareLocationDialogFragment dialog) {

        EditText routeNumberValue = dialog.routeNumberValue;
        routeNumber = routeNumberValue.getText().toString();

        RadioGroup busCrowdedButtonGroup = dialog.busCrowdedButtonGroup;

        System.out.println("%%%%%%%% route value " + routeNumber);

        int checkedRadioButtonId = busCrowdedButtonGroup.getCheckedRadioButtonId();

        RadioButton crowdSelection = (RadioButton) busCrowdedButtonGroup.findViewById(checkedRadioButtonId);

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
        mServiceIntent.putExtra("receiver", locationShareReceiver);
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

    @Override
    public void gotLocationUpdated(Location location) {
        System.out.println("Got location --> " + location.getLatitude() + "---" + location.getLongitude());
        if(referenceToken != null) {
            Intent mServiceIntent = new Intent(this, LocationUpdateService.class);
            mServiceIntent.putExtra("refToken", referenceToken);
            mServiceIntent.putExtra("location", new com.intuit.in24hr.namma_bmtc.types.Location(referenceToken, currentLocation.getLatitude(),
                    currentLocation.getLongitude(), routeNumber, crowdString));
            mServiceIntent.putExtra("receiver", locationUpdateResultReciever);
            startService(mServiceIntent);
        }
    }

    @Override
    public void onReceiveUpdateLocationShareResult(int resultCode, Bundle resultData) {
        System.out.println("Location updated sucessfully !!!!");
    }

    @Override
    public void handleStopSharingAction() {
        StopSharingDialogFragment dialog = StopSharingDialogFragment.newInstance();
                Bundle args = new Bundle();
                args.putSerializable("refToken", referenceToken); //get location
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "stop_sharing");
    }
}
