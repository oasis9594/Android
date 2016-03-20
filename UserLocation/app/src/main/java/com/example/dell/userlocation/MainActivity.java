package com.example.dell.userlocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, ResultCallback<LocationSettingsResult>, SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout refresh;
    public static final String TAG="myTag";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1, PLAY_SERVICES_RESOLUTION_REQUEST=9000, REQUEST_CODE_LOCATION = 2;
    private double latitude, longitude;
    private TextView mLatitude, mLongitude, mAddress, mLastUpdateTime;
    private String LastUpdateTime;
    private AddressResultReceiver mResultReceiver;
    private String mAddressOutput;
    private LocationSettingsRequest mLocationSettingsRequest;
    Button sendButton;

    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Check if google play services are available
        if (!checkPlayServices()) {
            Toast.makeText(this, "Device not compatible", Toast.LENGTH_SHORT).show();
            finish();
        }
        mLatitude = (TextView) findViewById(R.id.latitude);
        mLongitude = (TextView) findViewById(R.id.longitude);
        mAddress = (TextView) findViewById(R.id.address);
        mLastUpdateTime = (TextView) findViewById(R.id.lastUpdateTime);
        mAddressOutput = "";
        sendButton = (Button) findViewById(R.id.button_send);
        refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        refresh.setOnRefreshListener(this);
        refresh.setColorSchemeColors(Color.GRAY, Color.GREEN, Color.BLUE,
                Color.RED, Color.CYAN);
        refresh.setDistanceToTriggerSync(20);// in dips
        refresh.setSize(SwipeRefreshLayout.DEFAULT);

        //Check if any saved instance is there and update
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

        checkLocationSettings();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mLastLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mLastLocation
                // is not null.
                mLastLocation = savedInstanceState.getParcelable(KEY_LOCATION);
                latitude=mLastLocation.getLatitude();
                longitude=mLastLocation.getLongitude();
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                LastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }
            updateUI();
            displayAddressOutput();
        }

    }

    private void displayAddressOutput() {
        Log.i(TAG, "displayAddressOutput");
        mAddress.setText(mAddressOutput);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }
    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if(mGoogleApiClient.isConnected())
            stopLocationUpdates();
    }

    public void createLocationRequest()
    {
        //Create a location Request
        Log.i(TAG, "createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);//sets the rate in milliseconds at which your app prefers to receive location updates
        mLocationRequest.setFastestInterval(10000);//sets fastest rate in milliseconds at which your app can handle location updates
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Sets the priority
        mLocationRequest.setSmallestDisplacement(5);
    }
    public void buildLocationSettingsRequest() {
        //Build a location request
        Log.i(TAG, "buildLocationSettingsRequest");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }
    public void checkLocationSettings()
    {
        Log.i(TAG, "checkLocationSettings");
        //check whether the current location settings are satisfied
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        mLocationSettingsRequest);
        result.setResultCallback(this);
    }

    @Override
    public void onResult(LocationSettingsResult result) {
        Log.i(TAG, "locationSettings");
        final Status status = result.getStatus();
        final LocationSettingsStates state = result.getLocationSettingsStates();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                startLocationUpdates();
                Log.i(TAG, "All location settings are satisfied.");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(
                            MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog.
                break;
        }
    }

    @Override
    public void onRefresh() {
        startLocationUpdates();
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                refresh.setRefreshing(false);
            }
        }, 5200);
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(MainActivity.this, R.string.address_found, Toast.LENGTH_SHORT).show();
            }

        }
    }

    protected void startIntentService() {
        Log.i(TAG, "Adress Service Started");
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        mResultReceiver=new AddressResultReceiver(new Handler());
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }
    @Override
    public void onConnected(Bundle bundle) {

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Connected");
        if (mLastLocation == null) {
            try {
                Log.i(TAG, "Location null");
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                LastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateUI();
                //Call service for address
                if (!Geocoder.isPresent()) {
                    Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                    return;
                }
                startLocationUpdates();
                startIntentService();
            } catch (SecurityException e){
                Log.i(TAG, "SecurityException: "+e.toString());
            }
        }
        else
        {
            startLocationUpdates();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }
/*
    private boolean getLocation()
    {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request missing location permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
        } else {
            // Location permission has been granted, continue as usual.
            mLastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if(mLastLocation!=null)
        {
            latitude=mLastLocation.getLatitude();
            longitude=mLastLocation.getLongitude();
            return true;
        }

        return false;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // success!
                try {
                    mLastLocation =
                            LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                } catch (SecurityException e) {
                }

            } else {
                // Permission was denied or request was cancelled
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
*/
    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates");
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
            Log.i(TAG, "startLocationUpdates");
        } catch (SecurityException e) {
            Log.i(TAG, "LocationUpdates Failed");
        }

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates");
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
        catch (RuntimeException e)
        {
            Log.i(TAG, "stopLocationUpdates Failed, "+e.toString());
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection Failed");
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        Toast.makeText(this , "Location Changed", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Location Changed");
        latitude=mLastLocation.getLatitude();
        longitude=mLastLocation.getLongitude();
        LastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        startIntentService();//For updating Address
        updateUI();

    }

    private void updateUI() {
        Log.i(TAG, "Update UI");
        mLatitude.setText(String.valueOf(latitude));
        mLongitude.setText(String.valueOf(longitude));
        mLastUpdateTime.setText(LastUpdateTime);
    }
    public void shareLocation(View view)
    {
        sendButton.setClickable(false);
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "My address is: "+mAddressOutput);
        Intent chooser=Intent.createChooser(intent, "Share Location via ");

        // Verify the original intent will resolve to at least one activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
        else
        {
            Toast.makeText(this, "No application can send location", Toast.LENGTH_SHORT).show();
        }
        sendButton.setClickable(true);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mLastLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, LastUpdateTime);
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }
}
