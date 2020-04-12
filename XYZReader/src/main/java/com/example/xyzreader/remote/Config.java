package com.example.xyzreader.remote;

import android.util.Log;

import com.example.xyzreader.util.Constants;

import java.net.MalformedURLException;
import java.net.URL;

class Config {
    static final URL BASE_URL;
    private static String TAG = Config.class.toString();

    static {
        URL url = null;
        try {
            url = new URL(Constants.BASE_URL);
        } catch (MalformedURLException e) {
            // TODO: throw a real error
            Log.e(TAG, "Problem building the URL.", e);
            Log.e(TAG, "Please check your internet connection.");
        }

        BASE_URL = url;
    }
}