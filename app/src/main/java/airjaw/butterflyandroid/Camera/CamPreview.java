package airjaw.butterflyandroid.Camera;

/**
 * Created by airjaw on 2/17/17.
*/
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

public class CamPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    public static int rotate;
    private Context mContext;

    public CamPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mContext = context;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // create the surface and start camera preview
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        stopPreview();
        // set preview size and make any resize, rotate or
        // reformatting changes here
        setCamera(camera);

        // start preview with new settings
        startPreview();
    }

    public void stopPreview(){
        try {
            if(mCamera != null)
                mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
            e.printStackTrace();
        }
    }

    public void startPreview(){
        try {
            if(mCamera != null) {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }else{
                Log.d(VIEW_LOG_TAG, "Error starting camera preview: " );
            }
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        refreshCamera(mCamera);
    }

    public void setCamera(Camera camera) {
        //method to set a camera instance
        mCamera = camera;
        /**
         * add camera orientation and display rotation according to screen landscape or portrait
         */
        setCameraRotation();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        if(mCamera != null){
            mCamera.release();
        }

    }

    public void setCameraRotation() {
        try {

            Camera.CameraInfo camInfo = new Camera.CameraInfo();

            if (CamActivity.cameraId == 0)
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);
            else
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, camInfo);
            int cameraRotationOffset = camInfo.orientation;
            // ...

            Camera.Parameters parameters = mCamera.getParameters();


            int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break; // Natural orientation
                case Surface.ROTATION_90:
                    degrees = 90;
                    break; // Landscape left
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;// Upside down
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;// Landscape right
            }
            int displayRotation;
            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                displayRotation = (cameraRotationOffset + degrees) % 360;
                displayRotation = (360 - displayRotation) % 360; // compensate
                // the
                // mirror
            } else { // back-facing
                displayRotation = (cameraRotationOffset - degrees + 360) % 360;
            }

            mCamera.setDisplayOrientation(displayRotation);


            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotate = (360 + cameraRotationOffset + degrees) % 360;
            } else {
                rotate = (360 + cameraRotationOffset - degrees) % 360;
            }

            parameters.set("orientation", "portrait");
            parameters.setRotation(rotate);
            mCamera.setParameters(parameters);

        } catch (Exception e) {

        }
    }



}