package airjaw.butterflyandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.face.Face;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static android.R.attr.bitmap;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by airjaw on 2/12/17.
 */

// bitmap BasicImageDownloader example code here:
// https://github.com/vad-zuev/ImageDownloader/blob/master/app/src/main/java/com/so/example/activities/ImageActivity.java

public class FacebookSDKMethods {
    private static final String TAG = "FacebookSDKMethods";

    private static final FacebookSDKMethods instance = new FacebookSDKMethods();
    public static FacebookSDKMethods getInstance() {
        return instance;
    }

    public static void getUserInfoFromFacebook(AccessToken accessToken, final Context context) {

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {

                        FacebookRequestError error = response.getError();
                        if (error == null) {
                            Log.i("JSON", object.toString());
                            try {
                                String name = object.getString("name");
                                String gender = object.getString("gender");
                                String firstName = object.getString("first_name");
                                String lastName = object.getString("last_name");
                                String email = object.getString("email");
                                String facebookID = object.getString("id");
                                String birthdayString = object.getString("birthday");

                                String urlString = "https://graph.facebook.com/" + facebookID + "/picture?type=large";
                                Log.i(TAG, urlString);

                                final StorageReference profilePicStorageRef = Constants.storageFBProfilePicRef.child(Constants.userID);

                                final BasicImageDownloader downloader = new BasicImageDownloader(new BasicImageDownloader.OnImageLoaderListener() {
                                    @Override
                                    public void onError(BasicImageDownloader.ImageError error) {
                                        Toast.makeText(context, "Error code " + error.getErrorCode() + ": " +
                                                error.getMessage(), Toast.LENGTH_LONG).show();
                                        error.printStackTrace();

                                    }
                                    @Override
                                    public void onProgressChange(int percent) {
                                    }

                                    @Override
                                    public void onComplete(Bitmap result) {
                                        final Bitmap.CompressFormat mFormat = Bitmap.CompressFormat.JPEG;

                                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                        result.compress(mFormat, 100, outputStream);
                                        byte[] data = outputStream.toByteArray();

                                        UploadTask uploadTask = profilePicStorageRef.putBytes(data);
                                        uploadTask.addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // Handle unsuccessful uploads
                                                Log.i(TAG, "FBStorage upload profile pic exception: " + exception.toString());
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                                Log.i(TAG, "Fb profile pic successfully uploaded");
                                            }
                                        });
                                    }
                                });
                                downloader.download(urlString, true);

                                // save basic settings in shared preferences: age, gender, first name

                                SharedPreferences.Editor editor = context.getSharedPreferences(Constants.USER_FBINFO_PREFS, MODE_PRIVATE).edit();

                                editor.putString("first_name", firstName);
                                editor.putString("gender", gender);

                                if (birthdayString != null) {
                                    int currentUserAge = Helper.calculateAgeFromDateString(birthdayString);
                                    editor.putInt("age", currentUserAge);
                                    Log.i(TAG, Integer.toString(currentUserAge));
                                }
                                editor.commit();

                                SharedPreferences prefs = context.getSharedPreferences(Constants.USER_FBINFO_PREFS, MODE_PRIVATE);
                                String firstNamePrefs = prefs.getString("first_name", null);
                                int agePrefs  = prefs.getInt("age", 30); // TODO: change this default age someday
                                String genderPrefs = prefs.getString("gender", null);
                                Log.i("PREFS", firstNamePrefs);
                                Log.i("PREFS", Integer.toString(agePrefs));
                                Log.i("PREFS", genderPrefs);

                                Facebook_Info fbInfoDic = new Facebook_Info(name, gender, firstName, lastName, email, birthdayString, urlString);
                                uploadFBUserInfo(fbInfoDic);

                            }


                            catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                        else {
                            Log.i("FacebookSDK", "Error code: " + error.getErrorCode());
                            Log.i("FacebookSDK", "Error message: " + error.getErrorMessage());
                        }


                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name, gender, picture, birthday, first_name, last_name, email");
        request.setParameters(parameters);
        request.executeAsync();
    }
    private static void uploadFBUserInfo(Facebook_Info info) {

        //userFacebookInfoRef
        DatabaseReference userIDRef = Constants.USERS_REF.child(Constants.userID);
        DatabaseReference userFacebookInfoRef = userIDRef.child("facebook_info");

        userFacebookInfoRef.setValue(info);
    }

}
