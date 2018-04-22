package com.symdesign.smartlist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import static com.symdesign.smartlist.MainActivity.autoSync;
import static com.symdesign.smartlist.MainActivity.vibrate;

import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.passwd;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.currList;
import static com.symdesign.smartlist.MainActivity.syncReg;
/*
 * Created by dennis on 2/12/17.
 */

public class Settings extends Activity {
    static CheckBox autoSyncBox,vibrateBox;
    TextView syncSettings;
    Toast toast;
    static final int Settings_Request = 1;

    public Settings() {
        // Empty contstuctor required for DialogFragment
    }
//    public interface SettingsListener {
//        void onFinishSettings(String email,String passwd);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        autoSyncBox = (CheckBox) findViewById(R.id.auto_sync);
        syncSettings = (TextView) findViewById(R.id.sync_settings);
        vibrateBox = (CheckBox) findViewById(R.id.vibrate);
        if(autoSync){
            autoSyncBox.setChecked(true);
        } else {
            autoSyncBox.setChecked(false);
        }
        if(vibrate)
            vibrateBox.setChecked(true);
        else
            vibrateBox.setChecked(false);
        autoSyncBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                autoSync = !autoSync;
                SharedPreferences.Editor ed = MainActivity.prefs.edit();     // Initialize shared preferences
                if(autoSync)
                    ed.putBoolean("autoSync", true);
                else
                    ed.putBoolean("autoSync",false);
                ed.apply();
        }});
        vibrateBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                vibrate = !vibrate;
                SharedPreferences.Editor ed = MainActivity.prefs.edit();     // Initialize shared preferences
                if(vibrate)
                    ed.putBoolean("vibrate",true);
                else
                    ed.putBoolean("vibrate",false);
                ed.apply();
            }});
        syncSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.symdesign.smartlist.intent.action.LoginSettings");
                startActivityForResult(intent,Settings_Request);
            }});
    }
    @Override
    protected void onActivityResult(int reqCode, int rsltCode, Intent data){
        if(reqCode==Settings_Request){
            if(rsltCode==Activity.RESULT_OK)
                finish();
        }
    }
}
