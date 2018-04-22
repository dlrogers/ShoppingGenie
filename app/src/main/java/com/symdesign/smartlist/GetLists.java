package com.symdesign.smartlist;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import static android.R.attr.font;
import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.currList;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.mainActivity;
import static com.symdesign.smartlist.MainActivity.passwd;
import static com.symdesign.smartlist.MainActivity.showToast;


/**
 * @ Copyright Dennis Rogers 8/9/17
 * Created by dennis on 8/9/17.
 * Receives and email and password from a phone and 
 * sends all lists corresponding to that email.
 * returns: "ok" if email and password are ok followed by lists data
 *          "nok" if account does not exist or password is not valid
 */

class GetLists extends AsyncTask<Void,Void,Boolean> {
    private ListView listView,suggestView;
    private MainActivity activity;
    //	BufferedOutputStream bos;
    private static ContentValues values = new ContentValues();
    private static String col;
    //	HttpURLConnection link;
    private URL url;
    static Context context;
    String nemail,npasswd;
    Boolean sync;
    static boolean started = false, ok=true;
    static String errmsg = null;

    final String table_row = "(_id INTEGER PRIMARY KEY, name TEXT, flags INT, last_time INT, last_avg INT, ratio REAL)";

    GetLists (MainActivity a, String em, String pwd, Boolean syc) {
        this.activity=a;
        context = a;
        nemail = em;
        npasswd = pwd;
        sync = syc;
    }
    public interface LoginListener {
        public void fin();
    }

    LoginListener loginListener;

    public void setListener(LoginListener listener)
    {
        this.loginListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        InputStream is;
        OutputStream os;
        String[] rows;
        String line;

        if(sync)
            SyncList.sync(activity,MainActivity.email,MainActivity.passwd,MainActivity.currList,
                MainActivity.listView,MainActivity.suggestView);

        try {       // Send post request
            log("starting GetLists");
//            long lastTime = System.currentTimeMillis();\
            started = false;
            url = new URL(MainActivity.serverAddr+"export.php");
            HttpURLConnection link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoInput(true);
            link.setDoOutput(true);
            // Get output stream and send email and password
            os = link.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            bos.write((nemail + "\n").getBytes());
            bos.write((npasswd + "\n").getBytes("UTF-8"));
            bos.flush();
            //  Get Input stream
            is = link.getInputStream();
            // Access Database
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            ItemDb itemDb = new ItemDb(context);
            db = itemDb.getWritableDatabase();
            String ans = reader.readLine();
            // Error message if email not ok or password wrong
            if ((!ans.equals("ok"))){
                ok = false;
                logF("not ok");
            }
            else {
                started = true;
                ok = true;
                email=nemail;
                passwd=npasswd;
                // update Shared Preferences
                SharedPreferences.Editor ed = MainActivity.prefs.edit();
                ed.putString("email",nemail);
                ed.putString("passwd",npasswd);
                ed.putString("currList","Groceries");
                ed.putBoolean("syncReg",false);
                ed.apply();
                // Remove existing lists except Groceries
                log("Removing existing lists");
                Cursor listsCursor = db.query("lists", new String[] {"_id","name"}, null, null, null, null, null);
                for (listsCursor.moveToFirst(); !listsCursor.isAfterLast(); listsCursor.moveToNext()) {
                    String listName = listsCursor.getString(1);
                    logF("list = %s", listName);
                    db.execSQL("DROP TABLE IF EXISTS '"+listName+"'");
                    db.execSQL("DELETE FROM lists where name='"+listName+"'");
                }
                db.execSQL(MainActivity.SQL_CREATE_GROCERIES);
//                values.put("name","Groceries");
//                db.insert("lists",null,values);
//                listsCursor = db.query("lists", new String[] {"_id","name"}, null, null, null, null, null);
                log("List of lists entries");
                for (listsCursor.moveToFirst(); !listsCursor.isAfterLast(); listsCursor.moveToNext()) {
                    logF("lists item = %s",listsCursor.getString(1));
                }
//                db.delete("lists","name != Groceries",null);
                line = reader.readLine();
                do {
                    // Read list name and insert in lists
                    String listName = line;
                    values.clear();
                    values.put("name",listName);
                    db.insert("lists",null,values);
                    String sql = "CREATE TABLE IF NOT EXISTS "+listName+table_row;  //  Add list table
                    logF("sql = %s",sql);
                    db.execSQL(sql);
                    line = reader.readLine();     // Read first list item
                    while(line.contains(",")){
                        rows = line.split(",");     // Split out columns
                        //  Insert into table
                        values.clear();
                        values.put("name", rows[0]);
                        values.put("flags", rows[1]);
                        values.put("last_time", rows[2]);
                        values.put("last_avg", rows[3]);
                        values.put("ratio", rows[4]);
    //                            log("name='"+rows[1]+"'");
                        db.insert(listName,null,values);
                        line = reader.readLine();
                    }
                } while(!line.equals("end"));
            }
            os.close();
            bos.close();
            is.close();
            link.disconnect();
        } catch (MalformedURLException e) {
            log("Malformed URL: " + e.toString());
        } catch (IOException e) {
            log("IOException: " + e.getMessage());
            for(int i=0; i<4; i++) {
                log(e.getStackTrace()[i].toString());
                log(String.format(Locale.getDefault(),"    line no. = %d", e.getStackTrace()[i].getLineNumber()));
            }
            if(mainActivity.popupWindow!=null) {
                mainActivity.popupWindow.dismiss();
                mainActivity.popupWindow = null;
            }
            errmsg="Connection failed";
        } finally {
            log("Disconnecting GetLists");
        }
        SystemClock.sleep(2000)  ;
        return true;
    }
    @Override
    protected void onPostExecute(Boolean exists) {
        if(!ok)
            showToast(context,"\nAccount not found!\n",Toast.LENGTH_LONG);
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
        MainActivity.updateAdapters();
        showLists(MainActivity.context);
        loginListener.fin();
    }
    public void showLists(Context context) {
        Cursor listsCursor = db.query("lists", new String[] {"_id","name"}, null, null, null, null, null);
        logF("listsCursor count = %d",listsCursor.getCount());
        SimpleCursorAdapter adpt = new SimpleCursorAdapter(
                context, R.layout.lists_layout,
                listsCursor,new String[] {"name"},new int[]{R.id.name}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        MainActivity.lists.setAdapter(adpt);
//        listsCursor.close();
    }
}
