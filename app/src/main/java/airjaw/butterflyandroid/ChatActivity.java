package airjaw.butterflyandroid;

import android.content.Intent;
import android.media.Image;
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

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    /*

    Clarification:
    "ChatActivity" (this) is a ChatsMeta ListView which shows an overall list of chats the user is part of.
        iOS equivalent: MatchesTableViewController

    "ChatRoomActivity" is the activity that shows individual chat Messages.
        iOS equivalent: ChatViewController

    */

    private static final String TAG = "ChatActivity";

    private static ArrayList<ChatsMeta> chatsMeta = new ArrayList<ChatsMeta>();
    private static ArrayList<String> chatMetaCellTitles = new ArrayList<>();
    private static ArrayList<Image> chatImages = new ArrayList<Image>();

    ArrayAdapter<String> stringAdapter;
    ListView chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        chatList = (ListView) findViewById(R.id.chatListView);

        stringAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                chatMetaCellTitles);
        chatList.setAdapter(stringAdapter);

        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title =  (String) parent.getItemAtPosition(position);
                ChatsMeta chatMetaSelected = chatsMeta.get(position);

                Log.i(TAG, "onitemclickListener: " + title);
                Log.i(TAG, "onitemclickListener key: " + chatMetaSelected.getKey());

                // get media
                ChatsMeta chatSelected = chatsMeta.get(position);

                Intent intent = new Intent(ChatActivity.this, ChatRoomActivity.class);
                intent.putExtra("chatID", chatMetaSelected.getKey());
                intent.putExtra("title", chatMetaSelected.getWithUserName());
                intent.putExtra("withUserID", chatMetaSelected.getWithUserID());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseMethods.fetchChatsMeta(new FirebaseMethodsInterface() {
            @Override
            public void getUsersFBInfoCompleted(Facebook_Info fbInfo) {

            }

            @Override
            public void checkIfUsersAreMatched(boolean alreadyMatched) {

            }

            @Override
            public void fetchChatsMetaCompleted(ArrayList<ChatsMeta> chatsMetaList) {
                chatsMeta = chatsMetaList;
                populateChatsMetaCellTitles(chatsMetaList);
            }
        });
    }

    private void populateChatsMetaCellTitles(ArrayList<ChatsMeta> chatsMetaList) {
        // helper function that iterates through our chatsMeta array and creates cell titles to display,
        // and adds them to our chatMetaCellTitles array.

        Log.i(TAG, "calling populateChatsMetaCellTitles");

        chatMetaCellTitles.clear();

        for (int i = 0; i < chatsMetaList.size(); i++) {

            Log.i(TAG, "KEY: " + chatsMetaList.get(i).getKey());

            ChatsMeta chatMetaObj = chatsMetaList.get(i);

            String lastMessage = chatMetaObj.getLastMessage();
            String lastSenderID = chatMetaObj.getLastSender();
            String withUserName = chatMetaObj.getWithUserName();

            // create the message to display here
            if (lastSenderID.equals("none")) {
                chatMetaCellTitles.add(lastMessage);
                Log.i(TAG, "adding key: " + chatMetaObj.getKey());
            }
            else { // already matched
                if (chatMetaObj.isUnread()) {
                    if (!lastSenderID.equals(Constants.userID)){
                        String cellText = withUserName + ": " + lastMessage;
                        chatMetaCellTitles.add(cellText);
                    }
                }
                else {
                    if (lastSenderID.equals(Constants.userID)) {
                        String cellText = "You: " + lastMessage;
                        chatMetaCellTitles.add(cellText);
                    }
                    else {
                        String cellText = withUserName + ": " + lastMessage;
                        chatMetaCellTitles.add(cellText);
                    }
                }
            }
        }
        stringAdapter.notifyDataSetChanged();
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

    private static void showConfirmDeleteNotificationFor() {

    }
}
