package airjaw.butterflyandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;

import android.net.Uri;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import airjaw.butterflyandroid.Camera.CamSendMeetActivity;

public class MeetActivity extends AppCompatActivity {

    private static final String TAG = "MeetActivity";

    private static ArrayList<Media_Info> mediaIntroQueueList = new ArrayList<Media_Info>();
    private static ArrayList<String> mediaIntroQueueListTitles = new ArrayList<>();
    ArrayAdapter<String> stringAdapter;
    ListView mediaList;

    GeoLocation lastLocation;
    VideoView vidView;
    RelativeLayout buttonOverlay;

    int selectedUserAtIndexPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

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

                selectedUserAtIndexPath = position;

                playVideoAtCell(position);
            }
        });
        vidView = (VideoView)findViewById(R.id.myVideo);
        vidView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "onPrepared");
            }
        });
        vidView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "OnCompletion");
                mp.start();
            }
        });

        buttonOverlay = (RelativeLayout) findViewById(R.id.buttonOverlay);

        // 1 - iPhone - WORKS
        String testVidPath = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FymjM7mL1sycztRG4E4WSRRqBv9o1-1487019771?alt=media&token=cfcdc7bc-1932-4d74-9856-154dc8c9d9c8";

        // 2 - iPhone - WORKS
        String testVidPath2 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FCXfmNF9nyjh1cPAKiZza8vtpg8A3-1476847624?alt=media&token=e21066c0-5618-40d8-9081-fc5de96f8511";

        // 3 - iPhone - WORKS
        String testVidPath3 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FCXfmNF9nyjh1cPAKiZza8vtpg8A3-1476941162?alt=media&token=0f467465-803b-469a-93bf-4f658d2c5570";

        // 4 - iPhone 9mb - WORKS on initial load
        String testVidPath4 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FhpM9dRDIAAUlCQsAhixWBxOyK9c2-1477696362?alt=media&token=771f412f-b697-406c-af74-f57772e1b13d";

        // 6 - iPhone - WORKS
        String testVidPath6 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FCXfmNF9nyjh1cPAKiZza8vtpg8A3-1476258706?alt=media&token=d2bb8c75-864c-4a0b-818e-16f33fbfa7ac";

        // 7 - Android - doesn't work
        String testVidPath7 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FU7QuOvxLW9Xt9OPMFwFgwmz5P1A2-1487467942089?alt=media&token=88d8c32d-e906-4a5b-b057-70468dacbfff";

        // 8 - Android
        String testVidPath8 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FU7QuOvxLW9Xt9OPMFwFgwmz5P1A2-1487468452056?alt=media&token=c2552611-8dcd-4b62-af99-03bf6b12d158";

        // 9 - Android -
        String testVidPath9 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FU7QuOvxLW9Xt9OPMFwFgwmz5P1A2-1487470433?alt=media&token=fb9375d7-51ac-4a94-89a8-106a40a845c0";

        // 10 - Android
        String testVidPath10 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FU7QuOvxLW9Xt9OPMFwFgwmz5P1A2-1487533871?alt=media&token=0a6cc64b-40e2-4ffe-9ce4-5a547c2639af";

        // 11 - Android - doesn't work
        String testVidPath11 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FU7QuOvxLW9Xt9OPMFwFgwmz5P1A2-1487535121?alt=media&token=5b9c67f0-2d2b-4cd0-9826-4bf77c5c65ba";

        // 12 - Android - doesn't work
        String testVidPath12 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FU7QuOvxLW9Xt9OPMFwFgwmz5P1A2-1487537118?alt=media&token=333ceaef-7971-4336-8ba8-f01b7d59c5ad";

        // 13 - Android 1080p -
        String testVidPath13 = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2FU7QuOvxLW9Xt9OPMFwFgwmz5P1A2-1487548888?alt=media&token=56cc9680-407e-4b58-9ef1-725bc1bfb5a7";

        // Android (manually uploaded file) - WORKS
        String testVidPathManual = "https://firebasestorage.googleapis.com/v0/b/butterfly2-ac0f9.appspot.com/o/media%2F20170219_150854.mp4?alt=media&token=b8d4d76c-8af5-4d78-82f8-1a73bca60bd7";

//        vidView.setVideoPath(testVidPath4);
//        vidView.setVisibility(View.VISIBLE);
//        buttonOverlay.setVisibility(View.VISIBLE);
//        vidView.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");

        getUserLocation();

        getLocalIntroductions();

    }

    private void playVideoAtCell(int cellNumber){

        getDownloadURL(cellNumber, new MeetActivityInterface() {
            @Override
            public void downloadURLCompleted(Uri url) {

                Log.i(TAG, "playVideo");
//                vidView.setVideoURI(url);
                vidView.setVideoPath(url.toString()); // this also works
                Log.i(TAG, "url.toString: " + url.toString());
                vidView.setVisibility(View.VISIBLE);
                buttonOverlay.setVisibility(View.VISIBLE);
                vidView.start();

//                try {
//                    // Start the MediaController
//                    MediaController mediacontroller = new MediaController(
//                            MeetActivity.this);
//                    mediacontroller.setAnchorView(vidView);
//                    // Get the URL from String VideoURL
//                    vidView.setMediaController(mediacontroller);
//                    vidView.setVideoURI(url);
//                    vidView.setVisibility(View.VISIBLE);
//                    buttonOverlay.setVisibility(View.VISIBLE);
//                    vidView.start();
//
//                } catch (Exception e) {
//                    Log.e("Error", e.getMessage());
//                    e.printStackTrace();
//                }
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

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

        lastLocation = GeoFireGlobal.getInstance().getLastLocation();

        if (lastLocation != null) {
            GeoQuery circleQuery = Constants.geoFireMedia.queryAtLocation(lastLocation, 50);

            circleQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                    Log.i("Query: Key added", key.toString());

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

        // TODO: FILTER: BLOCK LIST

        long currentTimeInMilliseconds = System.currentTimeMillis();
        System.out.println("currentTimeInMilliseconds:" + currentTimeInMilliseconds);

        long startTime = currentTimeInMilliseconds - Constants.twentyFourHoursInMilliseconds;
        long endTime = currentTimeInMilliseconds;
        long monthStartTime = currentTimeInMilliseconds - (Constants.twentyFourHoursInMilliseconds * 31);

        // GENDER FILTER
        Context context = this;
        SharedPreferences settingsPrefs = context.getSharedPreferences(Constants.USER_SETTINGS_PREFS, MODE_PRIVATE);
        final boolean showMen = settingsPrefs.getBoolean("meetMenSwitch", false);
        final boolean showWomen = settingsPrefs.getBoolean("meetWomenSwitch", false);

        // custom query (set to one month currently)
        Query twentyFourHourqueryRef = Constants.MEDIA_INFO_REF.orderByChild("timestamp").startAt(monthStartTime).endAt(endTime);

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
                    Log.i(TAG, "name: " + name);

                    String gender = (String)snapshot.child("gender").getValue();
                    Log.i(TAG, "gender: " + gender);

                    String mediaID = (String)snapshot.child("mediaID").getValue();
                    Log.i(TAG, "mediaID: " + mediaID);

                    String title = (String)snapshot.child("title").getValue();
                    Log.i(TAG, "title: " + title);

                    String userID = (String)snapshot.child("userID").getValue();
                    Log.i(TAG, "userID: " + userID);

                    long timestamp = (Long)snapshot.child("timestamp").getValue();
                    Log.i(TAG, "timestamp: " + timestamp);

                    long age = (Long)snapshot.child("age").getValue();

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

                        mediaIntroQueueList.add(mediaInfoDic);
                        mediaIntroQueueListTitles.add(title);

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
                    lastLocation = location;
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

        vidView.stopPlayback();
        vidView.setVisibility(View.INVISIBLE);
        buttonOverlay.setVisibility(View.INVISIBLE);

    }

    public void sendMeet(View view) {
        Log.i(TAG, "Meet Button Clicked");

        String toUserID = mediaIntroQueueList.get(selectedUserAtIndexPath).getUserID();

        if (toUserID != Constants.userID) {
            // currentlyPlayingVideo = false;

            // open CamSendMeetActivity
            Intent camIntent = new Intent(this, CamSendMeetActivity.class);
            camIntent.putExtra("toUserID", toUserID);
            startActivity(camIntent);
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
}
