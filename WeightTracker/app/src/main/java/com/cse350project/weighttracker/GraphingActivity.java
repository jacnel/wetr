package com.cse350project.weighttracker;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.lang.reflect.Array;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class GraphingActivity extends AppCompatActivity implements AsyncResponse{

    private String TAG = "GraphingActivity";
    List<Long> xAxis;
    List<Float> yAxis;
    private XYPlot plot;
    private String period;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphing);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        xAxis = new ArrayList<>();
        yAxis = new ArrayList<>();

        // get period from user
        period = "week";

        getData();
    }

    private void getData() {
        ServerCommTask serverCommTask = new ServerCommTask(getString(R.string.server_domain));
        serverCommTask.setDelegate(this);
        serverCommTask.execute("dataRequest", period);
    }

    @Override
    public void processImageResponse(String result) {
        return;
    }

    @Override
    public void submitWeightResponse(String result) {
        return;
    }

    @Override
    public void dataRequestResponse(String result) {
        result = result.substring(1);
        result = result.substring(0, result.length() - 1);
        String[] results = result.split(",");
        for(int i = 0; i < results.length; i++) {
            String[] entry = results[i].split(";");
            Date date = stringToDate(entry[1]);
            xAxis.add(date.getTime());
            Float weight = Float.valueOf(entry[2]);
            yAxis.add(weight);
        }
        plotResults();
    }

    protected void plotResults() {
        plot = (XYPlot) findViewById(R.id.plot);

        // set range
        List<Float> sortedWeight = new ArrayList<>(yAxis);
        Collections.sort(sortedWeight);
        plot.setRangeBoundaries(sortedWeight.get(0).intValue() - 5, BoundaryMode.FIXED,
                sortedWeight.get(sortedWeight.size() - 1).intValue() + 5, BoundaryMode.FIXED);

        // set the title based on oldest measurement
        List<Long> sortedDate = new ArrayList<>(xAxis);
        Collections.sort(sortedDate);

        if(period.compareTo("week") == 0) {
            String day = parseDate(sortedDate.get(0));
            plot.getTitle().setText("Your Weight Since " + day);
        }

        XYSeries weights = new SimpleXYSeries(xAxis,yAxis,"weight");
        LineAndPointFormatter weightsFormat =
                new LineAndPointFormatter(Color.GREEN,Color.GREEN,null,null);

        plot.addSeries(weights, weightsFormat);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).getPaint().setAlpha(0);

        plot.redraw();
    }

    protected Date stringToDate(String s) {
        Log.d(TAG,s);

        // get the fields
        Integer year, month, day, hour, minute, second;
        year = Integer.valueOf(s.substring(0,4));
        month = Integer.valueOf(s.substring(5, 7));
        day = Integer.valueOf(s.substring(8, 10));
        hour = Integer.valueOf(s.substring(11, 13));
        minute = Integer.valueOf(s.substring(14, 16));
        second = Integer.valueOf(s.substring(17, 19));

        // convert UTC to local time
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, hour, minute, second);
        Calendar localTime = new GregorianCalendar(TimeZone.getDefault());
        localTime.setTimeInMillis(c.getTimeInMillis());
        return localTime.getTime();
    }

    protected String parseDate(Long l) {
        StringBuilder sb = new StringBuilder();
        Date d = new Date(l);
        Log.d(TAG,d.toString());
        sb.append(d.toString().substring(4,7) + " ");
        sb.append(d.toString().substring(8,10) + ", ");
        sb.append(d.toString().substring(24,28));
        return sb.toString();
    }
}
