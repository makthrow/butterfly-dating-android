package airjaw.butterflyandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
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
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

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
    ListView chatList;
    private List<ChatRow> chatRowList = new ArrayList<>();
    private ChatRowCustomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        initChatRows();
    }

    private void initChatRows() {
        chatList = (ListView) findViewById(R.id.activity_chat_listview);
        adapter = new ChatRowCustomListAdapter(this, chatRowList);
        chatList.setAdapter(adapter);

        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatsMeta chatMetaSelected = chatRowList.get(position).getChatMetaObj();

                Intent intent = new Intent(ChatActivity.this, ChatRoomActivity.class);
                intent.putExtra("chatID", chatMetaSelected.getKey());
                intent.putExtra("title", chatMetaSelected.getWithUserName());
                intent.putExtra("withUserID", chatMetaSelected.getWithUserID());
                startActivity(intent);
            }
        });

        fetchChatsMeta();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void fetchChatsMeta() {

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
                populateChatRows(chatsMetaList);
            }
        });
    }

    private void populateChatRows(ArrayList<ChatsMeta> chatsMetaList) {

        chatRowList.clear();

        for (int i = 0; i < chatsMetaList.size(); i++) {
            Log.i(TAG, "KEY: " + chatsMetaList.get(i).getKey());

            final ChatsMeta chatMetaObj = chatsMetaList.get(i);

            StorageReference fbPhotoRef = Constants.storageFBProfilePicRef.child(chatMetaObj.getWithUserID());
            fbPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String profilePicURL = uri.toString();
                    Log.i(TAG, "retrieved profilePicURL: " + profilePicURL);

                    ChatRow newChatRowObj = new ChatRow(chatMetaObj, profilePicURL);
                    chatRowList.add(newChatRowObj);
                    adapter.notifyDataSetChanged();

                }
            });
        }
        adapter.notifyDataSetChanged();
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
}
