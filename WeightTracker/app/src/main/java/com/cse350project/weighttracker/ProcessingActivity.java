package com.cse350project.weighttracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

public class ProcessingActivity extends AppCompatActivity implements AsyncResponse {

    private String TAG = "ProcessingActivity";
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        String encodedImage = "";
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            encodedImage = extras.getString("encodedImage");
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        if(encodedImage.compareTo("") != 0) {
            progressBar.setVisibility(View.VISIBLE);
            sendImageToServer(encodedImage);
        }
        else {
            Log.e(TAG, "Error: no encoded image...");
            System.exit(1);
        }
    }

    @Override
    public void processResponse(String result) {
        // do something with the result
        Log.d(TAG, "Response: " + result);
        setContentView(R.layout.processing_done);
    }

    public void sendImageToServer(String image) {
        // get the server communication ready
        ServerCommTask sendImage = new ServerCommTask(getString(R.string.server_domain));
        sendImage.setDelegate(this);
        sendImage.execute("sendImage", image);
    }

    public void restart(View v) {
        Intent intent = new Intent(this, PhotoActivity.class);
        finish();
        startActivity(intent);
    }
}
