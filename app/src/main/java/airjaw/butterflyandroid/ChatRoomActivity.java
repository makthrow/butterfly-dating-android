package airjaw.butterflyandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

    public void openReportActivity(View view) {
        Intent reportIntent = new Intent(this, ReportActivity.class);
        reportIntent.putExtra("withUserName", withUserName);
        reportIntent.putExtra("userIDToReport", withUserID);
        startActivity(reportIntent);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chatroom, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.report:
                Log.i("ChatRoomActivityMenu", "report");

                Intent reportIntent = new Intent(this, ReportActivity.class);
                reportIntent.putExtra("withUserName", withUserName);
                reportIntent.putExtra("userIDToReport", withUserID);
                startActivity(reportIntent);

                return true;
            case R.id.delete:
                Log.i("ChatRoomActivityMenu", "delete");
                showConfirmDeleteNotification();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void showConfirmDeleteNotification() {
        String title = "Delete Chat";
        String message = "Are you sure you want to close this chat? You can still match with this user later";

        AlertDialog alertDialog = new AlertDialog.Builder(ChatRoomActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Close this Chat",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteCurrentChat();
                        onSupportNavigateUp();
                    }
                });
        alertDialog.show();
    }
    private void deleteCurrentChat() {
        FirebaseMethods.deleteChatFor(chatID, withUserID);
    }

}
