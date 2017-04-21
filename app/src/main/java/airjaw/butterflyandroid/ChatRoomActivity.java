package airjaw.butterflyandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
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
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity {

    private String chatID;
    private String withUserID;
    private String title;
    private String withUserName; // gonna be the same as the title
    ListView listOfMessages;
    private boolean chatActive;

    int lastVisiblePosition;
    int positionStart;

    private FirebaseListAdapter<ChatsMessage> adapter;

    private static final String TAG = "ChatRoomActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        lastVisiblePosition = -1;

        Intent intent = getIntent();
        chatID = intent.getStringExtra("chatID");
        withUserID = intent.getStringExtra("withUserID");
        title = intent.getStringExtra("title");
        withUserName = title; // created new string with same value, not reference

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle(title);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseMethods.observeChatActiveStatusFor(chatID, new FirebaseMethodsInterface() {
            @Override public void getUsersFBInfoCompleted(Facebook_Info fbInfo) {}
            @Override public void checkIfUsersAreMatched(boolean alreadyMatched) {}
            @Override public void fetchChatsMetaCompleted(ArrayList<ChatsMeta> chatsMeta) {}
            @Override public void getBlockListCompleted(ArrayList<String> blockedUsers) {}
            @Override public void getChatStatusCompleted(boolean active) {
                chatActive = active;
                if (!chatActive && !isDestroyed()) { showChatClosedNotification();}
            }
        });

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

                // animate sending message
                if (chatActive) { FirebaseMethods.createChatsMessagesFor(chatID, Constants.userID, withUserID, inputString);}
                else { showChatClosedNotification();}

                input.setText("");
            }
        });
        displayChatMessages();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove all listeners
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void displayChatMessages() {
        listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        Query latestChatsMessageQuery = Constants.CHATS_MESSAGES_REF.child(chatID);

        adapter = new FirebaseListAdapter<ChatsMessage>(this, ChatsMessage.class,
                R.layout.chatmessage, latestChatsMessageQuery) {
            @Override
            protected void populateView(View v, ChatsMessage chatsMessage, int position) {
                // Get references to the views of chatmessage.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                messageText.setText(chatsMessage.getText());

                if (chatsMessage.getSenderId().equals(Constants.userID)) {
                    messageUser.setText("Me");
                }
                else { messageUser.setText(withUserName);}

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        chatsMessage.getTimestampLong()));
            }
        };
        setupDataObserverForScroll();
        listOfMessages.setAdapter(adapter);
    }

    private void setupDataObserverForScroll() {

        listOfMessages.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastVisiblePosition = firstVisibleItem + visibleItemCount;
                lastVisiblePosition--;
                positionStart = firstVisibleItem;
                Log.i("lastVisiblePosition: ", Integer.toString(lastVisiblePosition));
                Log.i("positionStart", Integer.toString(positionStart));
            }
        });
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                int messageCount = adapter.getCount();
                // this is calling itself each time it finds another message...
                // only want to set the scroll position when this is done finding all the messages
//                Log.i("lastVisiblePosition", Integer.toString(lastVisiblePosition));
//                Log.i("positionStart", Integer.toString(positionStart));
//                Log.i("messageCount", Integer.toString(messageCount));
//                Log.i("adapterCount", Integer.toString(adapter.getCount()));
                if (adapter.getCount() == messageCount) {
                    listOfMessages.setSelection(messageCount - 1);
                }

                // TODO: Fix this to make it more user-friendly
                // ie. scroll to chat the user last read, rather than the latest sent

//                if (lastVisiblePosition < messageCount) {
//                    Log.i("Scrolling to position: ", Integer.toString(messageCount));
//                }

//                if (lastVisiblePosition == -1) {
//                    // the recycler view is initially being loaded, scroll to bottom
//                }
//                if (positionStart >= (messageCount - 1) &&
//                        lastVisiblePosition == (positionStart - 1)) {
//                    // user is at the bottom of the list, scroll to the bottom of the list to show the newly added message.
//                }
            }
        });
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

    private void showChatClosedNotification() {
        Log.i(TAG, "chat closed");
        String title = "Closed";
        String message = "This chat was closed";

        AlertDialog alertDialog = new AlertDialog.Builder(ChatRoomActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Aww, OK...",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onSupportNavigateUp();
                    }
                });
        alertDialog.show();
    }

}
