package airjaw.butterflyandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import airjaw.butterflyandroid.Camera.CamSendMeetActivity;

public class SettingsContactUsActivity extends AppCompatActivity {

    private String inputText;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_contact_us);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Contact Us");
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText = (EditText) findViewById(R.id.editText);
        int maxLength = 1000;
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});

    }


    private void showSentNotification() {
        String toastText = "Sent! Thank you for contacting us.";
        Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_LONG);
        toast.show();
    }

    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void submitButtonPressed(View view) {

        inputText = editText.getText().toString();

        SharedPreferences prefs = this.getSharedPreferences(Constants.USER_FBINFO_PREFS, MODE_PRIVATE);
        String firstNameFromPrefs = prefs.getString("first_name", null);
        int ageFromPrefs  = prefs.getInt("age", 25); // TODO: change this default age someday
        String genderFromPrefs = prefs.getString("gender", null);
        String emailFromPrefs = prefs.getString("email", "");

        ContactObject contactDic = new ContactObject(inputText, firstNameFromPrefs, genderFromPrefs, emailFromPrefs, ageFromPrefs);

        Constants.CONTACT_REF.push().setValue(contactDic);

        showSentNotification();

        onSupportNavigateUp();
    }
}
