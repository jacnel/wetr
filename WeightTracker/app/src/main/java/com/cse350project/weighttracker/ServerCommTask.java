package com.cse350project.weighttracker;

import android.app.Activity;
import android.content.res.Resources;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by jjn on 4/17/17.
 */
public class ServerCommTask extends AsyncTask<String, Void, String> {

    private String TAG = "ServerCommTask";
    private AsyncResponse asyncResponse;
    private String link;

    public ServerCommTask(String link) {
        this.link = link;
    }

    @Override
    protected String doInBackground(String... params) {
        if(params[0].compareTo("sendImage") == 0) {
            // send information to the server via HTTP post request
            try {
                String encodedImage = params[1];
                String data = URLEncoder.encode("encoded_image", "UTF-8") + "=" + URLEncoder.encode(encodedImage, "UTF-8");

                URL url = new URL("http://" + link);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                osw.write(data);
                osw.flush();
                osw.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                Log.d(TAG, sb.toString());
                return sb.toString();
            }
            catch (Exception e){
                Log.d(TAG, "Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    // this allows the communication back to the PhotoActivity
    public void setDelegate(AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
    }

    @Override
    public void onPostExecute(String result) {
        asyncResponse.processResponse(result);
    }
}
