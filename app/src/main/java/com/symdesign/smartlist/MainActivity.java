// master branch

package com.symdesign.smartlist;

import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;
import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.passwd;
import static com.symdesign.smartlist.PickList.dBid;
import static com.symdesign.smartlist.SLAdapter.*;
import static com.symdesign.smartlist.SLAdapter.itemsList;
import static com.symdesign.smartlist.SLHandler.MSG_SYNC;
import static com.symdesign.smartlist.SLHandler.MSG_REPEAT;

//  Version of SmartList that uses file to store database on phone

/**
 * @ Copyright Dennis Rogers 2/18/18
 * Created by dennis on 1/23/18.
 * Syncs a list on the phone with the server
 * returns: "ok" if password was ok and sync was successful
 *          "nok" if account does not exist or password is not valid
 */

public class MainActivity extends AppCompatActivity implements AdminDialog.AdminDialogListener,
        NewListDialog.Listener, LongClickDialog.Listener, PickList.Listener, GetLists.LoginListener {

    private PickList.Listener pickListener;
    static Context context;
    static ItemDb itemDb;
//    static String serverAddr="http://sym-designs.com/cgi-bin/";
//    static String serverAddr="http://192.168.1.21/php/";
    static String serverAddr="http://symdesign.us/php/";
    static ContentValues values = new ContentValues();
    public static SQLiteDatabase db;
    public static ListView listView, suggestView,lists;
    static String adminURL;

    enum ClickLocation {none, del, name, box}
    static ClickLocation clickLocation;
    static final long second = 1, minute = 60 * second, hour = 60 * minute,
            day = 24 * hour, week = 7 * day, refresh_time = 10*minute, sync_time = 10*minute,
            autosync_time = 5*minute,
            max_time = 60*day;
    static int scrn_width, scrn_height, VOICE_RECOGNITION_REQUEST_CODE = 2;
    static final String SQL_CREATE_GROCERIES =
            "CREATE TABLE Groceries(_id INTEGER PRIMARY KEY, name TEXT, flags INT, last_time INT, last_avg INT, ratio REAL)";
        // name =       name of list item
        // flags =     flags: bito ? (in shopping list) : 1 (in suggest list)
        //                     bit1 ? (to be deleted) : (normal)
        // last_time =  Last time item bought (in seconds since epoch
        // last_avg =   Running averate of times between buys
        // ratio =      (time since last purchase)/last_avg
    static final String SQL_LISTS = "CREATE TABLE lists(_id INTEGER PRIMARY KEY, name TEXT)";
    static AssetManager assetManager;
    static Toast toast;
    static ArrayList<String> deleteList = new ArrayList<>();
//    static ArrayList<ListItem> listTable = new ArrayList<>(5);
    static String currList,currTable,installed,email,passwd;     // Current Shared Prefs
    static android.support.v7.app.ActionBar actionBar;
    static SharedPreferences prefs;
    static Cursor listsCursor;
    static Boolean syncReg,autoSync,vibrate;
    static Typeface headFont;
    static ScrollView scrollView;
    static PickList pickList;
    static MainActivity mainActivity;
    static Handler slHandler;
    static String[] cols = new String[]
            {"_id","name","flags","last_time","last_avg","ratio"};
    static TextView listHeading;
    static PopupWindow popupWindow;
    static AsyncTask syncList,auth;
    static GetLists getLists;
    static long time_millis;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        mainActivity = this;
        itemDb = new ItemDb(getContext());
        db = itemDb.getWritableDatabase();
        prefs = getPreferences(MODE_PRIVATE);
//        Code to clear prefs and databases
/*        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
        */
//        db.execSQL("drop table if exists itemDb");
//        db.execSQL("drop table if exists 'Grocery List'");
//        db.execSQL("drop table if exists lists");
/*        db.execSQL("drop table if exists 'Garden Supplies'");
        db.delete("lists","name='Garden supplies'",null);
        SharedPreferences.Editor ed = prefs.edit();     // Initialize shared preferences
        ed.putString("currList","Groceries");
        ed.putString("currTable","Groceries");
        ed.putString("installed","yes");
        ed.apply();
*/
        //        db.execSQL("drop table if exists lists");
//        db.execSQL(SQL_LISTS);

//        Code to do one time install code

        installed = prefs.getString("installed", "no");
        String syncEnabled = prefs.getString("sync", "no");
//        db.execSQL("DROP TABLE lists");
//        installed = "no";
        if (installed.equals("no")) {
            db.execSQL("drop table if exists Groceries");   // Remove any exisitng Groceries
            db.execSQL("drop table if exists lists");
            SharedPreferences.Editor ed = prefs.edit();     // Initialize shared preferences
            ed.putString("installed", "yes");
            ed.putString("currList", "Groceries");
            ed.putString("email", "no_email");
            ed.putString("passwd", "no_passwd");
            ed.putBoolean("syncReg", false);
            ed.putBoolean("autoSync", true);
            ed.putBoolean("vibrate", true);
            ed.apply();
            db.execSQL(SQL_LISTS);                          // Create "lists" table
            NewListDialog.addToLists("Groceries");
            currList = "Groceries";
            db.execSQL(SQL_CREATE_GROCERIES);
        }
//        db.execSQL(SQL_LISTS);                          // Create "lists" table
        setupOptions();     // Setup up sidebar
        showLists(context);
        printLists();

        email = prefs.getString("email", "no_email");
        passwd = prefs.getString("passwd", "no_passwd");
        syncReg = prefs.getBoolean("syncReg", false);
        currList = prefs.getString("currList", "Groceries");
        autoSync = prefs.getBoolean("autoSync", true);
        vibrate = prefs.getBoolean("vibrate", true);
//        currList = "Groceries";
/*        SharedPreferences.Editor ed = prefs.edit();     // Initialize shared preferences
        ed.putBoolean("syncReg",true);
        ed.apply();
        syncReg = true;
*/
        showLists();

//        db.execSQL("drop table if exists Groceries");   // code to reset default list to Groceries
//        db.execSQL(SQL_CREATE_GROCERIES);
//        NewListDialog.addToLists("Groceries");
/*        currList = "Groceries";
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("currList","Groceries");
        ed.apply();
*/
        currTable = prefs.getString("currTable", "Groceries");

        assetManager = getAssets();
        PickList.prepareListData();
        // Get view links
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(currList);
        deleteList.clear();

//        Setup left drawer
        drawers = (DrawerLayout) findViewById(R.id.drawer_layout);
//        toolbar.setNavigationIcon(R.drawable.ic_menu_moreoverflow_normal_holo_dark);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_more);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawers.openDrawer(navList);
            }
        });
        clickLocation = ClickLocation.none;

        // Get view links

        listView = (ListView) findViewById(R.id.list_view);
        suggestView = (ListView) findViewById(R.id.suggest_view);
        headFont = Typeface.createFromAsset(getAssets(), "fonts/Kalam-Regular.ttf");
        listHeading = (TextView) findViewById(R.id.list_heading);
        listHeading.setTypeface(headFont, 1);
        TextView suggestHeading = (TextView) findViewById(R.id.suggest_heading);
        suggestHeading.setTypeface(headFont);

        slHandler = new SLHandler(context, listView, suggestView);

//        Setup task handler

        Message repeat = Message.obtain(slHandler, MSG_REPEAT);
        slHandler.sendMessageDelayed(repeat, 1000 * sync_time);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scrn_width = size.x;
        scrn_height = size.y;

        // Add FloatingActionButton for "add" function

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                pickList = PickList.newInstance("", 0, false);
                pickList.show(ft, "dialog");
            }
        });
        log(String.format(Locale.getDefault(), "Starting MainActivity, time=%d", getTime()));
        updateAdapters(context, listView, suggestView);

        // Handle Shopping List item tap.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                EditText nameView = (EditText) item.findViewById(R.id.name);
                values.clear();
                long dBid = itemsList.get(position).id;
                if (db == null) db = MainActivity.itemDb.getWritableDatabase();
                String name = itemsList.get(position).name;
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                final String[] avgCols = {"last_time", "last_avg", "flags"};
                switch (clickLocation) {
                    case box:
                        clickLocation = ClickLocation.none;
                        time_millis = System.currentTimeMillis();
                        if (vibrate)
                            vb.vibrate(25);
                        updateAvg(dBid, 0);
                        if (autoSync) {
                            slHandler.removeMessages(MSG_SYNC);
                            Message syncmsg = Message.obtain(slHandler, MSG_SYNC);
                            slHandler.sendMessageDelayed(syncmsg, 1000 * autosync_time);
                            log("Sent autosync message");
                        }
                        break;
/*                    case name:
                        if(vibrate)
                            vb.vibrate(25);
                        pickList = PickList.newInstance(name,dBid,true);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        Bundle bundle = new Bundle();
                        bundle.putString("name",name);
                        bundle.putLong("_id",dBid);
                        bundle.putBoolean("inLists",true);  // Item is in the lists
                        pickList.setArguments(bundle);
                        pickList.show(ft,"dialog");
                        break;
*/
                    case del:
                        values.clear();
                        values.put("flags",6);  //  unset "inList" flag and set "delete" and "change" flags
                        values.put("last_time", getTime());
                        db.update("'" + currList + "'", values, "_id=" + Long.toString(dBid), null);
                        vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrate)
                            vb.vibrate(25);
                        updateAdapters(context, listView, suggestView);
                }
            }
        });

        // Handle Suggest List item tap.
        suggestView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item,
                                    int position, long id) {
                EditText nameView = (EditText) item.findViewById(R.id.name);
//                nameView.setWidth((int) .8*item.getWidth());
                values.clear();
                long dBid = itemsSuggest.get(position).id;
                String name = itemsSuggest.get(position).name;
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                final String[] avgCols = {"last_time", "last_avg", "flags"};
                switch (clickLocation) {
                    case box:
                        clickLocation = ClickLocation.none;
                        time_millis = System.currentTimeMillis();
                        if (vibrate)
                            vb.vibrate(25);
                        Cursor curs = db.query("'" + currList + "'", avgCols, "_id=" + Long.toString(dBid), null, "", "", "name ASC");
                        curs.moveToFirst();
                        long last = curs.getLong(0);
                        long avg = curs.getLong(1);
                        long ct = getTime();
                        values.put("flags",1);     // set "Inlist" flags
                        values.put("last_time", last + 1);
                        if (db == null)
                            db = itemDb.getWritableDatabase();
                        db.update("'" + currList + "'", values,
                                "_id=" + Long.toString(dBid), null);
                        curs.close();
                        updateAdapters(context, listView, suggestView);
                        if (autoSync) {
                            slHandler.removeMessages(MSG_SYNC);
                            Message syncmsg = Message.obtain(slHandler, MSG_SYNC);
                            slHandler.sendMessageDelayed(syncmsg, 1000 * autosync_time);
                        }
                        break;
/*                    case name:
                        if(vibrate)
                            vb.vibrate(25);
                        pickList = PickList.newInstance(name,dBid,true);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        Bundle bundle = new Bundle();
                        bundle.putString("name",name);
                        bundle.putLong("_id",dBid);
                        bundle.putBoolean("inLists",true);  // Item is in the lists
                        pickList.setArguments(bundle);
                        pickList.show(ft,"dialog");
                        break;
*/
                    case del:
                        if (vibrate)
                            vb.vibrate(25);
                        values.clear();
//                        values.put("flags",(itemsSuggest.get(position).flags)|2);
                        values.put("flags",7);  //  set "inList", "delete" and "change" flags
                        values.put("last_time", getTime());
                        db = itemDb.getWritableDatabase();
                        db.update("'" + currList + "'", values, "_id=" + Long.toString(dBid), null);
                        updateAdapters(context, listView, suggestView);
                        break;
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

        /**
         * Update Frequency averages in database for item with given id.
         *
         * @param id     ID of currently selected item
         * @param flags New state of flags entry
         */

        final String[] avgCols = {"last_time", "last_avg", "_id","name"};

    public void updateAvg(long id, int flags) {

        logF("start updateAvg = %d ms", System.currentTimeMillis() - time_millis);
        if (db == null)
            db = itemDb.getWritableDatabase();
        Cursor curs = db.query("'" + currList + "'", avgCols, "_id=" + Long.toString(id), null, "", "", "name ASC");
        curs.moveToFirst();
        long last = curs.getLong(0);
        long avg = curs.getLong(1);
        long ct = getTime();
        String nm = curs.getString(3);
        values.clear();
        values.put("flags",flags);
        if (avg > 36499 * day) { // Check for "one time item"
            db.delete("'" + currList + "'", "_id=" + Long.toString(id), null); // and delete
        } else
            values.put("last_avg", running_avg(ct - last, avg));
        values.put("last_time", ct + 1);
        logF("last_time = %d\t current time = %d\t, last_avg = %d\t, elapsed = %d", last, ct, avg, ct - last);
        db.update("'" + currList + "'", values, "_id=" + Long.toString(id), null);
        updateAdapters(context, listView, suggestView);
        logF("after updateAvg end = %d ms", System.currentTimeMillis() - time_millis);
        curs.close();
    }
    /**
     * running_avg(elapsed_time,last_avg:
     *      Calculates new average buy time from last buy time and the last average
     *
     * @param elapsed_time
     * @param last_avg
     * @return
     */
    public long running_avg(long elapsed_time, long last_avg) {
        return (long) (0.75 * elapsed_time + 0.25 * last_avg);
    }


    /*    /**
     * addItem(View): called from add_button ("+")
     * @param View
     */
/*    public void addItem(View view){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        pickList = PickList.newInstance("",0,false);
        pickList.show(ft,"dialog");
        db = itemDb.getWritableDatabase();
        updateAdapters(context,listView,suggestView);
    }
*/
    @Override
    public void pickDone(){
        itemDb = new ItemDb(getContext());
        db = itemDb.getWritableDatabase();
        updateAdapters(context,listView,suggestView);
    }
        // Setup Sidebar options

            /**
     * Implements Listener
     * Show the sidebar
     */
    public void showLists(){
        showLists(context);
    }

    //      Setup slide in menu for List management

    static DrawerLayout drawers;
    LinearLayout navList;
    Button newList,closeList;

    void setupOptions() {
        navList = (LinearLayout) findViewById(R.id.navList);
        newList = (Button) findViewById(R.id.newList);
        lists = (ListView) findViewById(R.id.lists);

//        closeList = (Button) findViewById(R.id.closeList);
            // Create list

        newList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                NewListDialog.newInstance();
                NewListDialog newListDialog = new NewListDialog();
                newListDialog.show(ft,"dialog");
            }
        });
            // List of lists
        showLists();
                            // Regular tap
        lists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                Cursor c = (Cursor) parent.getItemAtPosition(position);
                currList = c.getString(1);
                actionBar.setTitle(c.getString(1));
                SharedPreferences.Editor ed = prefs.edit();
                ed.putString("currList",c.getString(1));
                ed.apply();
                updateAdapters(context,listView,suggestView);
                closeDrawer();
                logF("name = %s",c.getString(1));
            }
        });                         // Long tap
        lists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View item, int position, long id) {
                Cursor c = (Cursor) parent.getItemAtPosition(position);
                LongClickDialog lcd = LongClickDialog.newInstance(c.getString(1),c.getLong(0));
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                lcd.show(ft,"dialog");
                return true;
            }
        });
            // Close Button
/*        closeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawers.closeDrawer(navList);
            }
        });
*/    }
    public void showLists(Context context) {
        listsCursor = db.query("lists", new String[] {"_id","name"}, null, null, null, null, null);
        logF("listsCursor count = %d",listsCursor.getCount());
        SimpleCursorAdapter adpt = new SimpleCursorAdapter(
                context, R.layout.lists_layout,
                listsCursor,new String[] {"name"},new int[]{R.id.name}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        lists.setAdapter(adpt);
//        listsCursor.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        FragmentManager fm=getSupportFragmentManager();
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.settings:
                log("settings");
                Intent intent = new Intent("com.symdesign.smartlist.intent.action.Settings");
                startActivity(intent);
                break;
            case R.id.mic:
                log("Microphone clicked");
                SpeechRecognitionHelper.run(mainActivity);
                return true;
            case R.id.sync:     // Sync
                if(email.equals("no_email")){ // New user
                    AdminDialog adminDialog = new AdminDialog();
                    adminDialog.show(fm,"dialog");
                } else {
                    LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = layoutInflater.inflate(R.layout.popup_window,null);
                    // Create popup window
                    popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView textView = (TextView) popupView.findViewById(R.id.text);
                    Button abortButton = (Button) popupView.findViewById(R.id.abort_button);
                    abortButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popupWindow.dismiss();
                            syncList.cancel(true);
                        }
                    });
                    drawers.requestLayout();
                    textView.setText("Syncing List");
                    popupWindow.setContentView(popupView);
                    popupWindow.showAtLocation(scrollView,Gravity.TOP,0,200);
//                    popupWindow.showAsDropDown(scrollView,Gravity.TOP,0,-150);
                    // Run SyncList
                    syncList = new SyncList(this,email,passwd,currList,listView,suggestView).execute();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onFinishAdminDialog(String nemail,String npasswd,Boolean reg) {

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(nemail).matches()) {
            showToast(getContext(), "Invalid email address!", Toast.LENGTH_LONG);
        } else {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.popup_window, null);
            // Create popup window
            popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView textView = (TextView) popupView.findViewById(R.id.text);
            Button abortButton = (Button) popupView.findViewById(R.id.abort_button);
            if (reg) {
                email = nemail;
                passwd = npasswd;
                SharedPreferences.Editor ed = MainActivity.prefs.edit();
                ed.putString("email",email);
                ed.putString("passwd",passwd);
                ed.apply();
                abortButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                        auth.cancel(true);
                    }
                });
                drawers.requestLayout();
                textView.setText("creating account");
                popupWindow.setContentView(popupView);
                popupWindow.showAtLocation(scrollView, Gravity.TOP, 0, 200);
                auth = new Auth(this, email, passwd, currList, "new").execute();
            } else {
                abortButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                        getLists.cancel(true);
                    }
                });
                drawers.requestLayout();
                textView.setText("Accessing Account");
                popupWindow.setContentView(popupView);
                popupWindow.showAtLocation(scrollView, Gravity.TOP, 0, 200);
                getLists = new GetLists(mainActivity, nemail, npasswd, false);
                getLists.setListener((GetLists.LoginListener) MainActivity.context);
                getLists.execute();
            }
        }
    }
    @Override
    public void fin(){
        popupWindow.dismiss();
    }
    public void onFinishAuth(String cmd, String rslt) {
        log(rslt);
        auth.cancel(true);
        switch(cmd) {
            case "new" :
                if(rslt.equals("exists")){
                    toast = Toast.makeText(getApplicationContext(),
                            "\nAlready Exists\n",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 200);
                    toast.show();
                    FragmentManager fm = getSupportFragmentManager();
                    AdminDialog adminDialog = new AdminDialog();
                    adminDialog.show(fm, "dialog");
                } else {
                    new SyncList(this,email, passwd, currList, listView,suggestView).execute();
                }
                break;
//            case "add" :
//                new Auth(this,email,passwd,currList,"add");
        }
    }

    public void onFinishNewList() {
        new Auth(this,email,passwd,currList,"add").execute();
    }

    public void onFinishDelList(String listName) {
        new Auth(this,email,passwd,listName,"del").execute();
    }
    //      Show lists table
    static void printLists() {
        Cursor lsts = db.query("lists",new String[] {"name"},null,null,null,null,null);
        for(lsts.moveToFirst(); !lsts.isAfterLast(); lsts.moveToNext()) {
            logF("name = %s",lsts.getString(0));
        }
        lsts.close();
    }
    public void closeDrawer() {drawers.closeDrawers();}

    public Context getContext() {
        return mainActivity;
    }

    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
//        db.close();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    /**
     * update Adapters:
     *      1.  Scans Shopping and Suggestions list updating
     *          the list arrays itemsList and itemsSuggest.
     *      2.  Sets the adapters listAdapter and suggestAdapter from
     *          these list arrays.
     */
    static private Cursor listCursor, suggestCursor ;
    private static SLAdapter listAdapter,suggestAdapter;

/*    static void remoteUpdate(Context context,ListView listView,ListView suggestView) {
        db = itemDb.getWritableDatabase();
        updateAdapters(context,listView,suggestView);
    }
*/
    static void updateAdapters() {
        updateAdapters(MainActivity.context,listView,suggestView);
    }

    static void updateAdapters(Context context,ListView listView,ListView suggestView) {
        logF("Starting updateAdapters()");
        db = itemDb.getWritableDatabase();
//        listCursor = db.query("'"+MainActivity.currList+"'", cols, "", null, "", "", null);
//        for (listCursor.moveToFirst(); !listCursor.isAfterLast(); listCursor.moveToNext()) {
//            logF("name = %s, flags = %d,_id = %d",
//                    listCursor.getString(1), listCursor.getInt(2),listCursor.getInt(0));
//        }
        listCursor = db.query("'"+MainActivity.currList+"'", cols, "flags=1 or flags=5", null, "", "", null);
        itemsList.clear();
        int n = 0;
        long time_sec = MainActivity.getTime();
        for (listCursor.moveToFirst(); !listCursor.isAfterLast(); listCursor.moveToNext()) {
            logF("list name = %s, flags = %d,_id = %d",
                    listCursor.getString(1),listCursor.getInt(2),listCursor.getInt(0));
            float ratio = Math.abs(((float) (getTime()-listCursor.getLong(3)))/((float) listCursor.getLong(4)));
            long la = listCursor.getLong(4);
            int et = (int) (time_sec-listCursor.getLong(3));    // elapsed time
            if(et > 2*la){      // If elapsed time is greater than twice avg, set avg to elapsed time
                values.clear();
                values.put("last_avg",et);
                db.update("'"+currList+"'", values, "_id=" + listCursor.getInt(0), null);
            } else {
                itemsList.add(new Item(listCursor.getLong(0), listCursor.getString(1),
                        listCursor.getLong(2), listCursor.getLong(3), listCursor.getLong(4),
                        ratio));
                n++;
            }
        }
        Collections.sort(itemsList);
        listAdapter = new SLAdapter(context, itemsList, R.layout.list_entry);
        listAdapter.checked = false;
        listView.setAdapter(listAdapter);
        // get cursor for suggestion list
        suggestCursor = db.query("'"+MainActivity.currList+"'", cols, "flags=0 or flags=4", null, "", "",
                "ratio DESC");
        itemsSuggest.clear();
        n = 0;
        for (suggestCursor.moveToFirst(); !suggestCursor.isAfterLast(); suggestCursor.moveToNext()) {
            logF("suggest name = %s, flags = %d, id = %d",
                    suggestCursor.getString(1),suggestCursor.getInt(2),suggestCursor.getInt(0));
            long la = suggestCursor.getLong(4);
            int et = (int) (time_sec-suggestCursor.getLong(3));    // elapsed time
            float ratio = Math.abs(((float) (getTime()-suggestCursor.getLong(3)))/((float) suggestCursor.getLong(4)));
            if(et > 2*la){
                values.clear();
                values.put("last_avg",et);
                db.update("'"+currList+"'", values, "_id=" + suggestCursor.getInt(0), null);
            } else {
                itemsSuggest.add(new Item(suggestCursor.getLong(0), suggestCursor.getString(1),
                        suggestCursor.getLong(2), suggestCursor.getLong(3), suggestCursor.getLong(4),
                        ratio));
                n++;
            }
        }
        Collections.sort(itemsSuggest);
        suggestAdapter = new SLAdapter(context, itemsSuggest, R.layout.suggest_entry);
        suggestAdapter.checked = true;
        suggestView.setAdapter(suggestAdapter);
        logF("updateAdapters end = %d",System.currentTimeMillis()-time_millis);
    }
/*    static void calcRatios(ArrayList<Item> list){

        for(int k=0; k<list.size(); k++){
            Item item= list.get(k);
            long time_since_last = getTime() - item.last_time;
            item.ratio = Math.abs(((float) (time_since_last))/((float) item.last_avg));
            list.set(k,item);
        }
    }
*/
    /**
     * updateRatios: Updates ratios averages for the displayed suggestions.
     *               Also updates last_avg if ratio>2 to (ct-last_time)/2
      */
/*    static void updateRatios() {

        logF("updateRatios start = %d ms",System.currentTimeMillis()-time_millis);
        Cursor curs = db.query("'"+currList+"'", cols, "flags=0 or flags=1", null, "", "", "name ASC");
        for(curs.moveToFirst();!curs.isAfterLast();curs.moveToNext()){
            long time_since_last = getTime() - curs.getLong(3);
            int flags = curs.getInt(2);
            float ratio = Math.abs(((float) (time_since_last))/((float) curs.getLong(4)));
            values.clear();
            if(time_since_last > max_time) {    // If item hasn't been purchace for max_time
                values.put("flags",flags | 2);      // set delete flag
                logF("deleting %s",curs.getString(1));
            }
            values.put("ratio", ratio);
            db.update("'" + MainActivity.currList + "'", values, String.format("_id=%d", curs.getLong(0)), null);
//           logF("name %s, last_time %d,last_avg %d,ratio %f",curs.getString(1),ct-curs.getLong(3),curs.getLong(4),ratio);
		}
        logF("updateRatios end = %d ms",System.currentTimeMillis()-time_millis);
    }
*/    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
 /*       if (id == R.id.action_settings) {
            return true;
        }
*/

    public void initDB(SQLiteDatabase db) {
//		values = new ContentValues();
        Long date = getTime() - 30;
        values = new ContentValues();
        values.clear();
        addItem("apples", 1, date, 30, 0.0);
        addItem("oranges", 1, date, 30, 0.0);
        addItem("milk", 0, date, 30, 0.0);
        addItem("bacon", 0, date, 30, 0.0);
        addItem("eggs", 0, date, 30, 0.0);
        addItem("lettuce", 1, date, 30, 0.0);
        addItem("OJ", 0, date, 30, 0.0);
        addItem("cereal", 0, date, 30, 0.0);
        addItem("blueberries", 1, date, 30, 0.0);
        addItem("kefir", 0, date, 30, 0.0);
        addItem("potatoes", 0, date, 30, 0.0);
        addItem("wine", 0, date, 30, 0.0);
    }

    public static void addItem(String nm, int flgs, long lt, long la, double r) {
        values.clear();
        values.put("name", nm);
        values.put("flags", flgs);
        values.put("last_time", lt);
        values.put("last_avg", la);
        values.put("ratio", r);
        db.insert("'"+currList+"'", null, values);
    }

    public static void changeItem(String nm, int flgs, long lt, long la, double r, long id) {
        values.clear();
        values.put("name", nm);
        values.put("flags", flgs);
        values.put("last_time", Math.abs(lt));
        values.put("last_avg", la);
        values.put("ratio", r);
        db.update("'"+currList+"'", values, "_id=" + Long.toString(id), null);
    }

    public static void deleteItem(long id) {
        db.delete("'"+currList+"'", "_id=" + Long.toString(id), null);
    }

    public static void newItem(String nm) {
        addItem(nm, 1, getTime(), 3 * day, 0);
    }

    /**
     * Returns time in seconds since 1/1/1970 epoch
     *
     * @return The time in ms returned
     *
     */
    public static long getTime() {
        return System.currentTimeMillis() / 1000;
    }

    public static void log(String str) {
        Log.v("smartlist", str);
    }

    public static void logF(String fmt, Object... arg) {
        Log.v("smartlist", String.format(fmt, arg));
    }
    static void showToast(Context ctx,String msg,int len){
        Toast t = Toast.makeText(ctx,msg,len);
        t.setGravity(Gravity.TOP, 0, 200);
        t.show();
    }
    // Activity Results handler
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // if it’s speech recognition results
        // and process finished ok
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {

            // receiving a result in string array
            // there can be some strings because sometimes speech recognizing inaccurate
            // more relevant results in the beginning of the list
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // in “matches” array we holding a results... let’s show the most relevant
            if (matches.size() > 0)
                Toast.makeText(this, ((CharSequence) matches.get(0)), Toast.LENGTH_LONG).show();
            Long date = getTime();
            String name = (String) matches.get(0);
            // A negative value of flags indicates a new item
            db = MainActivity.itemDb.getWritableDatabase();
            MainActivity.addItem(name, 1, getTime(), 7 * day, 0);
            updateAdapters(context,listView,suggestView);
            db.close();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Created by dennis on 2/18/16.
     */
    @Override
    public void onPause() {
        super.onPause();
        log("Pausing MainActivity");
        db.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        log("Resuming MainActivity");
        db = itemDb.getWritableDatabase();
        updateAdapters(context,listView,suggestView);
    }
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SmartList") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://www.symdesign.us/listmate"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
}

