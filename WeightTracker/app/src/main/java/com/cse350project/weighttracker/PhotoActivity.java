package com.cse350project.weighttracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;


public class PhotoActivity extends AppCompatActivity{

    private String TAG = "PhotoActivity";
    private Camera mCamera;
    private CameraPreview mPreview;
    private String mEncodedImage = "";
    Camera.PictureCallback jpeg;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        if(mCamera != null) {
//            Camera.CameraInfo info = new Camera.CameraInfo();
//            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
//            int degrees = this.getWindowManager().getDefaultDisplay().getRotation();
//            int rotate = (info.orientation - degrees + 360) % 360;
//            Camera.Parameters params = mCamera.getParameters();
//            params.setRotation(rotate + 1);
//            mCamera.setParameters(params);

            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            // remove and add back the button so it is on top
            View qButton = findViewById(R.id.button_capture);
            preview.removeView(qButton);
            preview.addView(qButton);
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            Log.d("getCameraInstance", "Number of cameras: " + Camera.getNumberOfCameras());
            for(int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.CameraInfo ci = new Camera.CameraInfo();
                Camera.getCameraInfo(i, ci);
                if(ci.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Log.d("getCameraInstance", "found camera");
                    c = Camera.open(i);
                    Camera.Parameters params = c.getParameters();
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    c.setParameters(params);
                    break;
                }
            }
        }
        catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public void captureImage(View view) {

        jpeg = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d(TAG, "onPictureTaken called");
                // create the image bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                // create final bitmap and String encoding
                // TODO: reduce picture to only include numbers
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                Log.d(TAG, image.toString());
                mEncodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                String filename = "img.encoded";
                try {
                    FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
                    fos.write(mEncodedImage.getBytes());
                    fos.close();
                }
                catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }


                // pass control to processing activity
                Intent intent = new Intent(PhotoActivity.this, ProcessingActivity.class);
                intent.putExtra("encodedImage", filename);
                finish();
                startActivity(intent);
            }
        };

        Camera.Parameters params = mCamera.getParameters();
        List focusModes = params.getSupportedFocusModes();
        if (focusModes.contains("auto")){
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if(success) camera.takePicture(null, null, jpeg);
                }
            });
        }else{
            mCamera.takePicture(null, null, jpeg);
        }
    }
}


