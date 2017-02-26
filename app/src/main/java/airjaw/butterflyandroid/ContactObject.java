package airjaw.butterflyandroid;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

/**
 * Created by airjaw on 2/25/17.
 */

public class ContactObject {
    private String message;
    private String name;
    private String gender;
    private String email;
    private long age;

    Object timestamp;

    public ContactObject(String message, String name, String gender, String email, long age) {
        this.message = message;
        this.name = name;
        this.gender = gender;
        this.email = email;
        this.age = age;
        timestamp = ServerValue.TIMESTAMP;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    @Exclude
    public long getTimestampLong() {
        return (long)timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = (long)timestamp;
    }
}
