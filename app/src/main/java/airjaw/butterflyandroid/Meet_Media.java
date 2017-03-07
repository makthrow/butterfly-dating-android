package airjaw.butterflyandroid;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

/**
 * Created by airjaw on 2/9/17.
 */

public class Meet_Media {

    private String mediaID;
    private String mediaType;
    private String title;
    private String toUserID;
    private String fromUserID;
    private String gender;

    private boolean unread;
    private boolean unsent_notification;

    Object timestamp;


    public Meet_Media(String mediaID, String mediaType, String title, String toUserID, String fromUserID, String gender, boolean unread, boolean unsent_notification) {
        this.mediaID = mediaID;

        this.mediaType = mediaType;
        this.title = title;
        this.toUserID = toUserID;
        this.fromUserID = fromUserID;
        this.gender = gender;
        this.unread = unread;
        this.unsent_notification = unsent_notification;

        timestamp = ServerValue.TIMESTAMP;

    }


    public String getMediaID() {
        return mediaID;
    }

    public void setMediaID(String mediaID) {
        this.mediaID = mediaID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getToUserID() {
        return toUserID;
    }

    public void setToUserID(String toUserID) {
        this.toUserID = toUserID;
    }

    public String getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(String fromUserID) {
        fromUserID = fromUserID;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Exclude
    public long getTimestampLong() {
        return (long)timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = (long)timestamp;
    }


    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public boolean isUnsent_notification() {
        return unsent_notification;
    }

    public void setUnsent_notification(boolean unsent_notification) {
        this.unsent_notification = unsent_notification;
    }
}
