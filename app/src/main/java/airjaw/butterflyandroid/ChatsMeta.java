package airjaw.butterflyandroid;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

/**
 * Created by airjaw on 2/20/17.
 */

public class ChatsMeta {

    private String key;
    private String lastMessage;
    private String withUserID;
    private String lastSender;
    private boolean unread;
    private String withUserName;
    private boolean unsent_notification;

    Object timestamp;

    public ChatsMeta(String key, String lastMessage, String withUserID, String lastSender, boolean unread, String withUserName, boolean unsent_notification) {
        this.key = key;
        this.lastMessage = lastMessage;
        this.withUserID = withUserID;
        this.lastSender = lastSender;
        this.unread = unread;
        this.withUserName = withUserName;
        this.unsent_notification = unsent_notification;
        timestamp = ServerValue.TIMESTAMP;

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getWithUserID() {
        return withUserID;
    }

    public void setWithUserID(String withUserID) {
        this.withUserID = withUserID;
    }

    public String getLastSender() {
        return lastSender;
    }

    public void setLastSender(String lastSender) {
        this.lastSender = lastSender;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public String getWithUserName() {
        return withUserName;
    }

    public void setWithUserName(String withUserName) {
        this.withUserName = withUserName;
    }

    public boolean isUnsent_notification() {
        return unsent_notification;
    }

    public void setUnsent_notification(boolean unsent_notification) {
        this.unsent_notification = unsent_notification;
    }

    @Exclude
    public long getTimestampLong() {
        return (long)timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = (long)timestamp;
    }
}
