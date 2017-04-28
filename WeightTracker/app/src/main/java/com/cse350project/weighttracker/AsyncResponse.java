package com.cse350project.weighttracker;

/**
 * Created by jjn on 4/19/17.
 */
public interface AsyncResponse {
    void processImageResponse(String result);
    void submitWeightResponse(String result);
    void dataRequestResponse(String result);
}
