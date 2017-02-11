package airjaw.butterflyandroid;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MeetActivity extends AppCompatActivity {

    private static final String TAG = "MeetActivity";

    // Firebase instance variables

    private static ArrayList<Media_Info> mediaIntroQueueList = new ArrayList<Media_Info>();
    private static ArrayList<String> mediaIntroQueueListTitles = new ArrayList<>();
    ArrayAdapter<String> stringAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        setupFirebaseListener();

        // LOG
        Log.i("Constants", Constants.userID);

        ListView mediaList = (ListView) findViewById(R.id.mediaListView);

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

                //playVideoAtCell(position)
            }
        });
    }

    private void playVideoAtCell(int cellNumber){
        getDownloadURL(cellNumber);
    }

    private void getDownloadURL(int cellNumber) {
        String mediaID = mediaIntroQueueList.get(cellNumber).getMediaID();

        // firebase storage

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
    private void setupFirebaseListener() {

        long currentTimeInMilliseconds = System.currentTimeMillis();
        long twentyFourHoursInMilliseconds = 86400000;
        long startTime = currentTimeInMilliseconds - twentyFourHoursInMilliseconds;
        long endTime = currentTimeInMilliseconds;
        long monthStartTime = currentTimeInMilliseconds - (twentyFourHoursInMilliseconds * 31);

        // custom query (set to one month currently)
        Query twentyFourHourqueryRef = Constants.MEDIA_INFO_REF.orderByChild("timestamp").startAt(monthStartTime).endAt(endTime);

        // Read from the database
        twentyFourHourqueryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                Media_Info value = dataSnapshot.getValue(Media_Info.class);
//                Log.d(TAG, "Value is: " + value);

//                Map<String, Object> objectMap = (HashMap<String, Object>)
//                        dataSnapshot.getValue();

                ArrayList<Media_Info> newItems = new ArrayList<Media_Info>();

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

                    long age = (Long)child.child("age").getValue();

                    Media_Info mediaInfoDic = new Media_Info(age, gender, mediaID, name, title, userID, timestamp);
                    newItems.add(mediaInfoDic);
                    mediaIntroQueueList = newItems;

                    }

                // get title strings from newItems array and put into array<String> for listView adapter
                for (Media_Info media : newItems) {
                    mediaIntroQueueListTitles.add(media.getTitle());
                }
                stringAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
