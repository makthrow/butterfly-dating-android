package airjaw.butterflyandroid;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

/**
 * Created by airjaw on 2/10/17.
 */

public class Constants {


    public static final Map<String, String> firebaseServerValueTimestamp = ServerValue.TIMESTAMP;
    //USER
    public static final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Firebase Storage
    public static final FirebaseStorage storage = FirebaseStorage.getInstance();
    public static final StorageReference storageRef = storage.getReferenceFromUrl("gs://butterfly2-ac0f9.appspot.com");
    public static final StorageReference storageMediaRef = storageRef.child("media");
    public static final StorageReference storageFBProfilePicRef = storageRef.child("fbProfilePic");

    // Firebase Database
    public static final FirebaseDatabase databaseRef = FirebaseDatabase.getInstance();

    // TABLE: "MEDIA_INFO"
    public static final DatabaseReference MEDIA_INFO_REF = databaseRef.getReference("media_info");

    // TABLE: location reference table for entries in media_info
    public static final DatabaseReference MEDIA_LOCATION_REF = databaseRef.getReference("media_location");

    // TABLE: "MEET" MEDIA  : Private introduction media ("video", "text", "picture") sent to other users
    // first key is for the userID it is being sent to eg. meet_media_for_userID/CXFmdsafnasdhf
    // second key is a mediaID, comprised of fromUserID-timestamp (fromUserID is userID of the sender)
    public static final DatabaseReference MEET_MEDIA_REF = databaseRef.getReference("meet_media_for_userID");

    // TABLE: "USERS"
    public static final DatabaseReference USERS_REF = databaseRef.getReference("users");

    // TABLE: "USER_LOCATIONS"
    public static final DatabaseReference USER_LOCATIONS_REF = databaseRef.getReference("user_locations");

    // TABLE: "CHATS_MEMBERS"
    public static final DatabaseReference CHATS_MEMBERS_REF = databaseRef.getReference("chats_members");

    // TABLE: "CHATS_META"
    public static final DatabaseReference CHATS_META_REF = databaseRef.getReference("chats_meta");

    // TABLE: "CHATS_MESSAGES"
    public static final DatabaseReference CHATS_MESSAGES_REF = databaseRef.getReference("chats_messages");

    // TABLE: "CONTACT"
    public static final DatabaseReference CONTACT_REF = databaseRef.getReference("contact");

    // TABLE: "REPORTED_USERS"
    public static final DatabaseReference REPORTED_USERS_REF = databaseRef.getReference("reported_users");

    // GeoFire
    public static final GeoFire geoFireUsers = new GeoFire(USER_LOCATIONS_REF);
    public static final GeoFire geoFireMedia = new GeoFire(MEDIA_LOCATION_REF);

    public static final long twentyFourHoursInMilliseconds = 86400000;

    public static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;



}
