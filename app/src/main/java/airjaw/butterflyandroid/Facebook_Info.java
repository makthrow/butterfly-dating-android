package airjaw.butterflyandroid;

import com.google.android.gms.vision.face.Face;

/**
 * Created by airjaw on 2/12/17.
 */

public class Facebook_Info {

    private String name;
    private String gender;
    private String first_name;
    private String last_name;
    private String email;
    private String birthday;
    private String pictureURL;

    public Facebook_Info() {

    }
    public Facebook_Info(String name, String gender, String first_name, String last_name, String email, String birthday, String pictureURL) {
        this.name = name;
        this.gender = gender;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.birthday = birthday;
        this.pictureURL = pictureURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

}
