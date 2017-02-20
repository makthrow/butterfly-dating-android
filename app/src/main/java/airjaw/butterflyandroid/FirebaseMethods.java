package airjaw.butterflyandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by airjaw on 2/18/17.
 */

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";


    private static final FirebaseMethods instance = new FirebaseMethods();

    public static FirebaseMethods getInstance() {
        return instance;
    }

    public static void uploadVideoToMediaInfo(Uri videoURL, final String title, final Context context) {

        // generate a timestamp
        long currentTimeInMilliseconds = System.currentTimeMillis();
        String videoTimeStamp = Long.toString(currentTimeInMilliseconds / 1000);

        // combine the two to form a unique video ID of userID + timestamp
        final String mediaID = Constants.userID + "-" + videoTimeStamp;
        Log.i(TAG, mediaID);

        // -----------FIREBASE STORAGE-----------
        // upload video file to Firebase storage
        StorageReference newMediaRef = Constants.storageMediaRef.child(mediaID);

        UploadTask uploadTask = newMediaRef.putFile(videoURL);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                Log.i(TAG, "upload FBStorage success: " + mediaID);

                // if SUCCESS in upload to FIREBASE STORAGE, upload video info to FIREBASE DATABASE
                uploadVideoInfoToDatabase(title, mediaID, context);
            }
        });


    }

    public static void uploadVideoToMeetMedia(URL videoURL, String title, String toUserID, String currentMediaTypeToUpload) {

    }

    private static void uploadVideoInfoToDatabase(String title, String mediaID, Context context) {
        GeoFireGlobal.getUserLocation();
        Constants.geoFireMedia.setLocation(mediaID, GeoFireGlobal.getInstance().getLastLocation());

        // -----------FIREBASE DATABASE-----------
        //upload video info to Firebase real-time database (separate from the video file itself which goes to Firebase storage, with the title we generate above)

        // create and upload video info data
        SharedPreferences prefs = context.getSharedPreferences(Constants.USER_FBINFO_PREFS, MODE_PRIVATE);
        String name = prefs.getString("first_name", null);
        int age  = prefs.getInt("age", 30); // TODO: change this default age someday
        String gender = prefs.getString("gender", null);
        Log.i("PREFS", name);
        Log.i("PREFS", Integer.toString(age));
        Log.i("PREFS", gender);

        Media_Info newVideoPost = new Media_Info(age, gender, mediaID, name, title, Constants.userID);

        DatabaseReference newVideoPostChild = Constants.MEDIA_INFO_REF.child(mediaID);
        newVideoPostChild.setValue(newVideoPost);
    }

    public static void setupNewChatWith(String matchedUserID) {

        /* this will setup a new chat conversation between 2 users
         * NOTE - all these functions create for BOTH users
         * creates new entries under /users/userID/chats    and   /users/userID/chats_with_users
         * creates new entries in chats_members, and chats_messages
         * creates new entries in chats_meta
        */
        // CURRENT USER - create new entries
        DatabaseReference userIDRef = Constants.USERS_REF.child(Constants.userID);

        // new entry in /users/userID/chats -> chatID
        DatabaseReference userChatsRef = userIDRef.child("chats");
        DatabaseReference newChatIDRef = userChatsRef.push();
        String newChatID = newChatIDRef.getKey(); // user later in WithUser's DB entries
        newChatIDRef.setValue(true);

        // new entry in /users/userID/chats_with_users  -> userID
        DatabaseReference userChatsWithUsersRef = userIDRef.child("chats_with_users");
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("matchedUserID", true);
        userChatsWithUsersRef.setValue(map);

        // ------------------------------------------------

        // WITH USER - create new entries
        DatabaseReference withUserIDRef = Constants.USERS_REF.child(matchedUserID);
        // new entry in /users/userID/chats -> chatID
        DatabaseReference withUserChatsRef = withUserIDRef.child("chats");
        DatabaseReference withUserNewChatIDRef = withUserChatsRef.child(newChatID);
        withUserNewChatIDRef.setValue(true);



    }
}
