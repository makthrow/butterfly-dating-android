
package airjaw.butterflyandroid.Camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import airjaw.butterflyandroid.FirebaseMethods;
import airjaw.butterflyandroid.R;

public class CamActivity extends Activity {

    private Camera mCamera;
    private CamPreview mPreview;
    private MediaRecorder mediaRecorder;
    private ImageView capture, switchCamera;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM = 3;
    private static final int RC_HANDLE_RECORD_AUDIO_PERM = 4;
    private String TAG = "test";
    private TextView mTimerTv;
    private String filePath;
    private Intent intent;
    private long mCountDownTimer = 5000;
    public static int cameraId = -1;

    private Uri fileToUploadURL;
    private String uploadMediaTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        mTimerTv = (TextView) findViewById(R.id.tvTimer);

        File mediaFile =
                new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/testVideo.mp4");
        filePath = Uri.fromFile(mediaFile).getPath();
        fileToUploadURL = Uri.fromFile(mediaFile);
        intent   = getIntent();

        // PERMISSIONS
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int sdCard = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (sdCard == PackageManager.PERMISSION_GRANTED) {
            int recordAudio = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            if (recordAudio == PackageManager.PERMISSION_GRANTED) {
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    initialize();
                    initializeCamera();
                } else {
                    requestCameraPermission();
                }
            } else {
                requestRecordAudioPermission();
            }
        } else {
            requestSDCardPermission();
        }
    }

    private int findFrontFacingCamera() {
        cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED)
            initializeCamera();
    }

    private void initializeCamera() {
//        try {
//            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
//            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
//
//        } catch (Exception e) {
//            Log.e(getString(R.string.app_name), "failed to open Camera");
//            e.printStackTrace();
//        }

        if (mCamera == null) {
            // if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                switchCamera.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findBackFacingCamera());
            mPreview.refreshCamera(mCamera);
        }
    }

    public void initialize() {
        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);

        mPreview = new CamPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);

        capture = (ImageView) findViewById(R.id.button_capture);
        capture.setOnClickListener(captureListener);

        switchCamera = (ImageView) findViewById(R.id.button_ChangeCamera);
        switchCamera.setOnClickListener(switchCameraListener);
        capture.setSelected(true);
    }

    OnClickListener switchCameraListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the number of cameras
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    // release the old camera instance
                    // switch camera, from the front and the back and vice versa

                    releaseCamera();
                    chooseCamera();
                } else {
                    Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    };

    public void chooseCamera() {
        // if the camera preview is the front
        if (cameraFront) {
            cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview

                mCamera = Camera.open(cameraId);
                mPreview.refreshCamera(mCamera);
            }
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview

                mCamera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // when on Pause, release camera in order to be used from other
        // applications
        releaseCamera();
    }

    private boolean hasCamera(Context context) {
        // check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    boolean recording = false;
    OnClickListener captureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (recording) {
                didFinishRecording();


            } else {
                if (!prepareMediaRecorder()) {
                    Toast.makeText(CamActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                    finish();
                }
                // work on UiThread for better performance
                runOnUiThread(new Runnable() {
                    public void run() {
                        // If there are stories, add them to the table

                        try {
                            capture.setSelected(false);
                            mediaRecorder.start();
                            startCountDownTimer();
                        } catch (final Exception ex) {
                            // Log.i("---","Exception in thread");
                        }
                    }
                });

                recording = true;
            }
        }
    };

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private boolean prepareMediaRecorder() {

        mediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOrientationHint(CamPreview.rotate);

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));

        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.setMaxDuration(5000); // Set max duration 5 sec.
        mediaRecorder.setMaxFileSize(10000000); // Set max file size 10Mb

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }


    /**
     * Handles the requesting of the camera permission.
     */
    private void requestCameraPermission() {
        Log.w(CamActivity.class.getName(), "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(CamActivity.this, permissions,
                RC_HANDLE_CAMERA_PERM);

    }

    private void requestSDCardPermission() {
        Log.w(CamActivity.class.getName(), "SDCard permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(CamActivity.this, permissions,
                RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM);

    }

    private void requestRecordAudioPermission() {
        Log.w(CamActivity.class.getName(), "Record audio permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        ActivityCompat.requestPermissions(CamActivity.this, permissions,
                RC_HANDLE_RECORD_AUDIO_PERM);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case RC_HANDLE_CAMERA_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //cameraView.setVisibility(View.VISIBLE);
                    initialize();
                    initializeCamera();
                } else {
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setMessage("This application cannot record video because it does not have the camera permission.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
                break;
            case RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestRecordAudioPermission();
                } else {
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setMessage("This application cannot record video because it does not have the write external storage permission.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
                break;
            case RC_HANDLE_RECORD_AUDIO_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission();
                } else {
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setMessage("This application cannot record video because it does not have the record audio permission.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
                break;

        }
    }

    private void startCountDownTimer(){
        new CountDownTimer(mCountDownTimer, 1000) {

            public void onTick(final long millisUntilFinished) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTimerTv.setText("" + millisUntilFinished / 1000);
                    }
                });

            }

            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTimerTv.setText("done!");
                        if (recording) {
                            didFinishRecording();
                        }
                    }
                });

            }
        }.start();
    }

    private void releaseCameraAndPreview() {
        mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void presentAddCommentAlert() {
        // uploadVideoToMediaInfo
        uploadMediaTitle = "test title"; // TODO: implement real title by user

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.add_title, (ViewGroup) findViewById(android.R.id.content), false);
        // Set up the input
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                uploadMediaTitle = input.getText().toString();
                FirebaseMethods.uploadVideoToMediaInfo(fileToUploadURL, uploadMediaTitle, CamActivity.this);

            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();


    }

    private void didFinishRecording() {
        // stop recording and release camera
        mediaRecorder.stop(); // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        Toast.makeText(CamActivity.this, "Video captured!", Toast.LENGTH_LONG).show();
        capture.setSelected(true);
        recording = false;

        presentAddCommentAlert();
    }

}
