package airjaw.butterflyandroid;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.Query;

public class ChatRoomActivity extends AppCompatActivity {

    private String chatID;
    private String withUserID;
    private String title;
    private String withUserName; // gonna be the same as the title

    private boolean chatActive;

    private FirebaseListAdapter<ChatsMessage> adapter;

    private static final String TAG = "ChatRoomActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Intent intent = getIntent();
        chatID = intent.getStringExtra("chatID");
        withUserID = intent.getStringExtra("withUserID");
        title = intent.getStringExtra("title");
        withUserName = title; // created new string with same value, not reference

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle(title);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chatActive = true;

        Log.i(TAG, "chatID: " + chatID);
        Log.i(TAG, "withUserID: " + withUserID);
        Log.i(TAG, "title: " + title);

        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input);
                String inputString = input.getText().toString();

                if (chatActive) {
                    FirebaseMethods.createChatsMessagesFor(chatID, Constants.userID, withUserID, inputString);
                    // animate sending message
                }
                else {
                    showChatClosedNotification();
                }

                input.setText("");
            }
        });
        displayChatMessages();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void displayChatMessages() {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        Query latestChatsMessageQuery = Constants.CHATS_MESSAGES_REF.child(chatID).limitToLast(25);

        adapter = new FirebaseListAdapter<ChatsMessage>(this, ChatsMessage.class,
                R.layout.chatmessage, latestChatsMessageQuery) {
            @Override
            protected void populateView(View v, ChatsMessage chatsMessage, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(chatsMessage.getText());

                Log.i(TAG, chatsMessage.getSenderId());
                Log.i(TAG, chatsMessage.getText());

                if (chatsMessage.getSenderId().equals(Constants.userID)) {
                    messageUser.setText("Me");
                }
                else {
                    messageUser.setText(withUserName);
                }

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        chatsMessage.getTimestampLong()));
            }
        };

        listOfMessages.setAdapter(adapter);

        setupDataObserverForScroll();
    }

    private void showChatClosedNotification(){

    }
    private void setupDataObserverForScroll() {
        // TODO:
    }

}
