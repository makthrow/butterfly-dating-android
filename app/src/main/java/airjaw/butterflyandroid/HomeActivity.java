package airjaw.butterflyandroid;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;


public class HomeActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationServices locationClient;
    private LocationRequest mLocationRequest;
    Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tab_home:
                Log.i("menu", "home");

                Intent homeIntent = new Intent(this, HomeActivity.class);
                startActivity(homeIntent);
                return true;
            case R.id.tab_meet:
                Log.i("menu", "meet");

                Intent meetIntent = new Intent(this, MeetActivity.class);
                startActivity(meetIntent);

                return true;
            case R.id.tab_inbox:
                Log.i("menu", "inbox");

                Intent inboxIntent = new Intent(this, InboxActivity.class);
                startActivity(inboxIntent);

                return true;
            case R.id.tab_chat:
                Log.i("menu", "chat");

                Intent chatIntent = new Intent(this, ChatActivity.class);
                startActivity(chatIntent);

                return true;
            case R.id.tab_settings:
                Log.i("menu", "settings");

                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    // Location

    private void initLocation() {

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(120 * 1000)        // 120 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 seconds, in milliseconds

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    // ConnectionCallbacks
    @Override
    public void onConnected(Bundle connectionHint) {

        // REQUEST PERMISSIONS
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.PERMISSION_ACCESS_COARSE_LOCATION);
        }
        // granted
        else {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (lastLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            else {
                GeoLocation newLocation = new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                Constants.geoFireUsers.setLocation(Constants.userID, newLocation);
                // set global var for GeoLocation
                GeoFireGlobal.getInstance().setLastLocation(newLocation);
            }
        }
    }



    // PERMISSIONS

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }    }

    // OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    // LocationListener
    @Override
    public void onLocationChanged(Location location) {
        GeoLocation newLocation = new GeoLocation(location.getLatitude(), location.getLongitude());

        Constants.geoFireUsers.setLocation(Constants.userID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));
        // set global
        GeoFireGlobal.getInstance().setLastLocation(newLocation);

    }


}
