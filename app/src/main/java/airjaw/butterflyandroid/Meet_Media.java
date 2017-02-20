package airjaw.butterflyandroid;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

/**
 * Created by airjaw on 2/9/17.
 */

public class Meet_Media {

    private String gender;
    private String mediaID;
    private String mediaType;
    private String name;
    private String title;
    private String toUserID;
    private String FromUserID;

    Object timestamp;

    public Meet_Media(String gender, String mediaID, String mediaType, String name, String title, String toUserID, String fromUserID) {
        this.gender = gender;
        this.mediaID = mediaID;
        this.mediaType = mediaType;
        this.name = name;
        this.title = title;
        this.toUserID = toUserID;
        FromUserID = fromUserID;

        timestamp = ServerValue.TIMESTAMP;

    }



    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMediaID() {
        return mediaID;
    }

    public void setMediaID(String mediaID) {
        this.mediaID = mediaID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return FromUserID;
    }

    public void setFromUserID(String fromUserID) {
        FromUserID = fromUserID;
    }

    @Exclude
    public long getTimestampLong() {
        return (long)timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = (long)timestamp;
    }
}
