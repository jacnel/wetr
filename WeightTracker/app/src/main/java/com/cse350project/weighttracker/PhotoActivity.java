package com.cse350project.weighttracker;

import android.content.Intent;
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


public class PhotoActivity extends AppCompatActivity{

    private String TAG = "PhotoActivity";
    private Camera mCamera;
    private CameraPreview mPreview;
    private String mEncodedImage = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        if(mCamera != null) {
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
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
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

        Camera.PictureCallback jpeg = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d(TAG, "onPictureTaken called");
                // create the image bitmap
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);

                // create final bitmap and String encoding
                // TODO: reduce picture to only include numbers
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                mEncodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // pass control to processing activity
                Intent intent = new Intent(PhotoActivity.this, ProcessingActivity.class);
                intent.putExtra("encodedImage", mEncodedImage);
                finish();
                startActivity(intent);
            }
        };

        mCamera.takePicture(null, null, jpeg);
    }
}

