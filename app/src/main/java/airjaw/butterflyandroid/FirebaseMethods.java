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
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import airjaw.butterflyandroid.model.ReportedUser;

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
                uploadMeetMediaInfoToDatabase(mediaID, Constants.userID, toUserID, title, currentMediaTypeToUpload, context);

            }
        });

    }

    private static void uploadMediaInfoToDatabase(String title, String mediaID, Context context) {
        GeoFireGlobal.getUserLocation();
        Constants.geoFireMedia.setLocation(mediaID, GeoFireGlobal.getInstance().getLastLocation());

        // -----------FIREBASE DATABASE-----------
        //upload video info to Firebase real-time database (separate from the video file itself which goes to Firebase storage, with the title we generate above)

        // create and upload video info data
        SharedPreferences userInfoPrefs = context.getSharedPreferences(Constants.USER_FBINFO_PREFS, MODE_PRIVATE);
        String name = userInfoPrefs.getString("first_name", null);
        int age  = userInfoPrefs.getInt("age", 30); // TODO: change this default age someday
        String gender = userInfoPrefs.getString("gender", "none");
        Log.i("PREFS", name);
        Log.i("PREFS", Integer.toString(age));
        Log.i("PREFS", gender);

        Media_Info newMediaInfo = new Media_Info(age, gender, mediaID, name, title, Constants.userID);

        DatabaseReference newMediaInfoChildRef = Constants.MEDIA_INFO_REF.child(mediaID);
        newMediaInfoChildRef.setValue(newMediaInfo);
    }

    private static void uploadMeetMediaInfoToDatabase(String mediaID, String userID, String toUserID, String title,
                                                      String mediaType, Context context) {
        boolean unread = true;
        boolean unsent_notification = true;

        SharedPreferences userInfoPrefs = context.getSharedPreferences(Constants.USER_FBINFO_PREFS, MODE_PRIVATE);
        final String userGender = userInfoPrefs.getString("gender", "none");

        Meet_Media newMeetMedia = new Meet_Media(mediaID, mediaType, title, toUserID, Constants.userID, userGender, unread, unsent_notification);

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

                @Override
                public void fetchChatsMetaCompleted(ArrayList<ChatsMeta> chatsMeta) {

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
    public static void fetchChatsMeta(final FirebaseMethodsInterface callback) {

        DatabaseReference currentUserChatsMetaRef = Constants.CHATS_META_REF.child(Constants.userID);
        Query latestChatsQuery = currentUserChatsMetaRef.orderByChild("timestamp");

        latestChatsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<ChatsMeta> fetchedChatsMeta = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatsMeta chatMetaDic = snapshot.getValue(ChatsMeta.class);
                    // EXPLANATION: firebase has listeners, and these listeners fire in odd ways when you update data. sometimes it
                    // will return duplicate entries. until we fix our firebase database calls, we have this error checking code here
                    // that checks for duplicate chatID keys in our chatsMeta array.

                    boolean skipOldDuplicate = false;

                    Log.i("FetchChatsMeta" , chatMetaDic.getKey() + ": " + chatMetaDic.getTimestampLong());

                    for (int i = 0; i < fetchedChatsMeta.size(); i++) {
                        if (fetchedChatsMeta.get(i).getKey().equals(chatMetaDic.getKey())) {
                            Log.i("duplicate key: ", chatMetaDic.getKey());
                            skipOldDuplicate = true;
                        }
                    }

                    // we could do this in one "if" statement but I kept it if-else to match the iOS code
                    if (skipOldDuplicate) {

                    }
                    else {
                        fetchedChatsMeta.add(chatMetaDic);
                    }
                }

                // reverse array so that timestamps are now descending
                // TODO: one mystery is why this works fine on iOS, without reversing?
                Collections.reverse(fetchedChatsMeta);
                callback.fetchChatsMetaCompleted(fetchedChatsMeta);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public static void createChatsMessagesFor(String chatID, String senderId, String withUserID, String text) {
        DatabaseReference chatsMessagesRef = Constants.CHATS_MESSAGES_REF;
        DatabaseReference newChatsMessagesRef = chatsMessagesRef.child(chatID).push();

        ChatsMessage chatMessageDic = new ChatsMessage(text, senderId);
        newChatsMessagesRef.setValue(chatMessageDic);
        updateChatsMetaFor(chatID, text, senderId, withUserID);

    }
    private static void updateChatsMetaFor(String chatID, String text, String senderId, String withUserID) {
        // NOTE: This method updates chats_meta for BOTH USERS in the chat
        // currentUserId: update chats_meta with last message and last sender
        DatabaseReference chatsMetaUserRef = Constants.CHATS_META_REF.child(senderId);
        DatabaseReference newChatsMetaIDRef = chatsMetaUserRef.child(chatID);

//        ChatsMeta chatsMetaDic = new ChatsMeta(chatID, text, withUserID, senderId, unread, withUserName, unsent_notification);
        // don't need to create a new chatsMetaDic, just updating a few values  so use a hashmap

        Map<String, Object> chatsMetaDic = new HashMap<String, Object>();
        chatsMetaDic.put("lastMessage", text);
        chatsMetaDic.put("lastSender", senderId);
        chatsMetaDic.put("unread", false); // set to false because this is the currentUser own message
        chatsMetaDic.put("unsent_notification", false); // set to false because this is the currentUser own message
        chatsMetaDic.put("timestamp", Constants.firebaseServerValueTimestamp);

        newChatsMetaIDRef.updateChildren(chatsMetaDic);

        // withUserId: update chats_meta with last message and last sender
        DatabaseReference withUserChatsMetaRef = Constants.CHATS_META_REF.child(withUserID);
        DatabaseReference newWithUserChatsMetaIDRef = withUserChatsMetaRef.child(chatID);

        Map<String, Object> withUserChatsMetaDic = new HashMap<String, Object>();
        withUserChatsMetaDic.put("lastMessage", text);
        withUserChatsMetaDic.put("lastSender", senderId);
        withUserChatsMetaDic.put("unread", true);
        withUserChatsMetaDic.put("unsent_notification", true);
        withUserChatsMetaDic.put("timestamp", Constants.firebaseServerValueTimestamp);
        newWithUserChatsMetaIDRef.updateChildren(withUserChatsMetaDic);

    }

    public static void reportUserInDatabase(int type, String userIDToReport, String text) {
        // types: "inappropriateContent" (1), "spamOrFakeUser" (2), "harassment" (3), "other" (4)

        // reportedUsers
        //   userID:
        //      types: 1
        //      fromUser:
        //      toUser (not necessary? it's the key)
        //      text: ""

        DatabaseReference reportRef = Constants.REPORTED_USERS_REF.child(userIDToReport).push();

        ReportedUser newReport = new ReportedUser(type, Constants.userID, text);

        reportRef.setValue(newReport);

    }

    public static void blockUser(String userIDToBlock) {
        //   users/userID/blockedUserIDs:
        //          userID1: true, userID2:true

        DatabaseReference blockedUserIDsRef = Constants.USERS_REF.child(Constants.userID).child("/blockedUserIDs");

//        Map<String, Boolean> dic = new HashMap<String, Boolean>();
//        dic.put(userIDToBlock, true);

        blockedUserIDsRef.child(userIDToBlock).setValue(true);

        // add block for the reporting user too
        DatabaseReference reportingUserIDRef = Constants.USERS_REF.child(userIDToBlock).child("/blockedUserIDs");

//        HashMap<String, Boolean> dic2 = new HashMap<String, Boolean>();
//        dic2.put(Constants.userID, true);

        reportingUserIDRef.child(Constants.userID).setValue(true);

    }

    public static void deleteChatFor(String chatID, String matchedUserID) {
        /* this will remove all traces of a match/chat between 2 users
     * if one user calls this method, the chats are removed for both users.
     * the current user of the app is not passed in as a parameter because we Constants.userID for himself
     * REMOVE new entries under /users/userID/chats    and   /users/userID/chats_with_users
     * REMOVE new entries in chats_members, and chats_messages
     * REMOVE new entries in chats_meta under "users/userId1/ID" and "users/userID2/ID"
     */
        // FOR CURRENT USER
        //   /users/userID/chats
        DatabaseReference userIDRef = Constants.USERS_REF.child(Constants.userID);
        // delete entry in /users/userID/chats -> chatID
        DatabaseReference userChatsRef = userIDRef.child("chats");
        userChatsRef.child(chatID).removeValue();

        // delete entry in /users/userID/chats_with_users  -> userID
        DatabaseReference userChatsWithUsersRef = userIDRef.child("chats_with_users");
        userChatsWithUsersRef.child(matchedUserID).removeValue();

        // for MATCHED USER
        //   /users/userID/chats
        DatabaseReference matchedUserIDRef = Constants.USERS_REF.child(matchedUserID);
        // delete entry in /users/userID/chats -> chatID
        DatabaseReference matchedUserChatsRef = matchedUserIDRef.child("chats");
        matchedUserChatsRef.child(chatID).removeValue();

        // delete entry in /users/userID/chats_with_users  -> userID
        DatabaseReference matchedUserChatsWithUsersRef = matchedUserIDRef.child("chats_with_users");
        matchedUserChatsWithUsersRef.child(Constants.userID).removeValue();

        // REMOVE new entries in chats_members
        DatabaseReference chatsMembersRef = Constants.CHATS_MEMBERS_REF;
        DatabaseReference newChatsMembersRef = chatsMembersRef.child(chatID);
        newChatsMembersRef.removeValue();

        //REMOVE new entries in chats_messages
        DatabaseReference chatsMessagesRef = Constants.CHATS_MESSAGES_REF;
        chatsMessagesRef.child(chatID).removeValue();

        // REMOVE new entries in chats_meta under "users/userId1/currentUserID" and "users/userID2/matchedUserID"
        // for current user
        DatabaseReference chatsMetaUserRef = Constants.CHATS_META_REF.child(Constants.userID);
        DatabaseReference newChatMetaRef = chatsMetaUserRef.child(chatID);
        newChatMetaRef.removeValue();

        // for matched user
        DatabaseReference chatsMetaMatchedUserRef = Constants.CHATS_META_REF.child(matchedUserID);
        DatabaseReference newChatMetaRefForMatchedUser = chatsMetaMatchedUserRef.child(chatID);
        newChatMetaRefForMatchedUser.removeValue();
    }

}
