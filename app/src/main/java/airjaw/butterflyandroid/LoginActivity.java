package airjaw.butterflyandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextView info;
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;

    private AccessToken token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        info = (TextView)findViewById(R.id.info);
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile", "email", "user_friends", "user_education_history", "user_birthday");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                /*
                info.setText(
                        "User ID: "
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                "Auth Token: "
                                + loginResult.getAccessToken().getToken()
                );
                */

                // check key permissions from facebook granted.
                // if user goes into facebook settings and revokes permissions later, need to handle that somehow
                // but we will have all of the data we need saved upon login

                if (verifyFacebookPermissionsGranted(loginResult)) {
                    // get an access token for the signed-in user, exchange it for a Firebase credential,
                    // and authenticate with Firebase using the Firebase credential:
                    token = loginResult.getAccessToken();
                    handleFacebookAccessToken(token);
                }
                else {
                    facebookPermissionsRequiredAlert();
                    mFirebaseAuth.signOut();
                    LoginManager.getInstance().logOut();
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                info.setText("Login attempt failed.");
            }
        });


        // Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();

                if (mFirebaseUser != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());

                    // Name, email address, and profile photo Url
                    String name = mFirebaseUser.getDisplayName();
                    String email = mFirebaseUser.getEmail();
                   // Uri photoUrl = user.getPhotoUrl();

                    // The user's ID, unique to the Firebase project. Do NOT use this value to
                    // authenticate with your backend server, if you have one. Use
                    // FirebaseUser.getToken() instead.
                    String uid = mFirebaseUser.getUid();
                    Log.i("debug", uid);

                    loggedInTransition();


                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void loggedInTransition() {

        Intent intent = new Intent(LoginActivity.this, MeetActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d("Facebook Login", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Facebook Login", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("Facebook Login", "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            //SUCCESS LOGIN
                            FacebookSDKMethods.getUserInfoFromFacebook(token, LoginActivity.this);
                        }
                    }
                });
    }

    private boolean verifyFacebookPermissionsGranted(LoginResult result) {
        if (result.getRecentlyDeniedPermissions().contains("user_birthday") ||
                result.getRecentlyDeniedPermissions().contains("user_friends") ||
                result.getRecentlyDeniedPermissions().contains("user_education_history")) {
            return false;
        }
        return true;
    }

    private void facebookPermissionsRequiredAlert() {
        String title = "Facebook Permissions";
        String message = "Butterfly requires you to provide additional Facebook permissions in order to create or use this service. This information is for your and other users' safety, provides more authenticity to profiles, and allows us to better provide support.";

        // Called upon login if user refuses to provide facebook permissions

        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ask Me",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}
