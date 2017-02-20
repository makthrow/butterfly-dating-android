package airjaw.butterflyandroid;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.widget.VideoView;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class InboxActivity extends AppCompatActivity {

    private static final String TAG = "InboxActivity";

    private static ArrayList<Meet_Media> meetMedia = new ArrayList<Meet_Media>();
    private static ArrayList<String> meetMediaListTitles = new ArrayList<>();
    ArrayAdapter<String> stringAdapter;
    ListView mediaList;
    VideoView vidView;
    RelativeLayout buttonOverlay;
    int selectedUserAtIndexPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

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

        long currentTimeInMilliseconds = System.currentTimeMillis();
        System.out.println("currentTimeInMilliseconds:" + currentTimeInMilliseconds);

        long startTime = currentTimeInMilliseconds - (Constants.twentyFourHoursInMilliseconds * 2); // 48 hours
        long endTime = currentTimeInMilliseconds;

        // TODO: FILTER: GENDER

        DatabaseReference meetMediaUserRef = Constants.MEET_MEDIA_REF.child(Constants.userID);
        Query media48HourQuery = meetMediaUserRef.orderByChild("timestamp").startAt(startTime).endAt(endTime);

        // Read from the database
        media48HourQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                meetMedia.clear();
                meetMediaListTitles.clear();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String name = (String)child.child("name").getValue();
                    Log.i(TAG, "name: " + name);

                    String gender = (String)child.child("gender").getValue();
                    Log.i(TAG, "gender: " + gender);

                    String mediaID = (String)child.child("mediaID").getValue();
                    Log.i(TAG, "mediaID: " + mediaID);

                    String title = (String)child.child("title").getValue();
                    Log.i(TAG, "title: " + title);

                    String userID = (String)child.child("userID").getValue();
                    Log.i(TAG, "userID: " + userID);

                    long timestamp = (Long)child.child("timestamp").getValue();
                    Log.i(TAG, "timestamp: " + timestamp);

                    Meet_Media newMeetMedia = new Meet_Media(gender, mediaID, name, title, userID);
                    newMeetMedia.setTimestamp(timestamp);

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

        getDownloadURL(cellNumber, new InboxActivityInterface() {
            @Override
            public void downloadURLCompleted(Uri url) {

                Log.i(TAG, "playVideoAtCell");
//                vidView.setVideoURI(url);
                vidView.setVideoPath(url.toString()); // this also works
                Log.i(TAG, "url.toString: " + url.toString());

                vidView.setVisibility(View.VISIBLE);
                buttonOverlay.setVisibility(View.VISIBLE);
                vidView.start();

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

        vidView.stopPlayback();
        vidView.setVisibility(View.INVISIBLE);
        buttonOverlay.setVisibility(View.INVISIBLE);

    }

    public void meetPerson(View view) {
        Log.i(TAG, "Meet Button Clicked");

        String currentUserID = Constants.userID;
        String fromUserID = userIDFromMatch(selectedUserAtIndexPath);

        // check if matched

        // if not
        Map newMatchDic = setupNewMatchToSave(fromUserID, currentUserID);
        if (newMatchDic != null) {
            // have all the necessary info, setup a new chat
            FirebaseMethods.setupNewChatWith(fromUserID);
            showMeetPersonAlert();
        }
        else {
            // show error alert
            showErrorMatchAlert();
        }

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

    }
    public void showErrorMatchAlert() {

    }
}
