package airjaw.butterflyandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class ChatRoomActivity extends AppCompatActivity {

    private String chatID;
    private String withUserID;
    private String title;

    private static final String TAG = "ChatRoomActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Intent intent = getIntent();
        chatID = intent.getStringExtra("chatID");
        withUserID = intent.getStringExtra("withUserID");
        title = intent.getStringExtra("title");

        Log.i(TAG, "chatID: " + chatID);
        Log.i(TAG, "withUserID: " + withUserID);
        Log.i(TAG, "title: " + title);

    }
}
