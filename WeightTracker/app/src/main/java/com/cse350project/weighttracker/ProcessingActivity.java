package com.cse350project.weighttracker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ProcessingActivity extends AppCompatActivity implements AsyncResponse {

    private String TAG = "ProcessingActivity";
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        String encodedImage = "";
        String filename = "";
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            filename = extras.getString("encodedImage");
        }

        try {
            FileInputStream inputStream = openFileInput(filename);
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            encodedImage = sb.toString();
            Log.d(TAG, encodedImage);
        }
        catch (Exception e) {
            Log.d(TAG, e.getMessage());
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
        TextView tv = (TextView) findViewById(R.id.resultTextView);
        tv.setText(result);
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
