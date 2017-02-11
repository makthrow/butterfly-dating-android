package airjaw.butterflyandroid;

/**
 * Created by airjaw on 2/9/17.
 */

public class Media_Info {

    private String gender;
    private String mediaID;
    private String name;
    private String title;
    private String userID;
    private long timestamp;
    private long age;

    public Media_Info() {
        // Default constructor required for calls to DataSnapshot.getValue(Media_Info.class)
    }

    public Media_Info(long age, String gender, String mediaID, String name, String title, String userID, long timestamp) {

        this.age = age;
        this.gender = gender;
        this.mediaID = mediaID;
        this.name = name;
        this.title = title;
        this.userID = userID;
        this.timestamp = timestamp;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
