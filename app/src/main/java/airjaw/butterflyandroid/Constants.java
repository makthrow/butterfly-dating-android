package airjaw.butterflyandroid;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by airjaw on 2/10/17.
 */

public class Constants {

    //USER
    public static final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Firebase Storage
    public static final FirebaseStorage storage = FirebaseStorage.getInstance();
    public static final StorageReference storageRef = storage.getReferenceFromUrl("gs://butterfly2-ac0f9.appspot.com");
    public static final StorageReference storageMediaRef = storageRef.child("media");
    public static final StorageReference storageFBProfilePicRef = storageRef.child("fbProfilePic");

    // Firebase Database
    public static final FirebaseDatabase databaseRef = FirebaseDatabase.getInstance();
    public static final DatabaseReference MEDIA_INFO_REF = databaseRef.getReference("media_info");

}
