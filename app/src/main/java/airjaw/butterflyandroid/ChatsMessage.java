package airjaw.butterflyandroid;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

/**
 * Created by airjaw on 2/26/17.
 */

public class ChatsMessage {
    private String text;
    private String senderId;
    Object timestamp;

    public ChatsMessage() {

    }

    public ChatsMessage(String text, String senderId) {
        this.text = text;
        this.senderId = senderId;
        timestamp = ServerValue.TIMESTAMP;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    @Exclude
    public long getTimestampLong() {
        return (long)timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = (long)timestamp;
    }

}

