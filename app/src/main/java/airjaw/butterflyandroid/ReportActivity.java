package airjaw.butterflyandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import static airjaw.butterflyandroid.FirebaseMethods.blockUser;
import static airjaw.butterflyandroid.FirebaseMethods.reportUserInDatabase;

public class ReportActivity extends AppCompatActivity {

    private static final String TAG = "ReportActivity";

    String userIDToReport;
    String withUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Intent intent = getIntent();
        userIDToReport = intent.getStringExtra("userIDToReport");
        withUserName = intent.getStringExtra("withUserName"); // save here to potentially use later

        Log.i(TAG, "reporting userID: " + userIDToReport + " with userName: " + withUserName);

        String title = "Report this User?";
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle(title);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void inappropriateContentButton(View view) {
        reportUserInDatabase(1, userIDToReport, "");
        showReportedAlert();
    }
    public void spamOrFakeUserButton(View view) {
        reportUserInDatabase(2, userIDToReport, "");
        showReportedAlert();
    }
    public void harassmentButton(View view) {
        reportUserInDatabase(3, userIDToReport, "");
        showReportedAlert();
    }
    public void otherButton(View view) {
        reportUserInDatabase(4, userIDToReport, "");
        showReportedAlert();
    }
    public void cancelButton(View view) {
        onSupportNavigateUp();
    }
    private void showReportedAlert(){
        String title = "Thank You For Reporting";
        String message = "Block this User Too? You Won't Be Able To See Their Introductions";

        AlertDialog alertDialog = new AlertDialog.Builder(ReportActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No Thanks",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onSupportNavigateUp();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Block",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        blockUser(userIDToReport);
                        showBlockedAlert();
                    }
                });

        alertDialog.show();
    }
    private void showBlockedAlert(){
        String title = "Success";
        String message = "User Blocked";

        AlertDialog alertDialog = new AlertDialog.Builder(ReportActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onSupportNavigateUp();
                    }
                });
        alertDialog.show();
    }
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
