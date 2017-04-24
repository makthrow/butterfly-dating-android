package airjaw.butterflyandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import android.widget.RelativeLayout;
import android.widget.Toast;

import airjaw.butterflyandroid.Camera.CamSendMeetActivity;

import static airjaw.butterflyandroid.R.styleable.SimpleExoPlayerView;

public class MeetActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "MeetActivity";

    private static ArrayList<Media_Info> mediaIntroQueueList = new ArrayList<Media_Info>();
    private static ArrayList<String> mediaIntroQueueListTitles = new ArrayList<>();
    ArrayAdapter<String> stringAdapter;
    ListView mediaList;

    private static ArrayList<String> blockList = new ArrayList<>();

    GeoLocation lastGeoLocation;
    RelativeLayout buttonOverlay;
    SimpleExoPlayerView simpleExoPlayerView;
    SimpleExoPlayer simpleExoPlayer;

    private GoogleApiClient mGoogleApiClient;
    private LocationServices locationClient;
    private LocationRequest mLocationRequest;
    Location lastLocation;

    private boolean shouldAutoPlay;
    private boolean shouldShowPlaybackControls;

    private int lastVideoPlaying;
    private boolean shouldResumeVideo;

    int selectedUserAtIndexPath;

    boolean currentlyPlayingVideo = false;// setting this bool avoids an exception with presenting video player modally over each other on multiple user taps.
    private boolean userIsAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setTitle("");
        myToolbar.setSubtitle("");

        mediaList = (ListView) findViewById(R.id.mediaListView);

        stringAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mediaIntroQueueListTitles);
        mediaList.setAdapter(stringAdapter);

        mediaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title =  (String) parent.getItemAtPosition(position);
                Log.i(TAG, "onitemclickListener: " + title);

                // get media
                Media_Info mediaSelected = mediaIntroQueueList.get(position);
                Log.i(TAG, "title: " + mediaSelected.getTitle());
                Log.i(TAG, "mediaID: " + mediaSelected.getMediaID());

                playVideoAtCell(position);
                currentlyPlayingVideo = true;
            }
        });

        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoPlayerVideoView);
        buttonOverlay = (RelativeLayout) findViewById(R.id.buttonOverlay);

        shouldAutoPlay = true;
        shouldShowPlaybackControls = false;
        shouldResumeVideo = false;

        initLocation();

        userIsAdmin = false;

    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();

        Log.i(TAG, "onStart");

        if (!shouldResumeVideo) {
            getUserLocation();
            getLocalIntroductions();
        }
        else {
            playVideoAtCell(lastVideoPlaying);
            currentlyPlayingVideo = true;
        }
    }

    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
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

    private void playVideoAtCell(final int cellNumber){

        if (currentlyPlayingVideo) {return;}

        selectedUserAtIndexPath = cellNumber;

        getDownloadURL(cellNumber, new MeetActivityInterface() {
            @Override
            public void downloadURLCompleted(Uri uri) {
                Log.i(TAG, "playVideo");

                simpleExoPlayerView.setVisibility(View.VISIBLE);
                buttonOverlay.setVisibility(View.VISIBLE);

                // 1. Create a default TrackSelector
                Handler mainHandler = new Handler();
                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                TrackSelection.Factory videoTrackSelectionFactory =
                        new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
                TrackSelector trackSelector =
                        new DefaultTrackSelector(videoTrackSelectionFactory);

                // 2. Create a default LoadControl
                LoadControl loadControl = new DefaultLoadControl();

                // 3. Create the player
                simpleExoPlayer =
                        ExoPlayerFactory.newSimpleInstance(MeetActivity.this, trackSelector, loadControl);

                // Bind the player to the view.
                simpleExoPlayerView.setPlayer(simpleExoPlayer);

                // In ExoPlayer every piece of media is represented by MediaSource.
                // To play a piece of media you must first create a corresponding MediaSource and
                // then pass this object to ExoPlayer.prepare

                // Produces DataSource instances through which media data is loaded.
                String userAgent = Util.getUserAgent(MeetActivity.this, "Butterfly");
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(MeetActivity.this,
                        userAgent);

                // Produces Extractor instances for parsing the media data.
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                // This is the MediaSource representing the media to be played.
                MediaSource videoSource = new ExtractorMediaSource(uri,
                        dataSourceFactory, extractorsFactory, null, null);

                // Loops the video indefinitely.
                LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);

                lastVideoPlaying = cellNumber;

                // Prepare the player with the source.
//                simpleExoPlayer.prepare(videoSource); // this doesn't loop
                simpleExoPlayer.prepare(loopingSource); // this loops
                simpleExoPlayerView.setUseController(shouldShowPlaybackControls);
                simpleExoPlayer.setPlayWhenReady(shouldAutoPlay);

            }
        });
    }

    private void getDownloadURL(int cellNumber, final MeetActivityInterface completion) {
        String mediaID = mediaIntroQueueList.get(cellNumber).getMediaID();

        // firebase storage
        Constants.storageMediaRef.child(mediaID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadURL)
            {
                Log.i(TAG, "downloadURLCompleted: " + downloadURL.toString());
                completion.downloadURLCompleted(downloadURL);
            }
        });
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
    private void getLocalIntroductions() {

        final ArrayList<String> mediaLocationKeysWithinRadius = new ArrayList<String>();

        lastGeoLocation = GeoFireGlobal.getInstance().getLastLocation();

        if (lastGeoLocation != null) {
            GeoQuery circleQuery = Constants.geoFireMedia.queryAtLocation(lastGeoLocation, 50);

            circleQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                    Log.i("Query: Key added: ", key);

                    mediaLocationKeysWithinRadius.add(key);

                }
                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.i("GeoQueryError:", error.toString());
                }
            });
        }
        else {
            // last location is null
            Log.i("LOCATION", "last location null)");
        }

        // FILTER: BLOCK LIST
        getBlockList();

        long currentTimeInMilliseconds = System.currentTimeMillis();
        System.out.println("currentTimeInMilliseconds:" + currentTimeInMilliseconds);

        long twentyFourHoursStartTime = currentTimeInMilliseconds - Constants.twentyFourHoursInMilliseconds;
        long endTime = currentTimeInMilliseconds;

        // GENDER FILTER
        Context context = this;
        SharedPreferences settingsPrefs = context.getSharedPreferences(Constants.USER_SETTINGS_PREFS, MODE_PRIVATE);
        final boolean showMen = settingsPrefs.getBoolean("meetMenSwitch", false);
        final boolean showWomen = settingsPrefs.getBoolean("meetWomenSwitch", false);

        Query twentyFourHourqueryRef = Constants.MEDIA_INFO_REF.orderByChild("timestamp").startAt(twentyFourHoursStartTime).endAt(endTime);

        // Read from the database
        twentyFourHourqueryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                mediaIntroQueueList.clear();
                mediaIntroQueueListTitles.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = (String)snapshot.child("name").getValue();
                    String gender = (String)snapshot.child("gender").getValue();
                    String mediaID = (String)snapshot.child("mediaID").getValue();
                    String title = (String)snapshot.child("title").getValue();
                    String userID = (String)snapshot.child("userID").getValue();
                    long timestamp = (Long)snapshot.child("timestamp").getValue();
                    long age = (Long)snapshot.child("age").getValue();
                    String nameAndTitle = name + ": " + title;

                    if (showMen && showWomen) {
                        // show all users
                    }
                    else if (!showMen && !showWomen) {
                        // show all users
                    }
                    else if (userID.equals(Constants.userID)) {
                        // always show user's own intro
                    }
                    else if (!showMen && showWomen) {
                        if (gender.equals("male")) {
                            continue; // exit loop for this child
                        }
                    }
                    else if (showMen && !showWomen) {
                        if (gender.equals("female")) {
                            continue; // exit loop for this child
                        }
                    }

                    Media_Info mediaInfoDic = new Media_Info(age, gender, mediaID, name, title, userID);
                    mediaInfoDic.setTimestamp(timestamp);
                    // continue filter list by geographical radius:
                    //  key is found in the array of local mediaID from circleQuery

                    if (mediaLocationKeysWithinRadius.contains(mediaID)) {

                        Log.i("media within radius: ", mediaID);

                        if (!blockList.contains(userID)) {
                            mediaIntroQueueList.add(mediaInfoDic);
                            mediaIntroQueueListTitles.add(nameAndTitle);
                        }
                        else {
                            Log.i(TAG, "blockList contains userID: " + userID);
                        }

                    }
                    else {
                        Log.i("media not in radius: ", mediaID);
                    }
                }

                stringAdapter.notifyDataSetChanged();

                Log.i("ARRAY", mediaIntroQueueListTitles.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void getUserLocation() {

        // get user location
        Constants.geoFireUsers.getLocation(Constants.userID, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                    lastGeoLocation = location;
                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                    // TODO: request location permission
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
            }
        });
    }

    public void showReportAction(View view) {
        Log.i(TAG, "Report Button Clicked");
    }

    public void closeVideo(View view) {
        Log.i(TAG, "Pass Button Clicked");
        buttonOverlay = (RelativeLayout) findViewById(R.id.buttonOverlay);

        simpleExoPlayer.stop();
        simpleExoPlayer.release();
        simpleExoPlayerView.setVisibility(View.INVISIBLE);
        buttonOverlay.setVisibility(View.INVISIBLE);
        currentlyPlayingVideo = false;
        shouldResumeVideo = false;
    }

    public void sendMeet(View view) {

        String toUserID = mediaIntroQueueList.get(selectedUserAtIndexPath).getUserID();
        Log.i(TAG, "Meet Button Clicked: trying to meet: " +  toUserID);


        if (!toUserID.equals(Constants.userID)){
            currentlyPlayingVideo = false;

            simpleExoPlayer.stop();
            simpleExoPlayer.release();

            shouldResumeVideo = true;
            // open CamSendMeetActivity
            Intent camIntent = new Intent(this, CamSendMeetActivity.class);
            camIntent.putExtra("toUserID", toUserID);
            startActivity(camIntent);
//
//            simpleExoPlayerView.setVisibility(View.INVISIBLE);
//            buttonOverlay.setVisibility(View.INVISIBLE);
        }
        else {
            String reason = "Self Meet";
            showMeetErrorAlert(reason);
        }

    }
    private void showMeetErrorAlert(String reason) {
        String title = "Error";
        String message = "We ran into an error";
        if (reason.equals("Self Meet")){
            message = "You can't meet yourself!";
        }
        AlertDialog alertDialog = new AlertDialog.Builder(MeetActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Aww, OK...",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
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
            Log.i("PERMISSION", "onConnected: Granted");
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
                    Log.i(TAG, "permission granted");
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
        Log.i(TAG, "onLocationChanged");
        GeoLocation newLocation = new GeoLocation(location.getLatitude(), location.getLongitude());

        Constants.geoFireUsers.setLocation(Constants.userID, newLocation);
        // set global
        GeoFireGlobal.getInstance().setLastLocation(newLocation);

    }

    private void getBlockList() {
        // FILTER: BLOCK LIST
        FirebaseMethods.getBlockList(new FirebaseMethodsInterface() {
            @Override public void getUsersFBInfoCompleted(Facebook_Info fbInfo) {}
            @Override public void checkIfUsersAreMatched(boolean alreadyMatched) {}
            @Override public void fetchChatsMetaCompleted(ArrayList<ChatsMeta> chatsMetaList) {}
            @Override public void getBlockListCompleted(ArrayList<String> fetchedBlockList) {
                blockList = fetchedBlockList;
                for (int i = 0; i < blockList.size(); i++) {
                    Log.i(TAG, "blocked user: " + blockList.get(i));
                }
            }
            @Override public void getChatStatusCompleted(boolean active) {}
        });

    }

}
