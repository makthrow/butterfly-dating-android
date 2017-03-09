package airjaw.butterflyandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
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
import android.widget.RelativeLayout;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InboxActivity extends AppCompatActivity {

    private static final String TAG = "InboxActivity";

    private static ArrayList<Meet_Media> meetMedia = new ArrayList<Meet_Media>();
    private static ArrayList<String> meetMediaListTitles = new ArrayList<>();
    ArrayAdapter<String> stringAdapter;
    ListView mediaList;
    RelativeLayout buttonOverlay;
    int selectedUserAtIndexPath;
    SimpleExoPlayerView simpleExoPlayerView;
    SimpleExoPlayer simpleExoPlayer;
    private boolean shouldAutoPlay;
    private boolean shouldShowPlaybackControls;
    boolean currentlyPlayingVideo = false;// setting this bool avoids an exception with presenting video player modally over each other on multiple user taps.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setTitle("");
        myToolbar.setSubtitle("");

        mediaList = (ListView) findViewById(R.id.mediaListView);

        stringAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                meetMediaListTitles);
        mediaList.setAdapter(stringAdapter);

        mediaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title =  (String) parent.getItemAtPosition(position);
                Log.i(TAG, "onitemclickListener: " + title);

                // get media
                Meet_Media mediaSelected = meetMedia.get(position);
                Log.i(TAG, "title: " + mediaSelected.getTitle());
                Log.i(TAG, "mediaID: " + mediaSelected.getMediaID());

                selectedUserAtIndexPath = position;

                playVideoAtCell(position);
                currentlyPlayingVideo = true;

            }
        });

        buttonOverlay = (RelativeLayout) findViewById(R.id.buttonOverlay);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoPlayerVideoView);

        shouldAutoPlay = true;
        shouldShowPlaybackControls = false;

        simpleExoPlayerView.setUseController(shouldShowPlaybackControls);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");

        getMeetMedia();

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

    private void getMeetMedia() {

        final ArrayList<String> mediaLocationKeysWithinRadius = new ArrayList<String>();

        // TODO: FILTER: BLOCK LIST


        // GENDER FILTER
        Context context = this;
        SharedPreferences settingsPrefs = context.getSharedPreferences(Constants.USER_SETTINGS_PREFS, MODE_PRIVATE);
        final boolean showMen = settingsPrefs.getBoolean("meetMenSwitch", false);
        final boolean showWomen = settingsPrefs.getBoolean("meetWomenSwitch", false);

        long currentTimeInMilliseconds = System.currentTimeMillis();
        System.out.println("currentTimeInMilliseconds:" + currentTimeInMilliseconds);

        long startTime = currentTimeInMilliseconds - (Constants.twentyFourHoursInMilliseconds * 2); // 48 hours
        long endTime = currentTimeInMilliseconds;

        DatabaseReference meetMediaUserRef = Constants.MEET_MEDIA_REF.child(Constants.userID);
        Query media48HourQuery = meetMediaUserRef.orderByChild("timestamp").startAt(startTime).endAt(endTime);

        long startTimeOneMonth = currentTimeInMilliseconds - (Constants.twentyFourHoursInMilliseconds * 31); // 31 days
        Query debuggingQueryOneMonth = meetMediaUserRef.orderByChild("timestamp").startAt(startTimeOneMonth).endAt(endTime);

        // TODO: debugging Query set to one month. don't put in production
        debuggingQueryOneMonth.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                meetMedia.clear();
                meetMediaListTitles.clear();

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    Log.i("SNAPSHOT", "running through children");

                    String mediaID = (String)child.child("mediaID").getValue();
                    Log.i(TAG, "mediaID: " + mediaID);

                    String mediaType = (String)child.child("mediaType").getValue();
                    Log.i(TAG, "mediaType: " + mediaType);

                    String title = (String)child.child("title").getValue();
                    Log.i(TAG, "title: " + title);

                    String fromUserID = (String)child.child("fromUserID").getValue();
                    Log.i(TAG, "fromUserID: " + fromUserID);

                    String toUserID = (String)child.child("toUserID").getValue();
                    Log.i(TAG, "toUserID: " + toUserID);

                    String gender = (String)child.child("gender").getValue();

                    boolean unread = (boolean)child.child("unread").getValue();
                    Log.i(TAG, "unread: " + unread);

                    boolean unsent_notification = (boolean)child.child("unsent_notification").getValue();
                    Log.i(TAG, "unsent_notification: " + unsent_notification);

                    long timestamp = (Long)child.child("timestamp").getValue();
                    Log.i(TAG, "timestamp: " + timestamp);

                    Meet_Media newMeetMedia = new Meet_Media(mediaID, mediaType, title, toUserID, fromUserID, gender, unread, unsent_notification);
                    newMeetMedia.setTimestamp(timestamp);

                    // FILTER: GENDER
                    if (showMen && showWomen) {
                        // show all users
                    }
                    else if (!showMen && !showWomen) {
                        // show all users
                    }
                    else if (!showMen) {
                        if (gender.equals("male")) {
                            continue; // exit loop for this child
                        }
                    }
                    else if (!showWomen) {
                        if (gender.equals("female")) {
                            continue; // exit loop for this child
                        }
                    }

                    meetMedia.add(newMeetMedia);
                    meetMediaListTitles.add(title);

                }

                stringAdapter.notifyDataSetChanged();

                Log.i("ARRAY", meetMediaListTitles.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


    private void playVideoAtCell(int cellNumber){

        if (currentlyPlayingVideo) {return;}

        getDownloadURL(cellNumber, new InboxActivityInterface() {
            @Override
            public void downloadURLCompleted(Uri uri) {

                Log.i(TAG, "playVideoAtCell");

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
                        ExoPlayerFactory.newSimpleInstance(InboxActivity.this, trackSelector, loadControl);

                // Bind the player to the view.
                simpleExoPlayerView.setPlayer(simpleExoPlayer);

                // In ExoPlayer every piece of media is represented by MediaSource.
                // To play a piece of media you must first create a corresponding MediaSource and
                // then pass this object to ExoPlayer.prepare

                // Produces DataSource instances through which media data is loaded.
                String userAgent = Util.getUserAgent(InboxActivity.this, "Butterfly");
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(InboxActivity.this,
                        userAgent);

                // Produces Extractor instances for parsing the media data.
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                // This is the MediaSource representing the media to be played.
                MediaSource videoSource = new ExtractorMediaSource(uri,
                        dataSourceFactory, extractorsFactory, null, null);
                // Loops the video indefinitely.
                LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);

                // Prepare the player with the source
//                simpleExoPlayer.prepare(videoSource); // this doesn't loop
                simpleExoPlayer.prepare(loopingSource); // this loops
                simpleExoPlayerView.setUseController(shouldShowPlaybackControls);
                simpleExoPlayer.setPlayWhenReady(shouldAutoPlay);

            }
        });
    }

    private void getDownloadURL(int cellNumber, final InboxActivityInterface completion) {
        String mediaID = meetMedia.get(cellNumber).getMediaID();

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
    }

    public void meetPerson(View view) {
        Log.i(TAG, "Meet Button Clicked");

        final String currentUserID = Constants.userID;
        final String fromUserID = userIDFromMatch(selectedUserAtIndexPath);

        // check if matched
        FirebaseMethods.checkIfMatched(currentUserID, fromUserID, new FirebaseMethodsInterface() {
            @Override
            public void getUsersFBInfoCompleted(Facebook_Info fbInfo) {

            }

            @Override
            public void checkIfUsersAreMatched(boolean matched) {
                if (matched) {
                    String reason = "Already Matched";
                    showErrorMatchAlert(reason);
                }
                else {

                    Map newMatchDic = setupNewMatchToSave(fromUserID, currentUserID);
                    if (newMatchDic != null) {
                        // have all the necessary info, setup a new chat
                        FirebaseMethods.setupNewChatWith(fromUserID);
                        showMeetPersonAlert();
                    }
                    else {
                        // show error alert
                        showErrorMatchAlert(null);
                    }
                }
            }

            @Override
            public void fetchChatsMetaCompleted(ArrayList<ChatsMeta> chatsMeta) {

            }
        });
    }

    public String userIDFromMatch(int selectedUserAtIndexPath) {
        String fromUserID = meetMedia.get(selectedUserAtIndexPath).getFromUserID();
        return fromUserID;
    }

    public Map<String, Object> setupNewMatchToSave (final String fromUserID, final String userID){
        if ((fromUserID != null) && (userID != null)) {
            /*
             matches table: {
             { userID1 }
             { userID2 }
             { timestamp}
             */

            Map matchDic = new HashMap<String, Object>() {
                {
                    put("timestamp", Constants.firebaseServerValueTimestamp);
                    put("userID1", userID);
                    put("userID2", fromUserID);

                }
            };
                    return matchDic;
        }
        return null;
    }

    public void showMeetPersonAlert() {
        String title = "You've Matched! Say Hello?";

        AlertDialog alertDialog = new AlertDialog.Builder(InboxActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(null);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Chat Now",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // TODO: take user to ChatActivity
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Later",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }
    public void showErrorMatchAlert(String reason) {
        String title = "Error Matching";
        String message = "We ran into an error matching you two. Sorry!";
        if (reason.equals("Already Matched")); {
            message = "You're already matched! Go say Hi";
        }

        AlertDialog alertDialog = new AlertDialog.Builder(InboxActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
