package com.symdesign.smartlist;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import static com.symdesign.smartlist.LoginSettings.popupWindow;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.currList;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.mainActivity;


/**
 * @ Copyright Dennis Rogers 2/18/18
 * Created by dennis on 1/23/17.
 * Syncs a list on the phone with the server
 * returns: "ok" if password was ok and sync was successful
 *          "nok" if account does not exist or password is not valid
 */

class SyncList extends AsyncTask<Void,Void,Boolean>  {
    static String email,passwd,list;
    private ListView listView,suggestView;
    private MainActivity activity;
//	BufferedOutputStream bos;
	private static ContentValues values = new ContentValues();
    private static String col;
//	HttpURLConnection link;
    static Context context;
    static String errmsg = null;

    SyncList (MainActivity a, String em, String pwd, String lst, ListView lv,ListView sv) {
        this.activity=a;
        context = a;
        listView = lv;
        suggestView = sv;
        email = em;
        passwd = pwd;
        list = lst;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {

        sync(activity,email,passwd,list,listView,suggestView);
        SystemClock.sleep(2000)  ;
        return true;
    }

    static void sync(MainActivity a,String email,String passwd,String list,ListView listView,ListView suggestView) {
        InputStream is;
        OutputStream os;
        Cursor cursor;
        URL url;
        try {       // Send post request
            log("starting SyncList");
            long lastTime = System.currentTimeMillis();
            url = new URL(MainActivity.serverAddr + "sync.php");
            HttpURLConnection link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoInput(true);
            link.setDoOutput(true);

            os = link.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            bos.write((email + "\n").getBytes());
            bos.write((passwd + "\n").getBytes("UTF-8"));
            bos.write((list + "\n").getBytes("UTF-8"));
            bos.flush();
            //          Send list items to server
            String str = "";
            db = MainActivity.itemDb.getWritableDatabase();
            long ct = MainActivity.getTime() ;
            cursor = db.query("'" + MainActivity.currList + "'", SLAdapter.cols,
                    "flags=1 OR flags=3", null, "", "", null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int flgs = cursor.getInt(2);
                int dBid = cursor.getInt(0);
                str = String.format(Locale.getDefault(), "%s,%d,%d,%d,%.6e\n",
                        cursor.getString(1), flgs, cursor.getInt(3), cursor.getInt(4), cursor.getFloat(5));
                bos.write(str.getBytes("UTF-8"));
                if ((flgs & 2) > 0) { // If being deleted, delete copy in phone database
                    db.delete("'" + currList + "'", "name='" + cursor.getString(1) + "'", null);
                }
                log(str + "\n");
// ;
            }
            cursor = db.query("'" + MainActivity.currList + "'", SLAdapter.cols,
                    "flags=0 OR flags=2", null, "", "", null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int flgs = cursor.getInt(2);
                int dBid = cursor.getInt(0);
                if (flgs == 0 || flgs == 2) {
                    str = String.format(Locale.getDefault(), "%s,%d,%d,%d,%.6e\n",
                            cursor.getString(1), flgs, cursor.getInt(3), cursor.getInt(4), cursor.getFloat(5));
                    bos.write(str.getBytes("UTF-8"));
                }
                if ((flgs & 2) > 0) { //If being deleted, delete copy in phone database
                    db.delete("'" + currList + "'", "name='" + cursor.getString(1) + "'", null);
                }
                log(str + "\n");
            }
            cursor.close();
            bos.flush();
            //          Receive items from server that are not on phone
            is = link.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            log("Reading back from phone");
            String ans = reader.readLine();
            if (!(ans.equals("ok")))
                log("Sync failed!");
            while (null != (col = reader.readLine())) {
//                log(col);
                col = col.replaceAll("\n", "");
                String[] cols = col.split(",");
                switch (cols[0]) {
                    case "d":
                        log("deleting");
                        db.delete("'" + currList + "'", "name='" + cols[1] + "'", null);
                        break;
                    case "u":   //Time changed on server, update item
//                        int delbit = (Integer.parseInt(cols[2]))&2;
//                        if(delbit<1) {
                        values.clear();
                        values.put("name", cols[1]);
                        values.put("flags", cols[2]);
                        values.put("last_time", cols[3]);
                        values.put("last_avg", cols[4]);
                        values.put("ratio", cols[5]);
//                            log("name='"+cols[1]+"'");
                        db.update("'" + currList + "'", values, "name='" + cols[1] + "'", null);
//                        } else {//                           db.delete("'"+currList+"'",null,null);
//                        }
                        break;
                    case "i":   //Item added to server, insert into database
                        values.clear();
                        values.put("name", cols[1]);
                        values.put("flags", cols[2]);
                        values.put("last_time", cols[3]);
                        values.put("last_avg", cols[4]);
                        values.put("ratio", cols[5]);
                        db.insert("'" + currList + "'", null, values);
                        break;    // logF("cols changed = %d",id);
//                log(col);
//                String[] st = text.split(",");
                }
            }
            db = MainActivity.itemDb.getWritableDatabase();
            Cursor rows = db.query("'" + currList + "'", new String[]{"name", "flags"}, "flags=1", null, null, null, null);
            for (rows.moveToFirst(); !rows.isAfterLast(); rows.moveToNext()) {
//                logF("name = %s, flags = %d",rows.getString(0),rows.getInt(1));
            }
            logF("Sync time = %d", System.currentTimeMillis() - lastTime);
            rows.close();
            os.close();
            bos.close();
            is.close();
            link.disconnect();
        } catch (MalformedURLException e) {
            log("Malformed URL: " + e.getCause());
        } catch (IOException e) {
            log("IOException: " + e.getLocalizedMessage());
            for (int i = 0; i < 4; i++) {
                log(e.getStackTrace()[i].toString());
                log(String.format(Locale.getDefault(), "    line no. = %d", e.getStackTrace()[i].getLineNumber()));
            }
            if(mainActivity.popupWindow!=null) {
                mainActivity.popupWindow.dismiss();
                mainActivity.popupWindow = null;
            }
            errmsg = "Connection failed";
        } finally {
            log("Disconnecting Sync");
        }
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean exists) {
        MainActivity.updateAdapters(context,listView,suggestView);
        if(mainActivity.popupWindow!=null) {
            mainActivity.popupWindow.dismiss();
            mainActivity.popupWindow = null;
        }
        if(errmsg!=null){
            Toast toast = Toast.makeText(context,errmsg,Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, toast.getXOffset() / 2, toast.getYOffset() / 2);
            TextView textView = new TextView(context);
            textView.setBackgroundColor(Color.MAGENTA);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(20);
            Typeface typeface = Typeface.create("serif",Typeface.BOLD);
            textView.setTypeface(typeface);
            textView.setPadding(4, 4, 4, 4);
            textView.setText(errmsg);
            toast.setView(textView);
            toast.show();
        }
        log("SyncList finished");
    }
}
