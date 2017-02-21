package airjaw.butterflyandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

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
                uploadMediaInfoToDatabase(title, mediaID, context);
            }
        });
    }

    public static void uploadVideoToMeetMedia(Uri videoURL, final String title, final String toUserID, final String currentMediaTypeToUpload, final Context context) {
        // generate a timestamp
        long currentTimeInMilliseconds = System.currentTimeMillis();
        String videoTimeStamp = Long.toString(currentTimeInMilliseconds / 1000);

        // combine the two to form a unique video ID of userID + timestamp
        // use userID of sender.
        final String mediaID = Constants.userID + "-" + videoTimeStamp;
        Log.i(TAG, "uploading mediaID: " + mediaID);
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

                // create and upload video info data
                uploadMeetMediaInfoToDatabase(mediaID, Constants.userID, toUserID, title, currentMediaTypeToUpload);

            }
        });

    }

    private static void uploadMediaInfoToDatabase(String title, String mediaID, Context context) {
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

        Media_Info newMediaInfo = new Media_Info(age, gender, mediaID, name, title, Constants.userID);

        DatabaseReference newMediaInfoChildRef = Constants.MEDIA_INFO_REF.child(mediaID);
        newMediaInfoChildRef.setValue(newMediaInfo);
    }

    private static void uploadMeetMediaInfoToDatabase(String mediaID, String userID, String toUserID, String title,
                                                      String mediaType) {
        boolean unread = true;
        boolean unsent_notification = true;
        Meet_Media newMeetMedia = new Meet_Media(mediaID, mediaType, title, toUserID, Constants.userID, unread, unsent_notification);

        DatabaseReference meetMediaUserRef = Constants.MEET_MEDIA_REF.child(toUserID);
        DatabaseReference newMeetMediaChildRef = meetMediaUserRef.child(mediaID);
        newMeetMediaChildRef.setValue(newMeetMedia);

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
        map.put(matchedUserID, true);
        userChatsWithUsersRef.setValue(map);

        // ------------------------------------------------

        // WITH USER - create new entries
        DatabaseReference withUserIDRef = Constants.USERS_REF.child(matchedUserID);
        // new entry in /users/userID/chats -> chatID
        DatabaseReference withUserChatsRef = withUserIDRef.child("chats");
        DatabaseReference withUserNewChatIDRef = withUserChatsRef.child(newChatID);
        withUserNewChatIDRef.setValue(true);
        // new entry in /users/userID/chats_with_users  -> userID
        DatabaseReference withUserChatsWithUsersRef = withUserIDRef.child("chats_with_users");
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        map2.put(Constants.userID, true);
        withUserChatsWithUsersRef.setValue(map2);

        createChatsMembersFor(newChatID, Constants.userID, matchedUserID);

//        Date currentDate; //
        String introductionMessage = "You've met someone new! Let's say hi";

        // need to call this twice. once to create a new chats_meta for each user.
        createChatsMetaFor(newChatID, introductionMessage, Constants.userID, matchedUserID);
        createChatsMetaFor(newChatID, introductionMessage, matchedUserID, Constants.userID);

    }


    private static void createChatsMembersFor(String chatID, String user1, String user2) {
        // chats_members
        DatabaseReference chatsMembersRef = Constants.CHATS_MEMBERS_REF;
        DatabaseReference newChatsMembersRef = chatsMembersRef.child(chatID);

        HashMap<String, Object> chatsMembersMap = new HashMap<String, Object>();
        HashMap<String, Boolean> chatsMembersMapData = new HashMap<String, Boolean>();
        chatsMembersMapData.put(user1, true);
        chatsMembersMapData.put(user2, true);
        chatsMembersMap.put("users", chatsMembersMapData);

        /* iOS equivalent for reference
        chatsMembersDic = [
            "users" : [
                user1: true,
                user2: true
                ]
            ]
        */
        newChatsMembersRef.setValue(chatsMembersMap);
    }

    private static void createChatsMetaFor(final String chatID, final String lastMessage, String userID, final String matchedUserID) {

        DatabaseReference chatsMetaUserRef = Constants.CHATS_META_REF.child(userID);
        final DatabaseReference newChatMetaRef = chatsMetaUserRef.child(chatID);

        if (lastMessage != null) {
            getUserFacebookInfoFor(matchedUserID, new FirebaseMethodsInterface() {
                @Override
                public void getUsersFBInfoCompleted(Facebook_Info fbInfo) {
                    if (fbInfo != null) {
                        String withUserName = fbInfo.getFirst_name();
                        String lastSender = "none"; // no last sender upon creation
                        boolean unread = true;
                        boolean unsent_notification = true;

                        Log.i(TAG, "withUserName: " + withUserName);
                        ChatsMeta chatsMetaDic = new ChatsMeta(chatID, lastMessage, matchedUserID,
                                lastSender, unread, withUserName, unsent_notification);
                        newChatMetaRef.setValue(chatsMetaDic);
                    }
                }
                @Override
                public void checkIfUsersAreMatched(boolean matched) {

                }
            });
        }
    }

    private static void getUserFacebookInfoFor(String userID, final FirebaseMethodsInterface callback) {
        DatabaseReference userIDRef = Constants.USERS_REF.child(userID);
        DatabaseReference userFacebookInfoRef = userIDRef.child("facebook_info");

        userFacebookInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Facebook_Info info = dataSnapshot.getValue(Facebook_Info.class);
                callback.getUsersFBInfoCompleted(info);
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//
//
////                    String name = (String)snapshot.child("name").getValue();
////                    String gender = (String)snapshot.child("gender").getValue();
////                    String birthday = (String)snapshot.child("birthday").getValue();
////                    String first_name = (String)snapshot.child("first_name").getValue();
////                    String last_name = (String)snapshot.child("last_name").getValue();
////                    String pictureURL = (String)snapshot.child("pictureURL").getValue();
////                    String email = (String)snapshot.child("email").getValue();
//
////                    Facebook_Info FBUserInfoMap = new Facebook_Info(name, gender, first_name, last_name, email, birthday, pictureURL);
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static void checkIfMatched(String currentUserID, String withUserID, final FirebaseMethodsInterface callback) {
        // run a check to see if there is an existing match already in users/currentUseridxxx/chats_with_users/withUserIDxxx
        DatabaseReference currentUser_UsersRef = Constants.USERS_REF.child(currentUserID);
        DatabaseReference chats_with_users_ref = currentUser_UsersRef.child("chats_with_users");
        DatabaseReference chats_with_particular_user_ref = chats_with_users_ref.child(withUserID);

        chats_with_particular_user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.exists();
                callback.checkIfUsersAreMatched(exists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
