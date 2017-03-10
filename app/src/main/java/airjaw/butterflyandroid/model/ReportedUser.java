package airjaw.butterflyandroid.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

/**
 * Created by airjaw on 3/10/17.
 */

public class ReportedUser {

    private int type;
    private String fromUserID;
    private String text;
    Object timestamp;


    public ReportedUser() {

    }

    public ReportedUser(int type, String fromUserID, String text) {
        this.type = type;
        this.fromUserID = fromUserID;
        this.text = text;
        timestamp = ServerValue.TIMESTAMP;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(String fromUserID) {
        this.fromUserID = fromUserID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Exclude
    public long getTimestampLong() {
        return (long)timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = (long)timestamp;
    }
}
