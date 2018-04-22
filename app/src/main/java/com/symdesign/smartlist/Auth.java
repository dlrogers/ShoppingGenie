package com.symdesign.smartlist;

import android.content.SharedPreferences;
import android.os.AsyncTask;

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
import static com.symdesign.smartlist.MainActivity.prefs;
import static com.symdesign.smartlist.MainActivity.syncReg;

/**
 * An AdyncTask that sends an email, password and list
 * to the server in order to set up a new list/account
 * or delete an existing one. On input command can be
 * "new" "add" or "delete".
 * returns: "exists" if it already exists
 *          "ok" if a new account/list was created
 * Created by dennis on 2/2/17.
 */

class Auth extends AsyncTask<Void,Void,Boolean> {
    private String email, passwd, list, ans, cmd;
    private MainActivity activity;
    private URL url;
    private HttpURLConnection link;

    Auth(MainActivity a, String em, String pwd, String lst, String command) {
        this.activity = a;
        email = em;
        passwd = pwd;
        list = lst;
        cmd = command;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        InputStream is;
        OutputStream os;

        try {       // Send post request
            switch (cmd) {
                case "new" :
                    url = new URL(MainActivity.serverAddr + "auth.php");
                    break;
                case "add" :
                    url = new URL(MainActivity.serverAddr + "addlist.php");
                    break;
                case "del" :
                    url = new URL(MainActivity.serverAddr + "dellist.php");
            }
            link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoInput(true);
            link.setDoOutput(true);
            os=link.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            bos.write((email + "\n").getBytes("UTF-8"));
            bos.write((passwd + "\n").getBytes("UTF-8"));
            bos.write((list + "\n").getBytes("UTF-8"));
            bos.flush();
            is = link.getInputStream();
            BufferedReader  reader = new BufferedReader(new InputStreamReader(is));
            ans=reader.readLine();
            if(ans.equals("ok")) {
                syncReg = true;
                SharedPreferences.Editor ed = prefs.edit();     // Initialize shared preferences
                ed.putBoolean("syncReg",true);
                ed.apply();
            }
            is.close();
            os.close();
        } catch (MalformedURLException e) {
            log("Malformed URL: " + e.toString());
            activity.onFinishAuth(cmd,"error");
        } catch (IOException e) {
            log("IOException: " + e.getMessage());
            for(int i=0; i<4; i++) {
                log(e.getStackTrace()[i].toString());
                log(String.format(Locale.US,"    line no. = %d", e.getStackTrace()[i].getLineNumber()));
                activity.onFinishAuth(cmd,"error");
            }
        } finally {
//            log("Disconnecting");
            link.disconnect();
        }
    return true;
    }
    @Override
    protected void onPostExecute(Boolean result){
        if(cmd.equals("new"))
            activity.onFinishAuth(cmd,ans);
    }
}
