package com.symdesign.smartlist;

/* @ Copyright 2017 Dennis Rogers
	Symbiotic Designs Confidential

	LostPW.java: Code to send lost password email
*/

import android.os.AsyncTask;
import static com.symdesign.smartlist.MainActivity.email;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static com.symdesign.smartlist.MainActivity.log;


/**
 * Created by dennis on 4/8/17.
 */

public class Lostpw extends AsyncTask<Void,Void,Boolean> {
    private String email;
    private URL url;
    HttpURLConnection link;
    String ans;

    public Lostpw(String em){
        email=em;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        OutputStream os;
        InputStream is;
        try {
            log("starting lostpw.java");
            url = new URL(MainActivity.serverAddr+"reset.php");
            link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoOutput(true);
            link.setDoInput(true);
            os=link.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            bos.write((email+"\n").getBytes("UTF-8"));
            bos.write((MainActivity.currList+"\n").getBytes("UTF-8"));
            bos.flush();
            is = link.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            ans=reader.readLine();
            is.close();
            os.close();
            bos.close();
        } catch (MalformedURLException e) {
            log("Malformed URL: " + e.toString());
        } catch (IOException e) {
            log("IOException: " + e.getMessage());
        } finally {
            link.disconnect();
        }
        return true;
    }
}