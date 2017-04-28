package com.cse350project.weighttracker;

import android.os.AsyncTask;
import android.util.Log;

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
    private String domain_name;
    private int responseNum = 0;

    public ServerCommTask(String domain_name) {
        this.domain_name = domain_name;
    }

    @Override
    protected String doInBackground(String... params) {
        if(params[0].compareTo("sendImage") == 0) {
            // send information to the server via HTTP post request
            try {
                String encodedImage = params[1];
                String data = URLEncoder.encode("encoded_image", "UTF-8") + "=" + URLEncoder.encode(encodedImage, "UTF-8");

                URL url = new URL("http://" + domain_name + "/process_img");
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

                responseNum = 1;
                return sb.toString();
            }
            catch (Exception e){
                Log.d(TAG, "Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        else if (params[0].compareTo("submitWeight") == 0) {

            try {
                String name = params[1];
                String weight = params[2];
                String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
                data += "&" + URLEncoder.encode("weight", "UTF-8") + "=" + URLEncoder.encode(weight, "UTF-8");

                URL url = new URL("http://" + domain_name + "/submit");
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                osw.write(data);
                osw.flush();
                osw.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                responseNum = 2;
                return sb.toString();
            } catch (Exception e) {
                Log.d(TAG, "Error: " + e.getMessage());
                e.printStackTrace();
            }

        }

        else if (params[0].compareTo("dataRequest") == 0) {
            try {
                String period = params[1];
                String data = URLEncoder.encode("period", "UTF-8") + "=" + URLEncoder.encode(period, "UTF-8");

                URL url = new URL("http://" + domain_name + "/data_request");
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                osw.write(data);
                osw.flush();
                osw.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                responseNum = 3;
                return sb.toString();
            } catch (Exception e) {
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
        switch(responseNum) {
            case 1:
                asyncResponse.processImageResponse(result);
                break;
            case 2:
                asyncResponse.submitWeightResponse(result);
                break;
            case 3:
                asyncResponse.dataRequestResponse(result);
            default:
                break;
        }

    }
}
