package com.symdesign.smartlist;

import java.io.IOException;
import java.lang.String;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dennis on 2/18/16.
 */
public class Sync {

    HttpURLConnection urlConnection;

    public HttpURLConnection Sync(java.lang.String link) {
        try {
            URL url = new URL(link);
            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            MainActivity.log("Malformed URL");
        } catch (IOException e) {
            MainActivity.log("IOException");
        }

        MainActivity.log("No URL errors");
        return urlConnection;
    }
}
