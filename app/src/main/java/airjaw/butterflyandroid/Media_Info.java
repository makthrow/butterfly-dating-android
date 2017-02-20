package airjaw.butterflyandroid;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Map;

/**
 * Created by airjaw on 2/9/17.
 */

public class Media_Info {

    private String gender;
    private String mediaID;
    private String name;
    private String title;
    private String userID;
    private long age;

    Object timestamp;

    public Media_Info() {
        // Default constructor required for calls to DataSnapshot.getValue(Media_Info.class)
    }

    public Media_Info(long age, String gender, String mediaID, String name, String title, String userID) {

        this.age = age;
        this.gender = gender;
        this.mediaID = mediaID;
        this.name = name;
        this.title = title;
        this.userID = userID;
        timestamp = ServerValue.TIMESTAMP;
    }


    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
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

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Exclude
    public long getTimestampLong() {
        return (long)timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = (long)timestamp;
    }

}
