package com.symdesign.smartlist;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.os.Handler;
import android.widget.ListView;

import static com.symdesign.smartlist.MainActivity.currList;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.passwd;
import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.db;


/**
 * @ Copyright Dennis Rogers 2/18/18
 * Created by dennis on 1/23/18.
 * Syncs a list on the phone with the server
 * returns: "ok" if password was ok and sync was successful
 *          "nok" if account does not exist or password is not valid
 */

public class SLHandler extends Handler {
    Context context;
    ListView listView,suggestView;

    static final int MSG_REPEAT=1,MSG_SYNC=2,MSG_RATIO=3;

    public SLHandler(Context c,ListView lv, ListView sv) {
        context = c;
        listView = lv;
        suggestView = sv;
    }

    @Override public void handleMessage(Message msg) {
        switch(msg.what) {
            case MSG_REPEAT:
                Message repeat = Message.obtain(this,MSG_REPEAT);
                sendMessageDelayed(repeat,1000*MainActivity.refresh_time);
                db = MainActivity.itemDb.getWritableDatabase();
                MainActivity.updateAdapters(context,listView,suggestView);
                log("refresh!");
                break;
            case MSG_SYNC:
                log("Starting autosync timer");
                new SyncList(MainActivity.mainActivity,email,passwd,currList,listView,suggestView).execute();
                break;
/*            case MSG_RATIO:
                MainActivity.updateRatios();
                log("Updating Ratios");
*/        }
    }
}