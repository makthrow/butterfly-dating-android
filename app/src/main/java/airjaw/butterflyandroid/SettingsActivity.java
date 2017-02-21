package airjaw.butterflyandroid;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myToolbar.setTitle("Settings");

        Context context = this;
        SharedPreferences settingsPrefs = context.getSharedPreferences(Constants.USER_SETTINGS_PREFS, MODE_PRIVATE);
        final SharedPreferences.Editor editor = settingsPrefs.edit();

        final Switch mensSwitch = (Switch) findViewById(R.id.meetMenSwitch);
        final Switch womensSwitch = (Switch) findViewById(R.id.meetWomenSwitch);

        boolean meetMenSwitchOn = settingsPrefs.getBoolean("meetMenSwitch", false);
        boolean meetWomenSwitchOn = settingsPrefs.getBoolean("meetWomenSwitch", false);

        mensSwitch.setChecked(meetMenSwitchOn);
        womensSwitch.setChecked(meetWomenSwitchOn);

        if (mensSwitch != null) {
            mensSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // do something, the isChecked will be
                    // true if the switch is in the On position
                    mensSwitch.setChecked(isChecked);
                    editor.putBoolean("meetMenSwitch", isChecked);
                    editor.commit();
                    Log.i(TAG, "meetMenSwitch changed to " + isChecked);
                }
            });
        }
        if (womensSwitch != null) {
            womensSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // do something, the isChecked will be
                    // true if the switch is in the On position
                    womensSwitch.setChecked(isChecked);
                    editor.putBoolean("meetWomenSwitch", isChecked);
                    editor.commit();
                    Log.i(TAG, "meetWomenSwitch changed to " + isChecked);
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void facebookLogout(View view) {
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
        gotoLogin();
    }

    private void gotoLogin() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
