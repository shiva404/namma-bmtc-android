package com.intuit.in24hr.namma_bmtc;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.intuit.in24hr.namma_bmtc.types.LocationPage;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationUpdateResultReceiver.Receiver,
        LocationPullResultReceiver.Receiver,
        ShareLocationDialogFragment.ShareLocationDialogListener{

    private GoogleMap mMap;
    private Location currentLocation;
    public static String referenceToken;
    LocationUpdateResultReceiver locationUpdateReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        locationUpdateReceiver = new LocationUpdateResultReceiver(new Handler());
        locationUpdateReceiver.setReceiver(MainActivity.this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.share);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.share_menu);
                LayoutInflater inflater = mapFragment.getLayoutInflater(savedInstanceState);
                View childView = inflater.inflate(R.layout.location_share, null);

//                final PopupWindow popupWindow = new PopupWindow(
//                        childView,
//                        LayoutParams.WRAP_CONTENT,
//                        LayoutParams.WRAP_CONTENT);

//                ViewGroup insertPoint = (ViewGroup) findViewById(R.id.main_menu);
//                insertPoint.addView(childView, 0);
                //insertPoint.bringChildToFront(childView);
                ShareLocationDialogFragment dialog = new ShareLocationDialogFragment();
                Bundle args = new Bundle();
                args.putParcelable("receiver", locationUpdateReceiver);
                args.putSerializable("location", new com.intuit.in24hr.namma_bmtc.types.Location(referenceToken,
                        currentLocation.getLatitude(), currentLocation.getLongitude(), null, null)); //get location
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "Share location");

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
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LatLng latLang = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                return true;
            }
        });
    }

    @Override
    public void onReceiveLocationUpdateResult(int resultCode, Bundle resultData) {
        //update buttons to [stop sharing/update bus crowd] and disable share button
        referenceToken = resultData.getString("refToken");
        System.out.println("******************* Reference token" + referenceToken);
    }

    @Override
    public void onReceiveLocationPullResult(int resultCode, Bundle resultData) {
        //draw map
        IconGenerator iconFactory = new IconGenerator(getApplicationContext());

        switch (resultCode) {
            case Constants.STATUS_RUNNING:
                setProgressBarIndeterminateVisibility(true);
                break;
            case Constants.STATUS_FINISHED:
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
            case Constants.STATUS_ERROR:
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
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        mMap.addMarker(markerOptions);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EditText routeNumberValue = (EditText) findViewById(R.id.routeNumberText);
        String routeNumber;
        if(routeNumberValue == null)
            routeNumber = "501";
        else
            routeNumber = routeNumberValue.getText().toString();

//        RadioGroup busCrowdedButtonGroup = (RadioGroup) findViewById(R.id.radioBusCrowd);
//
//        int checkedRadioButtonId = busCrowdedButtonGroup.getCheckedRadioButtonId();
//
//        RadioButton crowdSelection = (RadioButton) findViewById(checkedRadioButtonId);
//        String crowdString = null;
//        switch (crowdSelection.getId()){
//            case R.id.radioBusCrowd:
//                crowdString = "CROWDED";
//            case R.id.radioSeats:
//                crowdString = "SEAT";
//            case R.id.radioStanding:
//                crowdString = "STAND";
//        }

        String crowdString = "STAND";
        Intent mServiceIntent = new Intent(this, LocationUpdateService.class);
        mServiceIntent.putExtra("location", new com.intuit.in24hr.namma_bmtc.types.Location(referenceToken, currentLocation.getLatitude(),
                currentLocation.getLongitude(), routeNumber, crowdString));
        mServiceIntent.putExtra("receiver", locationUpdateReceiver);
        startService(mServiceIntent);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
